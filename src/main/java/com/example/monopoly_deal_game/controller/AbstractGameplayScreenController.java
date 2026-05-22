package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.logic.*;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.RentCard;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.view.GameplayUiBundle;
import com.example.monopoly_deal_game.view.GameplayViewCoordinator;
import com.example.monopoly_deal_game.view.MoneyHudText;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractGameplayScreenController implements StageAware, Initializable {
    private static final double[] UI_SCALE_STEPS = {0.85, 1.0, 1.15, 1.3};
    private final AtomicInteger scaleIndex = new AtomicInteger(1);
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
    protected Stage stage;
    protected GameplayViewCoordinator viewCoordinator;
    private Card selectedHandCard;
    private final LinkedHashSet<Card> discardSelections = new LinkedHashSet<>();
    private InvalidationListener playChromeLayoutListener;
    private static final double PLAY_CHROME_BOTTOM_INSET = 216;
    @Override public void setStage(Stage stage) { this.stage = stage; }
    @Override public void initialize(URL location, ResourceBundle resources) { versionLabel.setText("dev"); syncScaleLabel(); if (actionLayer != null) actionLayer.setPickOnBounds(false); viewCoordinator = new GameplayViewCoordinator(toUiBundle()); viewCoordinator.setOnHandCardPick(this::handleHandCardPick); installJustSayNoHandler(); installPaymentPicker(); java.util.function.Consumer<com.example.monopoly_deal_game.network.NetworkMessage> snapshotHandler = msg -> { if (msg != null && msg.getType() == com.example.monopoly_deal_game.network.NetworkMessage.Type.SESSION_SNAPSHOT && msg.getSession() != null) { javafx.application.Platform.runLater(() -> { AppContext.get().networkLobbyState().setSession(msg.getSession()); AppContext.get().gameEngine().resumeSession(msg.getSession()); selectedHandCard = null; discardSelections.clear(); refreshGameplayUi(); }); } }; AppContext.get().networkLobbyState().addListener(snapshotHandler); HostLobbyBridge.setListener(snapshotHandler); if (undoButton != null) { undoButton.setDisable(true); undoButton.setTooltip(new Tooltip("暂未开放：原计划用于撤回上一次打出的牌并恢复棋盘状态。\n当前对局无快照机制，为避免误触已禁用该按钮。")); } if (gameRoot != null) gameRoot.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;"); refreshGameplayUi(); }
    private void installJustSayNoHandler() { JustSayNoMediator.installUi(this::promptJustSayNoDialog); }
    private void installPaymentPicker() { PaymentPickerMediator.installUi((payer, amountDueM, receiver, session, reasonText) -> { if (payer == null || PaymentService.totalLiquidityValue(payer) <= 0) return java.util.Optional.empty(); return PaymentPickDialogs.choosePaymentCards(stage, payer, amountDueM, receiver, session, reasonText); }); }
    private boolean promptJustSayNoDialog(Player respondent, Player activator, String situation) { GameSession session = AppContext.get().gameEngine().getCurrentSession(); Player local = localPlayer(session); if (local == null || respondent == null || !local.getName().equals(respondent.getName())) return false; var useNo = new ButtonType("使用 Just Say No（反对）", ButtonBar.ButtonData.APPLY); Alert alert = new Alert(Alert.AlertType.CONFIRMATION, situation + "\n\n你的「反对（Just Say No）」在手牌或银行中。\n• 若使用：当场弃掉该张反对，并抵消本条仅对你的效果。\n• 若不使用：结算照常继续。", useNo, new ButtonType("不使用反对（继续结算）", ButtonBar.ButtonData.CANCEL_CLOSE)); alert.setTitle("Just Say No"); if (stage != null) alert.initOwner(stage); String act = activator != null ? activator.getName() : "对方"; alert.setHeaderText(respondent.getName() + " — 是否要反对「" + act + "」的举动？"); return alert.showAndWait().filter(r -> r == useNo).isPresent(); }
    protected GameplayUiBundle toUiBundle() { return new GameplayUiBundle(gameRoot, topbar, leftSidebar, deckPane, discardPane, voidPane, opponentsPane, selfBoardPane, handPane, actionLayer, menuOverlay, chatPaneOrNull()); }
    protected Pane chatPaneOrNull() { return null; }
    @FXML protected void onMenu(ActionEvent event) { menuOverlay.setVisible(true); menuOverlay.setManaged(true); }
    @FXML protected void onMenuReturn(ActionEvent event) { menuOverlay.setVisible(false); menuOverlay.setManaged(false); }
    @FXML protected void onMenuLeave(ActionEvent event) { selectedHandCard = null; discardSelections.clear(); JustSayNoMediator.clearUi(); PaymentPickerMediator.clearUi(); AppContext.get().gameEngine().clearSession(); if (stage != null) ScreenNavigation.show(stage, "StartScreen.fxml"); }
    @FXML protected void onDebug(ActionEvent event) { }
    @FXML protected void onUiMinus(ActionEvent event) { int i = scaleIndex.get(); if (i > 0) { scaleIndex.set(i - 1); syncScaleLabel(); } }
    @FXML protected void onUiPlus(ActionEvent event) { int i = scaleIndex.get(); if (i < UI_SCALE_STEPS.length - 1) { scaleIndex.set(i + 1); syncScaleLabel(); } }
    @FXML protected void onPrimaryAction(ActionEvent event) { GameEngine engine = AppContext.get().gameEngine(); GameSession session = engine.getCurrentSession(); if (session == null) return; if (!isLocalPlayersTurn(session)) { setFeedback("当前轮到 " + (session.getCurrentPlayer() != null ? session.getCurrentPlayer().getName() : "其他玩家") + "，请等待。 "); refreshGameplayUi(); return; } GameLogic logic = engine.getGameLogic(); GameState state = session.getGameState(); if (state.isGameOver()) return; if (state.getPhase() == GameState.Phase.DRAW_PHASE) { clearFeedback(); if (state.isHasDrawnThisTurn()) setFeedback("本回合已经摸过牌，请出牌或结束回合。"); else logic.drawCard(session); } else if (state.getPhase() == GameState.Phase.PLAY_PHASE) { if (selectedHandCard != null) { if (requiresExplicitPlayMode(selectedHandCard)) { setFeedback("请使用牌桌上方蓝色条内的按钮：使用效果 / 存入银行（或选租金颜色）。"); } else { try { boolean ok = logic.playCard(session, selectedHandCard); if (ok) { clearFeedback(); selectedHandCard = null; if (logic.checkGameOver(session)) state.setGameOver(true); } else { setFeedback("无法出牌：须先摸牌、本回合已满3张，或手牌引用异常。"); } } catch (IllegalStateException ex) { setFeedback(ex.getMessage() != null ? ex.getMessage() : "出牌条件不满足"); } } } else { clearFeedback(); logic.endTurn(session); } } else { clearFeedback(); Player cur = session.getCurrentPlayer(); if (cur == null) return; int handSize = cur.getHand().size(); int mustDiscard = handSize - 7; if (mustDiscard <= 0) return; if (discardSelections.size() < mustDiscard) setFeedback("请至少再选 " + (mustDiscard - discardSelections.size()) + " 张弃掉（当前 " + handSize + " 张手牌，弃后须 ≤7）。"); else if (handSize - discardSelections.size() > 7) setFeedback("所选张数不够：弃掉这些后手牌仍会超过 7 张，请多选几张。"); else { logic.discardFromHandChosen(session, new ArrayList<>(discardSelections)); discardSelections.clear(); selectedHandCard = null; } } publishSessionChange(session); refreshGameplayUi(); }
    private void handleHandCardPick(Card card) { if (card == null) return; GameSession session = AppContext.get().gameEngine().getCurrentSession(); if (session == null) return; if (!isLocalPlayersTurn(session)) { setFeedback("当前轮到 " + (session.getCurrentPlayer() != null ? session.getCurrentPlayer().getName() : "其他玩家") + "，你只能查看自己的手牌。 "); refreshGameplayUi(); return; } GameState.Phase phase = session.getGameState().getPhase(); if (phase == GameState.Phase.DISCARD_PHASE) { selectedHandCard = null; if (discardSelections.contains(card)) discardSelections.remove(card); else discardSelections.add(card); clearFeedback(); refreshGameplayUi(); return; } if (phase != GameState.Phase.PLAY_PHASE) { selectedHandCard = null; if (phase == GameState.Phase.DRAW_PHASE) setFeedback("请先点「摸牌」，再进入出牌阶段选手牌。"); else setFeedback("当前阶段不可选手牌。"); refreshGameplayUi(); return; } discardSelections.clear(); selectedHandCard = selectedHandCard == card ? null : card; clearFeedback(); refreshGameplayUi(); }
    private void setFeedback(String msg) { if (feedbackLabel != null) { feedbackLabel.setText(msg == null ? "" : msg); feedbackLabel.setVisible(msg != null && !msg.isBlank()); feedbackLabel.setManaged(msg != null && !msg.isBlank()); } }
    private void clearFeedback() { setFeedback(""); }
    @FXML protected void onUndo(ActionEvent event) { }
    private void syncScaleLabel() { double s = UI_SCALE_STEPS[scaleIndex.get()]; uiScaleLabel.setText(String.format("%.2fx", s)); if (gameRoot != null) { gameRoot.setScaleX(s); gameRoot.setScaleY(s); } }
    public void refreshSessionUi() { refreshGameplayUi(); }
    protected void refreshGameplayUi() { GameSession session = AppContext.get().gameEngine().getCurrentSession(); if (session == null) discardSelections.clear(); else { GameState gs = session.getGameState(); if (gs.getPhase() == GameState.Phase.DISCARD_PHASE) { selectedHandCard = null; Player cur = session.getCurrentPlayer(); if (cur != null) discardSelections.removeIf(c -> !cur.getHand().getCards().contains(c)); } else discardSelections.clear(); } applyHud(session); if (viewCoordinator != null) viewCoordinator.refreshFromSession(session, selectedHandCard, discardSelections); updatePlayChrome(session); }
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
        topbarTitle.setText("当前回合: " + who + (myTurn ? "（你）" : "（等待）") + "  │  " + formatPhase(state.getPhase()));
        movesLabel.setText("本回合出牌: " + state.getCardsPlayedThisTurn() + " / 3");
        if (moneyHudLabel != null) moneyHudLabel.setText(MoneyHudText.forPlayer(local != null ? local : current));
        switch (state.getPhase()) {
            case DRAW_PHASE -> {
                primaryActionButton.setText(myTurn ? "摸牌" : "等待 " + who + " 摸牌");
                primaryActionButton.setDisable(!myTurn);
            }
            case PLAY_PHASE -> {
                if (feedbackLabel != null && feedbackLabel.getText() != null && feedbackLabel.getText().startsWith("提示：")) feedbackLabel.setText("");
                if (feedbackLabel != null) feedbackLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #c62828; -fx-padding: 2 0 4 0;");
                if (!myTurn) {
                    primaryActionButton.setText("等待 " + who + " 出牌");
                    primaryActionButton.setDisable(true);
                } else if (selectedHandCard == null) {
                    primaryActionButton.setDisable(false);
                    primaryActionButton.setText("结束回合");
                } else if (requiresExplicitPlayMode(selectedHandCard)) {
                    primaryActionButton.setDisable(true);
                    primaryActionButton.setText("请用上方按钮出牌");
                } else if (selectedHandCard instanceof BankCard) {
                    primaryActionButton.setDisable(false);
                    primaryActionButton.setText("打出(入银行)");
                } else {
                    primaryActionButton.setDisable(false);
                    primaryActionButton.setText("打出选中牌");
                }
            }
            case DISCARD_PHASE -> {
                int handSize = current != null ? current.getHand().size() : 0;
                int must = Math.max(0, handSize - 7);
                if (!myTurn) {
                    primaryActionButton.setText("等待 " + who + " 弃牌");
                    primaryActionButton.setDisable(true);
                } else if (must <= 0) {
                    primaryActionButton.setText("弃牌");
                    primaryActionButton.setDisable(true);
                } else {
                    primaryActionButton.setText("确认弃掉所选（至少 " + must + " 张）");
                    boolean ok = discardSelections.size() >= must && handSize - discardSelections.size() <= 7;
                    primaryActionButton.setDisable(!ok);
                }
            }
        }
    }
    private boolean isLocalPlayersTurn(GameSession session) { Player local = localPlayer(session); Player current = session != null ? session.getCurrentPlayer() : null; return local != null && current != null && local.equals(current); }
    private Player localPlayer(GameSession session) { if (session == null) return null; String localName = AppContext.get().networkLobbyState().getLocalPlayerName(); if (localName == null || localName.isBlank()) return session.getCurrentPlayer(); for (Player p : session.getPlayers()) { if (localName.equals(p.getName())) return p; } return session.getCurrentPlayer(); }
    private void publishSessionChange(GameSession session) { if (session == null) return; AppContext.get().networkLobbyState().setSession(session); String roomId = AppContext.get().networkLobbyState().getRoomId(); if (roomId == null || roomId.isBlank()) return; if (AppContext.get().networkLobbyState().isHost()) { GameServer.Room room = AppContext.get().gameServer().getRoom(roomId); if (room != null) room.broadcastSessionSnapshot(); return; } var client = AppContext.get().networkLobbyState().getClient(); if (client == null) { setFeedback("同步失败：未连接到房主。请确认从 Join Room 页面加入房间。"); return; } try { client.send(com.example.monopoly_deal_game.network.NetworkMessage.builder(com.example.monopoly_deal_game.network.NetworkMessage.Type.PLAYER_ACTION).roomId(roomId).playerName(AppContext.get().networkLobbyState().getLocalPlayerName()).session(session).build()); setFeedback("操作已提交，等待房主同步..."); } catch (Exception ex) { setFeedback("同步操作失败：" + ex.getMessage()); } }
    private static boolean requiresExplicitPlayMode(Card c) { return c instanceof ActionCard || c instanceof RentCard || c instanceof PropertyCard; }
    private void removePlayChromeLayoutListener() { if (playChromeLayoutListener != null) { actionLayer.widthProperty().removeListener(playChromeLayoutListener); actionLayer.heightProperty().removeListener(playChromeLayoutListener); playChromeLayoutListener = null; } }
    private void layoutPlayChromeDock(VBox dock) { if (dock == null || !actionLayer.getChildren().contains(dock)) return; double pw = actionLayer.getWidth(); double ph = actionLayer.getHeight(); if (pw <= 1 || ph <= 1) return; double maxW = Math.max(200, pw - 40); double dw = dock.prefWidth(maxW); double dh = dock.prefHeight(dw); dock.setMaxWidth(maxW); dock.setLayoutX(Math.max(20, (pw - Math.min(dw, maxW)) / 2)); double y = ph - PLAY_CHROME_BOTTOM_INSET - dh; dock.setLayoutY(Math.max(40, y)); }
    private void updatePlayChrome(GameSession session) {
        removePlayChromeLayoutListener();
        actionLayer.getChildren().clear();
        if (session == null || selectedHandCard == null) { actionLayer.setMouseTransparent(true); actionLayer.setPickOnBounds(false); return; }
        GameState st = session.getGameState();
        if (st.isGameOver() || st.getPhase() != GameState.Phase.PLAY_PHASE || !requiresExplicitPlayMode(selectedHandCard)) { actionLayer.setMouseTransparent(true); actionLayer.setPickOnBounds(false); return; }
        actionLayer.setMouseTransparent(false);
        actionLayer.setPickOnBounds(false);
        GameLogic logic = AppContext.get().gameEngine().getGameLogic();
        var me = session.getCurrentPlayer();
        if (me == null) { actionLayer.setMouseTransparent(true); actionLayer.setPickOnBounds(false); return; }
        VBox dock = new VBox(10);
        dock.setAlignment(Pos.CENTER);
        dock.setPadding(new Insets(10, 16, 10, 16));
        dock.setPickOnBounds(true);
        dock.setStyle("-fx-background-color: rgba(25,118,210,0.94); -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 2);");
        Label bar = new Label("出牌选项（可点击其他手牌取消或重选）");
        bar.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 0 0 2 0;");
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        Card sel = selectedHandCard;
        if (sel instanceof RentCard rc) {
            Button toBank = new Button("存入银行（现金 " + Math.max(0, rc.getValue()) + "M）");
            toBank.setStyle("-fx-font-size:12px;");
            toBank.setOnAction(e -> tryPlayWithOptions(session, CardPlayOptions.bankOnly()));
            row.getChildren().add(toBank);
            java.util.List<CardColor> colors = RentRules.eligibleChargeColors(rc, me);
            FlowPane fp = new FlowPane();
            fp.setAlignment(Pos.CENTER);
            fp.setHgap(8);
            fp.setVgap(8);
            if (colors.isEmpty()) {
                Label empty = new Label("当前没有可收租的颜色");
                empty.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                fp.getChildren().add(empty);
            } else {
                boolean dbl = st.isDoubleNextRent();
                for (CardColor col : colors) {
                    int base = RentCalculator.rentOnColor(me, col);
                    int display = dbl ? base * 2 : base;
                    Button cb = new Button("收租 " + rentColorShortLabel(col) + " → " + display + "M");
                    cb.setStyle("-fx-font-size:11px;");
                    CardColor cap = col;
                    boolean rentBtnOk = RentRules.canUseRentEffectForUi(rc, me, cap, session);
                    cb.setDisable(!rentBtnOk);
                    cb.setOnAction(e -> tryPlayWithOptions(session, CardPlayOptions.rentWithColor(cap)));
                    fp.getChildren().add(cb);
                }
            }
            dock.getChildren().addAll(bar, row, fp);
        } else if (sel instanceof PropertyCard pc) {
            Button toTable = new Button("摆上桌面（当前: " + rentColorShortLabel(pc.getCurrentColor()) + "）");
            toTable.setStyle("-fx-font-size:12px;");
            toTable.setOnAction(e -> tryPlayWithOptions(session, CardPlayOptions.auto()));
            Button toBank = new Button("存入银行（现金 " + Math.max(0, sel.getValue()) + "M）");
            toBank.setStyle("-fx-font-size:12px;");
            toBank.setOnAction(e -> tryPlayWithOptions(session, CardPlayOptions.bankOnly()));
            row.getChildren().addAll(toTable, toBank);
            dock.getChildren().addAll(bar, row);
            if (pc.canFlipWildDualColor()) {
                FlowPane colorChoices = new FlowPane();
                colorChoices.setAlignment(Pos.CENTER);
                colorChoices.setHgap(8);
                colorChoices.setVgap(8);
                for (CardColor color : pc.getSelectableColors()) {
                    Button choose = new Button("作为" + rentColorShortLabel(color));
                    choose.setDisable(color == pc.getCurrentColor());
                    choose.setStyle("-fx-font-size:12px;");
                    choose.setOnAction(e -> { pc.alignToDeclaredColor(color); refreshGameplayUi(); });
                    colorChoices.getChildren().add(choose);
                }
                dock.getChildren().add(colorChoices);
            }
        } else if (sel instanceof ActionCard ac) {
            Button use = new Button("使用卡牌效果");
            use.setStyle("-fx-font-size:12px;");
            boolean can = logic.getEffectExecutor().canUseActionEffectForUi(ac, session);
            use.setDisable(!can);
            if (!can) use.setTooltip(new Tooltip("当前没有可生效的对手或前提不满足；可把牌作现金存入银行。"));
            use.setOnAction(e -> tryPlayWithOptions(session, CardPlayOptions.auto()));
            Button toBank = new Button("存入银行（现金 " + Math.max(0, ac.getValue()) + "M）");
            toBank.setStyle("-fx-font-size:12px;");
            toBank.setOnAction(e -> tryPlayWithOptions(session, CardPlayOptions.bankOnly()));
            row.getChildren().addAll(use, toBank);
            dock.getChildren().addAll(bar, row);
        }
        actionLayer.getChildren().add(dock);
        playChromeLayoutListener = obs -> layoutPlayChromeDock(dock);
        actionLayer.widthProperty().addListener(playChromeLayoutListener);
        actionLayer.heightProperty().addListener(playChromeLayoutListener);
        layoutPlayChromeDock(dock);
    }
    private static String rentColorShortLabel(CardColor c) { if (c == null) return "?"; return switch (c) { case BROWN -> "棕"; case LIGHT_BLUE -> "浅蓝"; case PURPLE -> "紫"; case ORANGE -> "橙"; case RED -> "红"; case YELLOW -> "黄"; case GREEN -> "绿"; case BLUE -> "深蓝"; case RAILROAD -> "铁路"; case UTILITY -> "公共"; case WILD, NONE -> "万能"; }; }
    private CardPlayOptions mergePlayTargets(GameSession session, Card card, CardPlayOptions options) { if (session == null || options == null) return options; Player me = session.getCurrentPlayer(); if (me == null) return options; if (card instanceof ActionCard ac && !options.asBankMoney()) { if (!PlayEligibility.needsChosenOpponent(ac)) return options; List<Player> elig = PlayEligibility.eligibleOpponentsForAction(me, session, ac.getActionType()); if (elig.isEmpty()) return options; if (elig.size() == 1) return options.withActionTarget(elig.get(0)); if (options.actionTargetPlayer() != null && elig.contains(options.actionTargetPlayer())) return options; return ActionTargetDialogs.chooseOpponent(stage, "选择对手", "请选择「" + ac.getName() + "」对哪一位对手生效。\n多名玩家时必须由你指定结算对象。", elig).map(options::withActionTarget).orElse(null); } if (card instanceof RentCard rc && !options.asBankMoney()) { List<Player> renters = RentRules.rentersExcludingLandlord(me, session); if (renters.isEmpty()) return options; if (!RentRules.canUseRentEffect(rc, me, options.rentColorChoice())) return options; CardColor cap = options.rentColorChoice(); if (renters.size() == 1) return CardPlayOptions.rentWithColorAndPlayer(cap, renters.get(0)); if (options.actionTargetPlayer() != null && renters.contains(options.actionTargetPlayer())) return CardPlayOptions.rentWithColorAndPlayer(cap, options.actionTargetPlayer()); boolean dbl = session.getGameState().isDoubleNextRent(); int preview = cap != null ? (dbl ? RentCalculator.rentOnColor(me, cap) * 2 : RentCalculator.rentOnColor(me, cap)) : (rc.isWildRent() ? RentCalculator.bestRentWild(me, dbl) : RentCalculator.bestRentForLandlord(me, rc.getApplicableColors(), dbl)); return ActionTargetDialogs.chooseOpponent(stage, "收取租金", "请选择向哪位玩家收取这条租金（预览约 " + preview + "M）。", renters).map(p -> CardPlayOptions.rentWithColorAndPlayer(cap, p)).orElse(null); } return options; }
    private void tryPlayWithOptions(GameSession session, CardPlayOptions options) { if (!isLocalPlayersTurn(session)) { setFeedback("当前轮到 " + (session != null && session.getCurrentPlayer() != null ? session.getCurrentPlayer().getName() : "其他玩家") + "，请等待。"); refreshGameplayUi(); return; } GameLogic logic = AppContext.get().gameEngine().getGameLogic(); Card card = selectedHandCard; if (card == null || session == null) return; try { CardPlayOptions merged = mergePlayTargets(session, card, options); if (merged == null) { setFeedback("已取消选择目标玩家。"); refreshGameplayUi(); return; } boolean ok = logic.playCard(session, card, merged); if (ok) { clearFeedback(); selectedHandCard = null; if (logic.checkGameOver(session)) session.getGameState().setGameOver(true); publishSessionChange(session); } else { setFeedback("无法这样打出，请检查手序、次数或前提条件。"); } } catch (IllegalStateException ex) { setFeedback(ex.getMessage() != null ? ex.getMessage() : "出牌条件不满足"); } refreshGameplayUi(); }
    private static String formatPhase(GameState.Phase phase) { if (phase == null) return "—"; return switch (phase) { case DRAW_PHASE -> "阶段: 摸牌"; case PLAY_PHASE -> "阶段: 出牌"; case DISCARD_PHASE -> "阶段: 弃牌"; }; }
}
