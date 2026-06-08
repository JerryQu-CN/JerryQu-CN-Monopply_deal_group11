package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.game.state.ActionState;
import com.example.monopoly_deal_game.game.state.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.state.ActionStateRent;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.GameConfig;
import com.example.monopoly_deal_game.logic.*;
import com.example.monopoly_deal_game.logic.payment.PaymentService;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.controller.dialog.ActionTargetDialogs;
import com.example.monopoly_deal_game.controller.dialog.DoubleTheRentPrompter;
import com.example.monopoly_deal_game.controller.dialog.JustSayNoHandler;
import com.example.monopoly_deal_game.controller.dialog.PaymentHandler;
import com.example.monopoly_deal_game.controller.gameplay.HandCardPicker;
import com.example.monopoly_deal_game.controller.gameplay.HudUpdater;
import com.example.monopoly_deal_game.controller.gameplay.PlayChromeBuilder;
import com.example.monopoly_deal_game.controller.gameplay.TargetSelectionHandler;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;
import com.example.monopoly_deal_game.network.HostLobbyBridge;
import com.example.monopoly_deal_game.network.NetworkMessage;
import com.example.monopoly_deal_game.view.GameActionLogger;
import com.example.monopoly_deal_game.view.GameplayUiBundle;
import com.example.monopoly_deal_game.view.GameplayViewCoordinator;
import com.example.monopoly_deal_game.view.animation.DrawMotion;
import com.example.monopoly_deal_game.view.animation.MotionContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Abstract base controller for the main gameplay screen — handles turn phases,
 * card play, payment flows, Just Say No chains, and UI refresh orchestration.
 */
