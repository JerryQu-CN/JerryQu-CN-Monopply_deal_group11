package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.game.model.ActionState;
import com.example.monopoly_deal_game.game.model.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.model.ActionStateRent;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.logic.*;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;
import com.example.monopoly_deal_game.network.NetworkMessage;
import com.example.monopoly_deal_game.view.GameplayUiBundle;
import com.example.monopoly_deal_game.view.GameplayViewCoordinator;
import com.example.monopoly_deal_game.view.MoneyHudText;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
    @FXML protected Button undoButton;
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

    // ---- extracted helpers ----
    private NetworkSyncHelper networkSync;
    private HandCardPicker handCardPicker;
    private JustSayNoHandler justSayNoHandler;
    private PaymentHandler paymentHandler;
    private PlayChromeBuilder playChromeBuilder;
    private TargetSelectionHandler targetSelectionHandler;

    // ---- log state tracking (prevent duplicate entries across refreshes) ----
    private String lastKnownTurnPlayer;
    private GameState.Phase lastKnownPhase;
    private String lastLoggedActionOwner;
    private String pendingNetworkLogText;

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
        paymentHandler = new PaymentHandler(stage, this::setFeedback, networkSync);
        handCardPicker = new HandCardPicker(dialogBusy);
        playChromeBuilder = new PlayChromeBuilder();
        targetSelectionHandler = new TargetSelectionHandler(stage, dialogBusy);

        // view coordinator
        viewCoordinator = new GameplayViewCoordinator(toUiBundle());
        viewCoordinator.setOnHandCardPick(this::handleHandCardPick);

        // register mediator UIs
        justSayNoHandler.installIntoMediator();
        paymentHandler.installIntoMediator();

        // network snapshot listener
        installNetworkSnapshotListener();

        // disable undo (not yet implemented)
        if (undoButton != null) {
            undoButton.setDisable(true);
            undoButton.setTooltip(new Tooltip("Not yet available: planned to undo the last played card and restore board state."));
        }
        if (gameRoot != null) gameRoot.getStyleClass().add("root-pane");
        refreshGameplayUi();
    }

    private void installNetworkSnapshotListener() {
        Consumer<NetworkMessage> handler = msg -> {
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
                // Only restore session from network snapshot when there are remote
                // clients. In a single-machine game the local session is already
                // authoritative — replacing it asynchronously causes stale-state races.
                String roomId = AppContext.get().networkLobbyState().getRoomId();
                boolean hasRemote = false;
                if (roomId != null && !roomId.isBlank()
                        && AppContext.get().networkLobbyState().isHost()) {
                    com.example.monopoly_deal_game.network.GameServer.Room room =
                            AppContext.get().gameServer().getRoom(roomId);
                    hasRemote = room != null && room.hasRemoteClients();
                }
                if (!hasRemote && roomId != null && !roomId.isBlank()
                        && !AppContext.get().networkLobbyState().isHost()) {
                    hasRemote = true;
                }
                if (!hasRemote) {
                    // single-machine: don't restore session from network, but still display log
                    String localLog = msg.getLogText();
                    if (localLog != null && !localLog.isBlank()) {
                        Platform.runLater(() -> {
                            appendLog(localLog,
                                    localLog.contains("used")
                                    || localLog.contains("charged")
                                    || localLog.contains("wins"));
                        });
                    }
                    return;
                }

                Platform.runLater(() -> {
                    AppContext.get().networkLobbyState().setSession(msg.getSession());
                    AppContext.get().gameEngine().resumeSession(msg.getSession());
                    handCardPicker.clearAll();
                    String netLog = msg.getLogText();
                    if (netLog != null && !netLog.isBlank()) {
                        boolean important = netLog.contains("Just Say No")
                                || netLog.contains("used")
                                || netLog.contains("charged")
                                || netLog.contains("wins");
                        appendLog(netLog, important);
                    }
                    if (!AppContext.get().networkLobbyState().isHost()) {
                        logRemoteSessionUpdate(msg.getSession());
                    }
                    refreshGameplayUi();
                });
            }
        };
        AppContext.get().networkLobbyState().addListener(handler);
        HostLobbyBridge.setListener(handler);
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

    @FXML protected void onMenuLeave(ActionEvent event) {
        handCardPicker.clearAll();
        justSayNoHandler.uninstallFromMediator();
        paymentHandler.uninstallFromMediator();
        AppContext.get().gameEngine().clearSession();
        if (stage != null) ScreenNavigation.show(stage, "StartScreen.fxml");
    }

    @FXML protected void onGameOverReturn(ActionEvent event) {
        handCardPicker.clearAll();
        justSayNoHandler.uninstallFromMediator();
        paymentHandler.uninstallFromMediator();
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
            case DRAW_PHASE -> handleDrawPhase(logic, session, state);
            case PLAY_PHASE -> handlePlayPhase(logic, session, state);
            case DISCARD_PHASE -> handleDiscardPhase(logic, session);
            case WAITING_FOR_SELECTION -> setFeedback("Please wait for the dialog selection to complete.");
        }

        refreshGameplayUi();
        String log = pendingNetworkLogText;
        pendingNetworkLogText = null;
        networkSync.publishSessionChange(session, log);
    }

    private void handleDrawPhase(GameLogic logic, GameSession session, GameState state) {
        clearFeedback();
        if (state.isHasDrawnThisTurn()) {
            setFeedback("Already drawn this turn. Play cards or end turn.");
        } else {
            logic.drawCard(session);
            Player cur = session.getCurrentPlayer();
            if (cur != null) setPendingLog(cur.getName() + " drew a card");
        }
    }

    private void handlePlayPhase(GameLogic logic, GameSession session, GameState state) {
        Card selected = handCardPicker.getSelectedHandCard();
        Player cur = session.getCurrentPlayer();
        String who = cur != null ? cur.getName() : "?";
        if (selected == null) {
            clearFeedback();
            logic.endTurn(session);
            setPendingLog(who + " ended their turn");
            return;
        }
        if (PlayChromeBuilder.requiresExplicitPlayMode(selected)) {
            setFeedback("Use the action bar buttons above: Use Effect / Bank as Cash (or choose rent color).");
            return;
        }
        try {
            boolean ok = logic.playCard(session, selected);
            if (ok) {
                clearFeedback();
                handCardPicker.setSelectedHandCard(null);
                setPendingLog(who + " played " + selected.getName());
                if (logic.checkGameOver(session)) {
                    state.setGameOver(true);
                    announceWinner(session);
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
            setPendingLog(cur.getName() + " discarded " + count + " card(s)");
        }
    }

    @FXML protected void onUndo(ActionEvent event) { /* reserved */ }

    // ---- hand card pick delegate ----

    private void handleHandCardPick(Card card) {
        GameSession session = AppContext.get().gameEngine().getCurrentSession();
        if (handCardPicker.handlePick(card, session, this::setFeedback)) {
            refreshGameplayUi();
        }
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

    public void refreshSessionUi() { refreshGameplayUi(); }

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

        applyHud(session);
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
            boolean ok = logic.playCard(session, card, merged);
            if (ok) {
                if (!merged.jsnBlocked()) clearFeedback();
                Player cur = session.getCurrentPlayer();
                String who = cur != null ? cur.getName() : "?";
                logCardPlay(who, card, merged, session);
                handCardPicker.setSelectedHandCard(null);
                if (logic.checkGameOver(session)) {
                    session.getGameState().setGameOver(true);
                    announceWinner(session);
                }
                refreshGameplayUi();
                String log2 = pendingNetworkLogText;
                pendingNetworkLogText = null;
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

    // ---- HUD ----

    private void applyHud(GameSession session) {
        if (session == null) {
            topbarTitle.setText("Monopoly Deal -- No game started");
            movesLabel.setText("--");
            if (moneyHudLabel != null) moneyHudLabel.setText("Funds --\nNot started");
            primaryActionButton.setText("Start/Draw");
            primaryActionButton.setDisable(true);
            return;
        }
        GameState state = session.getGameState();
        Player current = session.getCurrentPlayer();
        Player local = localPlayer(session);
        String who = current != null ? current.getName() : "?";
        boolean myTurn = isLocalPlayersTurn(session);

        if (state.isGameOver()) {
            topbarTitle.setText("Game Over");
            movesLabel.setText("--");
            if (moneyHudLabel != null) moneyHudLabel.setText("Funds --\nFinished");
            primaryActionButton.setText("Finished");
            primaryActionButton.setDisable(true);
            if (gameOverOverlay != null && !gameOverOverlay.isVisible()) {
                gameOverOverlay.setVisible(true);
                gameOverOverlay.setManaged(true);
            }
            return;
        }

        logStateTransition(session);

        topbarTitle.setText("Turn: " + who + (myTurn ? " (You)" : " (Waiting)")
                + "  |  " + formatPhase(state.getPhase()));
        movesLabel.setText("Plays this turn: " + state.getCardsPlayedThisTurn()
                + " / " + GameConfig.MAX_PLAY_PER_TURN);
        if (moneyHudLabel != null) moneyHudLabel.setText(
                MoneyHudText.forPlayer(local != null ? local : current));

        switch (state.getPhase()) {
            case DRAW_PHASE -> {
                primaryActionButton.setText(myTurn ? "Draw" : "Waiting for " + who + " to draw");
                primaryActionButton.setDisable(!myTurn);
            }
            case PLAY_PHASE -> {
                if (feedbackLabel != null && feedbackLabel.getText() != null
                        && feedbackLabel.getText().startsWith("Hint:")) feedbackLabel.setText("");
                if (!myTurn) {
                    primaryActionButton.setText("Waiting for " + who + " to play");
                    primaryActionButton.setDisable(true);
                } else {
                    Card sel = handCardPicker.getSelectedHandCard();
                    if (sel == null) {
                        primaryActionButton.setDisable(false);
                        primaryActionButton.setText("End Turn");
                    } else if (PlayChromeBuilder.requiresExplicitPlayMode(sel)) {
                        primaryActionButton.setDisable(true);
                        primaryActionButton.setText("Use action bar above");
                    } else if (sel instanceof BankCard) {
                        primaryActionButton.setDisable(false);
                        primaryActionButton.setText("Play (Bank)");
                    } else {
                        primaryActionButton.setDisable(false);
                        primaryActionButton.setText("Play Selected");
                    }
                }
            }
            case DISCARD_PHASE -> {
                int handSize = current != null ? current.getHand().size() : 0;
                int must = Math.max(0, handSize - GameConfig.MAX_HAND_SIZE_END_TURN);
                int sel = handCardPicker.getDiscardSelections().size();
                if (!myTurn) {
                    primaryActionButton.setText("Waiting for " + who + " to discard");
                    primaryActionButton.setDisable(true);
                } else if (must <= 0) {
                    primaryActionButton.setText("Discard");
                    primaryActionButton.setDisable(true);
                } else {
                    primaryActionButton.setText("Confirm discard (min " + must + " card(s))");
                    primaryActionButton.setDisable(
                            sel < must || handSize - sel > GameConfig.MAX_HAND_SIZE_END_TURN);
                }
            }
            case WAITING_FOR_SELECTION -> {
                primaryActionButton.setText("Waiting for response...");
                primaryActionButton.setDisable(true);
            }
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
        Player local = localPlayer(session);
        Player current = session != null ? session.getCurrentPlayer() : null;
        return local != null && current != null && local.equals(current);
    }

    private Player localPlayer(GameSession session) {
        if (session == null) return null;
        return session.localPlayer(AppContext.get().networkLobbyState().getLocalPlayerName());
    }

    /**
     * Check all pending targets of the current action state.
     * For each unresponded human target: offer JSN if held, otherwise auto-accept.
     * For AI targets: auto-accept immediately.
     * In networked games only the local player is checked; offline covers all.
     * <p>
     * State change (setAccepted) and side effects (payment/transfer) are separate steps.
     * Network sync happens once after all targets in the loop are resolved.
     */
    private void checkAndPromptJustSayNo(GameSession session) {
        if (session == null || dialogBusy.get()) return;

        GameState gs = session.getGameState();
        ActionState as = gs.getActionState();
        if (as == null || as == gs.getTurnState()) return;

        // Determine which targets to process.
        String roomId = AppContext.get().networkLobbyState().getRoomId();
        boolean hasRemote = false;
        if (roomId != null && !roomId.isBlank() && AppContext.get().networkLobbyState().isHost()) {
            com.example.monopoly_deal_game.network.GameServer.Room room =
                    AppContext.get().gameServer().getRoom(roomId);
            hasRemote = room != null && room.hasRemoteClients();
        }
        if (!hasRemote && roomId != null && !roomId.isBlank()
                && !AppContext.get().networkLobbyState().isHost()) {
            hasRemote = true;
        }

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

            if (target.isAI()) {
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
                    setPendingLog(target.getName() + " used Just Say No to refuse "
                            + as.getActionOwner().getName() + "'s action");
                    refreshGameplayUi();
                    String logJsn = pendingNetworkLogText;
                    pendingNetworkLogText = null;
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

        // Push updated state once after all targets processed.
        // Host broadcasts to all clients; non-host clients send PLAYER_ACTION to host.
        if (stateChanged && hasRemote) {
            String logSt = pendingNetworkLogText;
            pendingNetworkLogText = null;
            networkSync.publishSessionChange(session, logSt);
        }
    }

    /** Execute business logic (payment, property transfer) after a target accepts. */
    private static void applyAcceptedSideEffects(ActionState as, Player target) {
        if (as instanceof ActionStatePlayerTargeted pts && pts.hasOnAccepted()) {
            pts.executeOnAccepted(target);
        } else if (as instanceof ActionStateRent rent && rent.hasOnAccepted()) {
            rent.executeOnAccepted(target);
        }
    }

    // ---- player log ----

    /** Logs normal operations (draw, play, discard, etc.). */
    private void appendLog(String text) {
        appendLog(text, false);
    }

    /** Logs important operations (highlights action cards used against others). */
    private void appendLog(String text, boolean important) {
        if (logEntriesBox == null) return;
        Label entry = new Label(text);
        entry.setWrapText(true);
        entry.setMaxWidth(230);
        entry.setFocusTraversable(false);
        entry.setMouseTransparent(true);
        if (important) {
            entry.getStyleClass().add("log-entry-important");
        } else {
            entry.getStyleClass().add("log-entry-normal");
        }
        logEntriesBox.getChildren().add(entry);
        Platform.runLater(() -> logScrollPane.setVvalue(1.0));
    }

    private void setPendingLog(String text) {
        pendingNetworkLogText = text;
    }

    /** System separator log (local display only, not synced to network). */
    private void appendSeparator(String text) {
        if (logEntriesBox == null) return;
        Label entry = new Label(text);
        entry.setWrapText(true);
        entry.setMaxWidth(230);
        entry.setFocusTraversable(false);
        entry.setMouseTransparent(true);
        entry.getStyleClass().add("log-entry-separator");
        logEntriesBox.getChildren().add(entry);
        Platform.runLater(() -> logScrollPane.setVvalue(1.0));
    }

    private void announceWinner(GameSession session) {
        Player winner = null;
        int maxSets = 0;
        for (Player p : session.getPlayers()) {
            int sets = p.getFullSetCount();
            if (sets > maxSets) { maxSets = sets; winner = p; }
        }
        if (winner != null) {
            setPendingLog(" Game over! " + winner.getName() + " wins! (" + maxSets + " full sets)");
            if (gameOverWinnerLabel != null) {
                gameOverWinnerLabel.setText(winner.getName() + " is the winner!");
            }
            if (gameOverDetailLabel != null) {
                gameOverDetailLabel.setText(maxSets + " full property sets completed");
            }
            if (gameOverOverlay != null) {
                gameOverOverlay.setVisible(true);
                gameOverOverlay.setManaged(true);
            }
        }
    }

    /** Detects and logs turn/phase/waiting state changes (called on every applyHud; internally deduplicated). */
    private void logStateTransition(GameSession session) {
        GameState gs = session.getGameState();
        Player cur = session.getCurrentPlayer();
        String curName = cur != null ? cur.getName() : null;
        GameState.Phase phase = gs.getPhase();

        // Turn switch
        if (!Objects.equals(curName, lastKnownTurnPlayer) && curName != null) {
            lastKnownTurnPlayer = curName;
            appendSeparator("-- " + curName + "'s turn --");
        }

        // Action state waiting
        ActionState as = gs.getActionState();
        if (phase == GameState.Phase.WAITING_FOR_SELECTION
                && as != null && as != gs.getTurnState()) {
            String owner = as.getActionOwner() != null ? as.getActionOwner().getName() : "?";
            String desc = as.getStatus() != null && !as.getStatus().isBlank()
                    ? as.getStatus() : "";
            if (!Objects.equals(owner, lastLoggedActionOwner)
                    || lastKnownPhase != phase) {
                lastLoggedActionOwner = owner;
                appendLog("Waiting for " + owner + "'s action response..." + desc);
            }
        } else {
            lastLoggedActionOwner = null;
        }

        lastKnownPhase = phase;
    }

    /** Extracts key actions from a remotely synced session and writes them to the log. */
    private void logRemoteSessionUpdate(GameSession session) {
        GameState gs = session.getGameState();
        ActionState as = gs.getActionState();
        if (as == null || as == gs.getTurnState()) return;

        String owner = as.getActionOwner() != null ? as.getActionOwner().getName() : "?";
        java.util.List<Player> targets = as.getTargetPlayers();
        String desc = as.getStatus() != null && !as.getStatus().isBlank()
                ? "(" + as.getStatus() + ")" : "";

        if (targets != null && !targets.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < targets.size(); i++) {
                if (i > 0) sb.append(i == targets.size() - 1 ? " and " : ", ");
                sb.append(targets.get(i).getName());
            }
            appendLog(owner + " used an action card on " + sb + desc, true);
        } else {
            appendLog(owner + " used an action card" + desc, true);
        }
    }

    /** Generates the appropriate log entry based on card type and play options. */
    private void logCardPlay(String who, Card card, CardPlayOptions opts, GameSession session) {
        if (opts.asBankMoney()) {
            setPendingLog(who + " banked " + card.getName() + " (+" + card.getValue() + "M)");
            return;
        }
        if (card instanceof PropertyCard) {
            setPendingLog(who + " placed " + card.getName() + " on the table");
            return;
        }
        if (card instanceof RentCard rc) {
            String color = opts.rentColorChoice() != null
                    ? CardColorLabel.shortLabel(opts.rentColorChoice()) : "?";
            String dbl = session.getGameState().isDoubleNextRent() ? " (x2)" : "";
            setPendingLog(who + " charged " + color + " rent" + dbl);
            return;
        }
        if (card instanceof ActionCard) {
            String cardName = card.getName();
            Player target = opts.actionTargetPlayer();
            if (target != null) {
                setPendingLog(who + " used " + cardName + " on " + target.getName());
            } else {
                setPendingLog(who + " used " + cardName);
            }
            return;
        }
        // fallback
        setPendingLog(who + " played " + card.getName());
    }

    private static String formatPhase(GameState.Phase phase) {
        if (phase == null) return "--";
        return switch (phase) {
            case DRAW_PHASE -> "Phase: Draw";
            case PLAY_PHASE -> "Phase: Play";
            case DISCARD_PHASE -> "Phase: Discard";
            case WAITING_FOR_SELECTION -> "Phase: Waiting...";
        };
    }
}