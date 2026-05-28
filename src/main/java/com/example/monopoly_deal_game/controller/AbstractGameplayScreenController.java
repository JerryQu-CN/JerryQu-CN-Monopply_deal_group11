package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.game.model.ActionState;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.logic.*;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class
AbstractGameplayScreenController implements StageAware, Initializable {

    private static final double[] UI_SCALE_STEPS = {0.85, 1.0, 1.15, 1.3};
    private final AtomicInteger scaleIndex = new AtomicInteger(1);

    // ---- FXML-injected fields ----
    @FXML protected AnchorPane gameRoot;
    @FXML protected HBox topbar;
    @FXML protected Button menuButton;
    @FXML protected Button debugButton;
    @FXML protected Label topbarTitle;
    @FXML protected VBox leftSidebar;
    @FXML protected Button uiMinusButton;
    @FXML protected Label uiScaleLabel;
    @FXML protected Button uiPlusButton;
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

    // ---- StageAware ----
    @Override public void setStage(Stage stage) { this.stage = stage; }

    // ---- initialize ----
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        versionLabel.setText("dev");
        syncScaleLabel();
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
            undoButton.setTooltip(new Tooltip("暂未开放：原计划用于撤回上一次打出的牌并恢复棋盘状态。"));
        }
        if (gameRoot != null) gameRoot.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
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
                Platform.runLater(() -> {
                    AppContext.get().networkLobbyState().setSession(msg.getSession());
                    AppContext.get().gameEngine().resumeSession(msg.getSession());
                    handCardPicker.clearAll();
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

    @FXML protected void onDebug(ActionEvent event) { /* reserved */ }

    @FXML protected void onUiMinus(ActionEvent event) {
        int i = scaleIndex.get();
        if (i > 0) { scaleIndex.set(i - 1); syncScaleLabel(); }
    }

    @FXML protected void onUiPlus(ActionEvent event) {
        int i = scaleIndex.get();
        if (i < UI_SCALE_STEPS.length - 1) { scaleIndex.set(i + 1); syncScaleLabel(); }
    }

    @FXML protected void onPrimaryAction(ActionEvent event) {
        if (dialogBusy.get()) { setFeedback("请先完成当前弹窗选择。"); return; }

        GameEngine engine = AppContext.get().gameEngine();
        GameSession session = engine.getCurrentSession();
        if (session == null) return;

        if (!isLocalPlayersTurn(session)) {
            setFeedback("当前轮到 " + (session.getCurrentPlayer() != null
                    ? session.getCurrentPlayer().getName() : "其他玩家") + "，请等待。");
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
            case WAITING_FOR_SELECTION -> setFeedback("请等待弹窗选择完成。");
        }

        networkSync.publishSessionChange(session);
        refreshGameplayUi();
    }

    private void handleDrawPhase(GameLogic logic, GameSession session, GameState state) {
        clearFeedback();
        if (state.isHasDrawnThisTurn()) {
            setFeedback("本回合已经摸过牌，请出牌或结束回合。");
        } else {
            logic.drawCard(session);
        }
    }

    private void handlePlayPhase(GameLogic logic, GameSession session, GameState state) {
        Card selected = handCardPicker.getSelectedHandCard();
        if (selected == null) {
            clearFeedback();
            logic.endTurn(session);
            return;
        }
        if (PlayChromeBuilder.requiresExplicitPlayMode(selected)) {
            setFeedback("请使用牌桌上方蓝色条内的按钮：使用效果 / 存入银行（或选租金颜色）。");
            return;
        }
        try {
            boolean ok = logic.playCard(session, selected);
            if (ok) {
                clearFeedback();
                handCardPicker.setSelectedHandCard(null);
                if (logic.checkGameOver(session)) state.setGameOver(true);
            } else {
                setFeedback("无法出牌：须先摸牌、本回合已满" + GameConfig.MAX_PLAY_PER_TURN + "张，或手牌引用异常。");
            }
        } catch (IllegalStateException ex) {
            setFeedback(ex.getMessage() != null ? ex.getMessage() : "出牌条件不满足");
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
            setFeedback("请至少再选 " + (mustDiscard - selected)
                    + " 张弃掉（当前 " + handSize + " 张手牌，弃后须 ≤"
                    + GameConfig.MAX_HAND_SIZE_END_TURN + "）。");
        } else if (handSize - selected > GameConfig.MAX_HAND_SIZE_END_TURN) {
            setFeedback("所选张数不够：弃掉这些后手牌仍会超过 "
                    + GameConfig.MAX_HAND_SIZE_END_TURN + " 张，请多选几张。");
        } else {
            logic.discardFromHandChosen(session,
                    new ArrayList<>(handCardPicker.getDiscardSelections()));
            handCardPicker.clearAll();
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
        if (dialogBusy.get()) { setFeedback("请先完成当前弹窗选择。"); return; }
        if (!isLocalPlayersTurn(session)) {
            setFeedback("当前轮到 " + (session.getCurrentPlayer() != null
                    ? session.getCurrentPlayer().getName() : "其他玩家") + "，请等待。");
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
                setFeedback("已取消选择目标玩家。");
                refreshGameplayUi();
                return;
            }
            boolean ok = logic.playCard(session, card, merged);
            if (ok) {
                if (!merged.jsnBlocked()) clearFeedback();
                handCardPicker.setSelectedHandCard(null);
                if (logic.checkGameOver(session)) session.getGameState().setGameOver(true);
                networkSync.publishSessionChange(session);
            } else {
                setFeedback("无法这样打出，请检查手序、次数或前提条件。");
            }
        } catch (IllegalStateException ex) {
            setFeedback(ex.getMessage() != null ? ex.getMessage() : "出牌条件不满足");
        }
        refreshGameplayUi();
    }

    // ---- HUD ----

    private void applyHud(GameSession session) {
        if (session == null) {
            topbarTitle.setText("Monopoly Deal — 未开始对局");
            movesLabel.setText("—");
            if (moneyHudLabel != null) moneyHudLabel.setText("资金 —\n未开局");
            primaryActionButton.setText("开始/摸牌");
            primaryActionButton.setDisable(true);
            return;
        }
        GameState state = session.getGameState();
        Player current = session.getCurrentPlayer();
        Player local = localPlayer(session);
        String who = current != null ? current.getName() : "?";
        boolean myTurn = isLocalPlayersTurn(session);

        if (state.isGameOver()) {
            topbarTitle.setText("对局结束");
            movesLabel.setText("—");
            if (moneyHudLabel != null) moneyHudLabel.setText("资金 —\n已结束");
            primaryActionButton.setText("已结束");
            primaryActionButton.setDisable(true);
            return;
        }

        topbarTitle.setText("当前回合: " + who + (myTurn ? "（你）" : "（等待）")
                + "  │  " + formatPhase(state.getPhase()));
        movesLabel.setText("本回合出牌: " + state.getCardsPlayedThisTurn()
                + " / " + GameConfig.MAX_PLAY_PER_TURN);
        if (moneyHudLabel != null) moneyHudLabel.setText(
                MoneyHudText.forPlayer(local != null ? local : current));

        switch (state.getPhase()) {
            case DRAW_PHASE -> {
                primaryActionButton.setText(myTurn ? "摸牌" : "等待 " + who + " 摸牌");
                primaryActionButton.setDisable(!myTurn);
            }
            case PLAY_PHASE -> {
                if (feedbackLabel != null && feedbackLabel.getText() != null
                        && feedbackLabel.getText().startsWith("提示：")) feedbackLabel.setText("");
                if (feedbackLabel != null)
                    feedbackLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #c62828; -fx-padding: 2 0 4 0;");
                if (!myTurn) {
                    primaryActionButton.setText("等待 " + who + " 出牌");
                    primaryActionButton.setDisable(true);
                } else {
                    Card sel = handCardPicker.getSelectedHandCard();
                    if (sel == null) {
                        primaryActionButton.setDisable(false);
                        primaryActionButton.setText("结束回合");
                    } else if (PlayChromeBuilder.requiresExplicitPlayMode(sel)) {
                        primaryActionButton.setDisable(true);
                        primaryActionButton.setText("请用上方按钮出牌");
                    } else if (sel instanceof BankCard) {
                        primaryActionButton.setDisable(false);
                        primaryActionButton.setText("打出(入银行)");
                    } else {
                        primaryActionButton.setDisable(false);
                        primaryActionButton.setText("打出选中牌");
                    }
                }
            }
            case DISCARD_PHASE -> {
                int handSize = current != null ? current.getHand().size() : 0;
                int must = Math.max(0, handSize - GameConfig.MAX_HAND_SIZE_END_TURN);
                int sel = handCardPicker.getDiscardSelections().size();
                if (!myTurn) {
                    primaryActionButton.setText("等待 " + who + " 弃牌");
                    primaryActionButton.setDisable(true);
                } else if (must <= 0) {
                    primaryActionButton.setText("弃牌");
                    primaryActionButton.setDisable(true);
                } else {
                    primaryActionButton.setText("确认弃掉所选（至少 " + must + " 张）");
                    primaryActionButton.setDisable(
                            sel < must || handSize - sel > GameConfig.MAX_HAND_SIZE_END_TURN);
                }
            }
            case WAITING_FOR_SELECTION -> {
                primaryActionButton.setText("等待玩家响应…");
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

    private void syncScaleLabel() {
        double s = UI_SCALE_STEPS[scaleIndex.get()];
        uiScaleLabel.setText(String.format("%.2fx", s));
        if (gameRoot != null) { gameRoot.setScaleX(s); gameRoot.setScaleY(s); }
    }

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
     */
    private void checkAndPromptJustSayNo(GameSession session) {
        if (session == null || dialogBusy.get()) return;

        GameState gs = session.getGameState();
        ActionState as = gs.getActionState();
        if (as == null || as == gs.getTurnState()) return;

        // Determine which targets to process.
        // In a truly networked game (multiple machines), only the local player needs to be
        // checked here — other clients receive the session snapshot and process their own
        // targets. In a single-machine game, all targets must be processed locally.
        String roomId = AppContext.get().networkLobbyState().getRoomId();
        boolean hasRemote = false;
        if (roomId != null && !roomId.isBlank() && AppContext.get().networkLobbyState().isHost()) {
            com.example.monopoly_deal_game.network.GameServer.Room room =
                    AppContext.get().gameServer().getRoom(roomId);
            hasRemote = room != null && room.hasRemoteClients();
        }
        if (!hasRemote && roomId != null && !roomId.isBlank()
                && !AppContext.get().networkLobbyState().isHost()) {
            // Non-host clients are remote by definition
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

        for (Player target : toCheck) {
            if (as.isRefused(target) || as.isAccepted(target)) continue;

            if (target.isAI()) {
                // AI: auto-accept (JSN logic handled inside JustSayNoMediator if needed)
                as.setAccepted(target, true);
                continue;
            }

            // Human target
            if (JustSayNoHandler.playerHasJustSayNo(target)) {
                String desc = as.getStatus() != null && !as.getStatus().isBlank()
                        ? as.getStatus()
                        : "对方对你使用了行动牌，你可以使用 Just Say No 反对。";
                boolean used = justSayNoHandler.promptDialog(
                        target, as.getActionOwner(), session, desc);
                if (used) {
                    networkSync.publishSessionChange(session);
                    refreshGameplayUi();
                    return; // state changed, restart from top
                }
                // re-check after dialog
                if (as.isRefused(target) || as.isAccepted(target)) continue;
            }

            // No JSN or chose not to use it — auto-accept
            as.setAccepted(target, true);
        }
    }

    private static String formatPhase(GameState.Phase phase) {
        if (phase == null) return "—";
        return switch (phase) {
            case DRAW_PHASE -> "阶段: 摸牌";
            case PLAY_PHASE -> "阶段: 出牌";
            case DISCARD_PHASE -> "阶段: 弃牌";
            case WAITING_FOR_SELECTION -> "阶段: 等待选择…";
        };
    }
}