public abstract class
AbstractGameplayScreenController implements StageAware, Initializable {

    // ---- FXML-injected fields ----
    @FXML protected AnchorPane gameRoot;
    @FXML protected HBox topbar;
    @FXML protected Button menuButton;
    @FXML protected Button debugButton;
    @FXML protected Label topbarTitle;
    @FXML protected VBox leftSidebar;
    @FXML protected Pane deckPane;
    @FXML protected Pane discardPane;
    @FXML protected Pane voidPane;
    @FXML protected Label movesLabel;
    @FXML private Label moneyHudLabel;
    @FXML private Label feedbackLabel;
    @FXML protected Button primaryActionButton;
    @FXML protected Label versionLabel;
    @FXML protected Pane opponentsPane;
    @FXML protected Pane selfBoardPane;
    @FXML protected Pane handPane;
    @FXML protected Pane actionLayer;
    @FXML protected StackPane menuOverlay;
    @FXML protected Button menuReturnButton;
    @FXML protected Button menuLeaveButton;

    // ---- game over overlay ----
    @FXML protected StackPane gameOverOverlay;
    @FXML protected Label gameOverWinnerLabel;
    @FXML protected Label gameOverDetailLabel;
    @FXML protected Button gameOverReturnButton;

    // ---- log panel ----
    @FXML private VBox logPanel;
    @FXML private ScrollPane logScrollPane;
    @FXML private VBox logEntriesBox;

    // ---- infrastructure ----
    protected Stage stage;
    protected GameplayViewCoordinator viewCoordinator;
    private final AtomicBoolean dialogBusy = new AtomicBoolean(false);
    private boolean drawAnimating = false;
    /**
     * Snapshot deferred while a dialog was busy — applied on the next
     * checkAndPromptJustSayNo entry, where local payment selections captured
     * in the fields below are merged in before publication.
     */
    private volatile GameSession deferredIncomingSession;
    private volatile List<Integer> lastPaymentCardIds;
    private volatile String lastPaymentReceiverName;

    // ---- extracted helpers ----
    private NetworkSyncHelper networkSync;
    private HandCardPicker handCardPicker;
    private JustSayNoHandler justSayNoHandler;
    private PaymentHandler paymentHandler;
    private PlayChromeBuilder playChromeBuilder;
    private TargetSelectionHandler targetSelectionHandler;

    private GameActionLogger actionLogger;
    private HudUpdater hudUpdater;
    private Consumer<NetworkMessage> networkSnapshotHandler;

    // ---- StageAware ----
    @Override public void setStage(Stage stage) { this.stage = stage; }

    // ---- initialize ----
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        versionLabel.setText("dev");
        if (actionLayer != null) actionLayer.setPickOnBounds(false);

        // create helpers
        networkSync = new NetworkSyncHelper(this::setFeedback);
        justSayNoHandler = new JustSayNoHandler(stage, this::setFeedback,
                this::refreshGameplayUi, networkSync, dialogBusy);
        paymentHandler = new PaymentHandler(stage, this::setFeedback, networkSync, dialogBusy,
                (receiver, cardIds) -> {
                    lastPaymentReceiverName = receiver;
                    lastPaymentCardIds = cardIds;
                });
        handCardPicker = new HandCardPicker(dialogBusy);
        playChromeBuilder = new PlayChromeBuilder();
        targetSelectionHandler = new TargetSelectionHandler(stage, dialogBusy);
        actionLogger = new GameActionLogger(logEntriesBox, logScrollPane,
                gameOverOverlay, gameOverWinnerLabel, gameOverDetailLabel);
        hudUpdater = new HudUpdater(topbarTitle, movesLabel, moneyHudLabel,
                primaryActionButton, feedbackLabel, this::setFeedback,
                handCardPicker, actionLogger, gameOverOverlay);

        // view coordinator
        viewCoordinator = new GameplayViewCoordinator(toUiBundle());
        viewCoordinator.setOnHandCardPick(this::handleHandCardPick);
        viewCoordinator.setOnTableWildCardClick(this::handleTableWildCardClick);
        viewCoordinator.setLocalPlayerName(AppContext.get().networkLobbyState().getLocalPlayerName());

        // register mediator UIs
        justSayNoHandler.installIntoMediator();
        paymentHandler.installIntoMediator();

        // network snapshot listener
        installNetworkSnapshotListener();

        if (gameRoot != null) gameRoot.getStyleClass().add("root-pane");

        refreshGameplayUi();
    }

    private void installNetworkSnapshotListener() {
        networkSnapshotHandler = msg -> {
            if (msg != null && msg.getType() == NetworkMessage.Type.PAYMENT_REQUEST) {
                paymentHandler.handleRequestMessage(msg);
                return;
            }
            if (msg != null && msg.getType() == NetworkMessage.Type.JUST_SAY_NO_REQUEST) {
                justSayNoHandler.handleRequestMessage(msg);
                return;
            }
            if (msg != null && msg.getType() == NetworkMessage.Type.SESSION_SNAPSHOT
                    && msg.getSession() != null) {
                boolean hasRemote = NetworkSyncHelper.hasRemoteClients();
                if (!hasRemote) {
                    String localLog = msg.getLogText();
                    if (localLog != null && !localLog.isBlank()) {
                        Platform.runLater(() -> {
                            actionLogger.appendLog(localLog,
                                    localLog.contains("used")
                                    || localLog.contains("charged")
                                    || localLog.contains("wins"));
                        });
                    }
                    return;
                }

                Platform.runLater(() -> {
                    GameSession incoming = msg.getSession();
                    if (dialogBusy.get()) {
                        // Defer rather than drop — the next refreshGameplayUi cycle
                        // will pick it up via checkAndPromptJustSayNo.
                        deferredIncomingSession = incoming;
                        return;
                    }
                    // Prevent local player's action response from being overwritten
                    // by a stale snapshot that hasn't processed our response yet.
                    // Only apply when the current and incoming action states are the
                    // same logical action (same class); otherwise a leftover resolved
                    // state from a prior action could block a new incoming action.
                    GameSession current = AppContext.get().gameEngine().getCurrentSession();
                    if (current != null) {
                        String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
                        ActionState curAs = current.getGameState().getActionState();
                        ActionState incAs = incoming.getGameState().getActionState();
                        if (curAs != null && incAs != null && localName != null
                                && curAs.getClass() == incAs.getClass()) {
                            Player curLocal = current.findPlayerByName(localName);
                            Player incLocal = incoming.findPlayerByName(localName);
                            if (curLocal != null && incLocal != null) {
                                boolean curResolved = curAs.isAccepted(curLocal) || curAs.isRefused(curLocal);
                                boolean incPending = !incAs.isAccepted(incLocal) && !incAs.isRefused(incLocal);
                                if (curResolved && incPending) {
                                    refreshGameplayUi();
                                    return;
                                }
                            }
                        }
                    }
                    AppContext.get().networkLobbyState().setSession(incoming);
                    AppContext.get().gameEngine().resumeSession(incoming);
                    handCardPicker.clearAll();
                    String netLog = msg.getLogText();
                    if (netLog != null && !netLog.isBlank()) {
                        boolean important = netLog.contains("Just Say No")
                                || netLog.contains("used")
                                || netLog.contains("charged")
                                || netLog.contains("wins");
                        actionLogger.appendLog(netLog, important);
                    }
                    if (!AppContext.get().networkLobbyState().isHost()) {
                        actionLogger.logRemoteSessionUpdate(msg.getSession());
                    }
                    refreshGameplayUi();
                });
            }
        };
        AppContext.get().networkLobbyState().addListener(networkSnapshotHandler);
        HostLobbyBridge.setListener(networkSnapshotHandler);
    }

    // ---- FXML actions ----

    @FXML protected void onMenu(ActionEvent event) {
        menuOverlay.setVisible(true);
        menuOverlay.setManaged(true);
    }

    @FXML protected void onMenuReturn(ActionEvent event) {
        menuOverlay.setVisible(false);
        menuOverlay.setManaged(false);
    }

    @FXML protected void onMenuLeave(ActionEvent event) { cleanupAndReturnToStart(); }

    @FXML protected void onGameOverReturn(ActionEvent event) { cleanupAndReturnToStart(); }

    private void cleanupAndReturnToStart() {
        handCardPicker.clearAll();
        justSayNoHandler.uninstallFromMediator();
        paymentHandler.uninstallFromMediator();
        if (networkSnapshotHandler != null) {
            AppContext.get().networkLobbyState().removeListener(networkSnapshotHandler);
            HostLobbyBridge.removeListener(networkSnapshotHandler);
        }
        AppContext.get().gameEngine().clearSession();
        if (stage != null) ScreenNavigation.show(stage, "StartScreen.fxml");
    }

    @FXML protected void onDebug(ActionEvent event) { /* reserved */ }

    @FXML protected void onPrimaryAction(ActionEvent event) {
        if (dialogBusy.get()) { setFeedback("Please complete the current dialog selection first."); return; }

        GameEngine engine = AppContext.get().gameEngine();
        GameSession session = engine.getCurrentSession();
        if (session == null) return;

        if (!isLocalPlayersTurn(session)) {
            setFeedback("It is " + (session.getCurrentPlayer() != null
                    ? session.getCurrentPlayer().getName() : "another player") + "'s turn. Please wait.");
            refreshGameplayUi();
            return;
        }

        GameLogic logic = engine.getGameLogic();
        GameState state = session.getGameState();
        if (state.isGameOver()) return;

        switch (state.getPhase()) {
            case DRAW_PHASE -> {
                drawAnimating = false;
                handleDrawPhase(logic, session, state);
                if (!drawAnimating) refreshGameplayUi();
            }
            case PLAY_PHASE -> {
                drawAnimating = false;
                handlePlayPhase(logic, session, state);
                if (!drawAnimating) refreshGameplayUi();
            }
            case DISCARD_PHASE -> {
                handleDiscardPhase(logic, session);
                refreshGameplayUi();
            }
            case WAITING_FOR_SELECTION -> {
                refreshGameplayUi();
                return;
            }
        }

        if (!drawAnimating) {
            String log = actionLogger.getPendingLog();
            actionLogger.clearPendingLog();
            networkSync.publishSessionChange(session, log);
        }
    }

    private void handleDrawPhase(GameLogic logic, GameSession session, GameState state) {
        clearFeedback();
        if (state.isHasDrawnThisTurn()) {
            setFeedback("Already drawn this turn. Play cards or end turn.");
        } else {
            Player cur = session.getCurrentPlayer();
            int before = cur != null ? cur.getHand().size() : 0;
            logic.drawCard(session);
            int after = cur != null ? cur.getHand().size() : 0;
            int newCards = after - before;
            if (cur != null) actionLogger.setPendingLog(cur.getName() + " drew a card");
            if (newCards > 0) {
                drawAnimating = true;
                playDrawSequence(newCards, () -> {
                    refreshGameplayUi();
                    String log = actionLogger.getPendingLog();
                    actionLogger.clearPendingLog();
                    networkSync.publishSessionChange(session, log);
                });
            }
        }
    }

    private void handlePlayPhase(GameLogic logic, GameSession session, GameState state) {
        Card selected = handCardPicker.getSelectedHandCard();
        Player cur = session.getCurrentPlayer();
        String who = cur != null ? cur.getName() : "?";
        if (selected == null) {
            clearFeedback();
            logic.endTurn(session);
            actionLogger.setPendingLog(who + " ended their turn");
            return;
        }
        if (PlayChromeBuilder.requiresExplicitPlayMode(selected)) {
            setFeedback("Use the action bar buttons above: Use Effect / Bank as Cash (or choose rent color).");
            return;
        }
        try {
            int before = cur != null ? cur.getHand().size() : 0;
            boolean ok = logic.playCard(session, selected);
            int after = cur != null ? cur.getHand().size() : 0;
            int newCards = after - before;
            if (ok) {
                clearFeedback();
                handCardPicker.setSelectedHandCard(null);
                actionLogger.setPendingLog(who + " played " + selected.getName());
                if (newCards > 0) {
                    drawAnimating = true;
                    playDrawSequence(newCards, () -> {
                        refreshGameplayUi();
                        if (logic.checkGameOver(session)) {
                            state.setGameOver(true);
                            actionLogger.announceWinner(session);
                        }
                        String log = actionLogger.getPendingLog();
                        actionLogger.clearPendingLog();
                        networkSync.publishSessionChange(session, log);
                    });
                    return;
                }
                if (logic.checkGameOver(session)) {
                    state.setGameOver(true);
                    actionLogger.announceWinner(session);
                }
            } else {
                setFeedback("Cannot play: must draw first, max " + GameConfig.MAX_PLAY_PER_TURN + " plays per turn reached, or hand reference invalid.");
            }
        } catch (IllegalStateException ex) {
            setFeedback(ex.getMessage() != null ? ex.getMessage() : "Play conditions not met");
        }
    }

    private void handleDiscardPhase(GameLogic logic, GameSession session) {
        Player cur = session.getCurrentPlayer();
        if (cur == null) return;
        int handSize = cur.getHand().size();
        int mustDiscard = handSize - GameConfig.MAX_HAND_SIZE_END_TURN;
        if (mustDiscard <= 0) return;

        int selected = handCardPicker.getDiscardSelections().size();
        if (selected < mustDiscard) {
            setFeedback("Please select at least " + (mustDiscard - selected)
                    + " more card(s) to discard (hand: " + handSize + ", must be <= "
                    + GameConfig.MAX_HAND_SIZE_END_TURN + ").");
        } else if (handSize - selected > GameConfig.MAX_HAND_SIZE_END_TURN) {
            setFeedback("Not enough cards selected: after discarding, hand would still exceed "
                    + GameConfig.MAX_HAND_SIZE_END_TURN + ". Please select more.");
        } else {
            int count = handCardPicker.getDiscardSelections().size();
            logic.discardFromHandChosen(session,
                    new ArrayList<>(handCardPicker.getDiscardSelections()));
            handCardPicker.clearAll();
            actionLogger.setPendingLog(cur.getName() + " discarded " + count + " card(s)");
        }
    }

    // ---- draw animation ----

    /** Play {@code count} card-fly animations sequentially, then invoke {@code onDone}. */
    private void playDrawSequence(int count, Runnable onDone) {
        if (count <= 0) { onDone.run(); return; }
        playNextDraw(count, 0, onDone);
    }

    private void playNextDraw(int total, int index, Runnable onDone) {
        if (index >= total) { onDone.run(); return; }
        MotionContext ctx = viewCoordinator.motionContext();
        new DrawMotion().play(ctx, () -> playNextDraw(total, index + 1, onDone));
    }

    // ---- hand card pick delegate ----



    private void handleHandCardPick(Card card) {
        GameSession session = AppContext.get().gameEngine().getCurrentSession();
        if (handCardPicker.handlePick(card, session, this::setFeedback)) {
            refreshGameplayUi();
        }
    }

    private void handleTableWildCardClick(PropertyCard pc) {
        GameSession session = AppContext.get().gameEngine().getCurrentSession();
        if (session == null) return;
        GameState gs = session.getGameState();
        if (gs.getPhase() != GameState.Phase.PLAY_PHASE) return;
        Player cur = session.getCurrentPlayer();
        if (cur == null || !isLocalPlayersTurn(session)) return;

        List<CardColor> colors = pc.getSelectableColors();
        if (colors.isEmpty()) return;

        ActionTargetDialogs.chooseColor(stage, "Switch Color",
                "Select a new color for \"" + pc.getName() + "\"", colors)
                .ifPresent(newColor -> {
                    GameLogic logic = AppContext.get().gameEngine().getGameLogic();
                    PropertyPlayHelper.moveWildCardToColor(cur, pc, newColor, session);
                    String who = cur.getName();
                    actionLogger.setPendingLog(who + " switched " + pc.getName() + " to " + CardColorLabel.shortLabel(newColor));
                    refreshGameplayUi();
                    String log = actionLogger.getPendingLog();
                    actionLogger.clearPendingLog();
                    networkSync.publishSessionChange(session, log);
                });
    }


    // ---- feedback helpers ----

    private void setFeedback(String msg) {
        if (feedbackLabel != null) {
            feedbackLabel.setText(msg == null ? "" : msg);
            feedbackLabel.setVisible(msg != null && !msg.isBlank());
            feedbackLabel.setManaged(msg != null && !msg.isBlank());
        }
    }

    private void clearFeedback() { setFeedback(""); }

    // ---- UI refresh ----

    protected void refreshGameplayUi() {
        GameSession session = AppContext.get().gameEngine().getCurrentSession();
        if (session == null) {
            handCardPicker.clearAll();
        } else {
            GameState gs = session.getGameState();
            if (gs.getPhase() == GameState.Phase.DISCARD_PHASE) {
                handCardPicker.setSelectedHandCard(null);
                handCardPicker.pruneDiscardSelections(session.getCurrentPlayer());
            } else {
                handCardPicker.clearDiscardSelections();
            }
        }
        // auto-trigger JSN dialog if local player is targeted by an action
        checkAndPromptJustSayNo(session);

        // Safety: clean up any finished action states that weren't removed.
        // When all targets have responded (accepted/refused), the action state
        // should be popped so getPhase() returns PLAY_PHASE instead of stuck at
        // WAITING_FOR_SELECTION.
        if (session != null) {
            GameState gs = session.getGameState();
            ActionState as = gs.getActionState();
            if (as != null && as != gs.getTurnState() && as.isFinished()) {
                gs.removeActionState(as);
            }
        }

        hudUpdater.applyHud(session);
        if (viewCoordinator != null) {
            viewCoordinator.refreshFromSession(session,
                    handCardPicker.getSelectedHandCard(),
                    handCardPicker.getDiscardSelections());
        }
        // play chrome
        Card selected = handCardPicker.getSelectedHandCard();
        VBox dock = playChromeBuilder.build(session, selected,
                opts -> tryPlayWithOptions(session, opts), this::refreshGameplayUi);
        if (dock != null) {
            playChromeBuilder.attach(actionLayer, dock);
        } else {
            playChromeBuilder.detach(actionLayer);
        }
    }

    // ---- try-play with options ----

    private void tryPlayWithOptions(GameSession session, CardPlayOptions options) {
        if (dialogBusy.get()) { setFeedback("Please complete the current dialog selection first."); return; }
        if (!isLocalPlayersTurn(session)) {
            setFeedback("It is " + (session.getCurrentPlayer() != null
                    ? session.getCurrentPlayer().getName() : "another player") + "'s turn. Please wait.");
            refreshGameplayUi();
            return;
        }

        GameLogic logic = AppContext.get().gameEngine().getGameLogic();
        Card card = handCardPicker.getSelectedHandCard();
        if (card == null || session == null) return;

        try {
            CardPlayOptions merged = targetSelectionHandler.mergeTargets(
                    session, card, options);
            if (merged == null) {
                setFeedback("Target player selection cancelled.");
                refreshGameplayUi();
                return;
            }
            Player cur = session.getCurrentPlayer();
            // Prompt for Double The Rent before playing a rent card
            boolean usedDoubleRent = false;
            if (!merged.asBankMoney() && card instanceof RentCard && cur != null) {
                Card doubleCard = DoubleTheRentPrompter.findDoubleTheRent(cur);
                if (doubleCard != null && DoubleTheRentPrompter.showPrompt(stage)) {
                    usedDoubleRent = true;
                    cur.getHand().removeCard(doubleCard);
                    session.discardCard(doubleCard);
                    session.getGameState().setDoubleNextRent(true);
                    session.getGameState().setDoubleRentCount(1);
                }
            }
            int before = cur != null ? cur.getHand().size() : 0;
            boolean ok = logic.playCard(session, card, merged);
            int after = cur != null ? cur.getHand().size() : 0;
            int newCards = after - before;
            if (ok) {
                if (!merged.jsnBlocked()) clearFeedback();
                String who = cur != null ? cur.getName() : "?";
                actionLogger.logCardPlay(who, card, merged, session);
                if (usedDoubleRent) {
                    actionLogger.setPendingLog(who + " used Double The Rent — rent doubled (x2)");
                }
                handCardPicker.setSelectedHandCard(null);
                if (logic.checkGameOver(session)) {
                    session.getGameState().setGameOver(true);
                    actionLogger.announceWinner(session);
                }
                if (newCards > 0) {
                    playDrawSequence(newCards, () -> {
                        refreshGameplayUi();
                        String log2 = actionLogger.getPendingLog();
                        actionLogger.clearPendingLog();
                        networkSync.publishSessionChange(session, log2);
                    });
                    return;
                }
                refreshGameplayUi();
                String log2 = actionLogger.getPendingLog();
                actionLogger.clearPendingLog();
                networkSync.publishSessionChange(session, log2);
            } else {
                setFeedback("Cannot play like this. Check turn order, play count, or prerequisites.");
                refreshGameplayUi();
            }
        } catch (IllegalStateException ex) {
            setFeedback(ex.getMessage() != null ? ex.getMessage() : "Play conditions not met");
            refreshGameplayUi();
        }
    }

    // ---- small utilities ----

    protected GameplayUiBundle toUiBundle() {
        return new GameplayUiBundle(gameRoot, topbar, leftSidebar,
                deckPane, discardPane, voidPane, opponentsPane,
                selfBoardPane, handPane, actionLayer, menuOverlay, chatPaneOrNull());
    }

    protected Pane chatPaneOrNull() { return null; }

    private boolean isLocalPlayersTurn(GameSession session) {
        if (session == null) return false;
        Player local = session.localPlayer(AppContext.get().networkLobbyState().getLocalPlayerName());
        Player current = session.getCurrentPlayer();
        return local != null && current != null && local.equals(current);
    }

    /**
     * Check all pending targets of the current action state.
     * For each unresponded human target: offer JSN if held, otherwise auto-accept.
     * For automated targets: auto-accept immediately.
     * In networked games only the local player is checked; offline covers all.
     * <p>
     * State change (setAccepted) and side effects (payment/transfer) are separate steps.
     * Network sync happens once after all targets in the loop are resolved.
     */
    private void checkAndPromptJustSayNo(GameSession session) {
        if (session == null || dialogBusy.get()) return;

        // Discard any payment card IDs from a previous action — they'll be
        // re-captured by the payment dialog if the current action needs them.
        lastPaymentCardIds = null;
        lastPaymentReceiverName = null;

        // Process any snapshot that was deferred while a dialog was open.
        GameSession deferred = deferredIncomingSession;
        if (deferred != null) {
            deferredIncomingSession = null;
            session = applySession(deferred);
        }

        GameState gs = session.getGameState();
        ActionState as = gs.getActionState();
        if (as == null || as == gs.getTurnState()) return;

        // Determine which targets to process.
        boolean hasRemote = NetworkSyncHelper.hasRemoteClients();

        String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
        java.util.List<Player> toCheck = new java.util.ArrayList<>();
        if (hasRemote) {
            Player local = session.localPlayer(localName);
            if (local != null && as.isTarget(local)
                    && !as.isRefused(local) && !as.isAccepted(local)) {
                toCheck.add(local);
            }
        } else {
            for (Player t : as.getTargetPlayers()) {
                if (!as.isRefused(t) && !as.isAccepted(t)) {
                    toCheck.add(t);
                }
            }
        }
        if (toCheck.isEmpty()) return;

        boolean stateChanged = false;
        for (Player target : toCheck) {
            if (as.isRefused(target) || as.isAccepted(target)) continue;

            // In offline mode, all non-local players are auto-accepted.
            // In networked mode, each client only processes its own target (above),
            // so the single target here is always the local human player.
            boolean effectivelyAI = !hasRemote && localName != null && !localName.equals(target.getName());

            if (effectivelyAI) {
                as.setAccepted(target, true);
                applyAcceptedSideEffects(as, target);
                stateChanged = true;
                continue;
            }

            // Human target: offer JSN if they hold one
            if (JustSayNoHandler.playerHasJustSayNo(target)) {
                String desc = as.getStatus() != null && !as.getStatus().isBlank()
                        ? as.getStatus()
                        : "An action card was used against you. You may use Just Say No to oppose.";
                boolean used = justSayNoHandler.promptDialog(
                        target, as.getActionOwner(), session, desc);
                if (used) {
                    actionLogger.setPendingLog(target.getName() + " used Just Say No to refuse "
                            + as.getActionOwner().getName() + "'s action");
                    refreshGameplayUi();
                    String logJsn = actionLogger.getPendingLog();
                    actionLogger.clearPendingLog();
                    networkSync.publishSessionChange(session, logJsn);
                    return; // state changed, restart from top
                }
                if (as.isRefused(target) || as.isAccepted(target)) continue;
            }

            // No JSN (or chose not to use it) — accept with side effects
            as.setAccepted(target, true);
            applyAcceptedSideEffects(as, target);
            stateChanged = true;
        }

        if (stateChanged && hasRemote) {
            GameSession def = deferredIncomingSession;
            if (def != null) {
                deferredIncomingSession = null;
                mergeLocalResponseIntoDeferred(session, def);
                session = applySession(def);
            }
            String logSt = actionLogger.getPendingLog();
            actionLogger.clearPendingLog();
            networkSync.publishSessionChange(session, logSt);
        }
    }

    /**
     * Merge our local player's action-state response and payment card selection
     * into a deferred snapshot.  The caller guarantees that {@code localSession}
     * already has the local player as either accepted or refused — this method
     * only copies that status (and replays payment transfers) into {@code deferred}.
     */
    private void mergeLocalResponseIntoDeferred(GameSession localSession, GameSession deferred) {
        String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
        if (localName == null) return;
        Player localPlayer = localSession.findPlayerByName(localName);
        Player defPlayer = deferred.findPlayerByName(localName);
        if (localPlayer == null || defPlayer == null) return;
        ActionState curAs = localSession.getGameState().getActionState();
        ActionState defAs = deferred.getGameState().getActionState();
        if (curAs == null || defAs == null
                || curAs.getClass() != defAs.getClass()
                || defAs == deferred.getGameState().getTurnState()) return;

        if (curAs.isAccepted(localPlayer)) {
            defAs.setAccepted(defPlayer, true);
            List<Integer> cardIds = lastPaymentCardIds;
            String receiverName = lastPaymentReceiverName;
            lastPaymentCardIds = null;
            lastPaymentReceiverName = null;
            if (cardIds != null && !cardIds.isEmpty() && receiverName != null) {
                Player receiver = deferred.findPlayerByName(receiverName);
                if (receiver != null) {
                    List<Card> resolved = PaymentService.resolveByIds(defPlayer, cardIds);
                    if (!resolved.isEmpty()) {
                        PaymentService.applyChosenPayment(defPlayer, receiver, resolved, deferred);
                    }
                }
            }
        } else if (curAs.isRefused(localPlayer)) {
            defAs.setRefused(defPlayer, true);
        }
    }

    /** Set the engine and lobby state to the given session. */
    private static GameSession applySession(GameSession session) {
        AppContext.get().networkLobbyState().setSession(session);
        AppContext.get().gameEngine().resumeSession(session);
        return session;
    }

    /** Execute business logic (payment, property transfer) after a target accepts. */
    private static void applyAcceptedSideEffects(ActionState as, Player target) {
        as.tryExecuteOnAccepted(target);
    }

}