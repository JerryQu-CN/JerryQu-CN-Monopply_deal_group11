package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.view.animation.MotionContext;
import com.example.monopoly_deal_game.view.scene.ScenePaneResolver;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 将 {@link GameplayUiBundle} 与对局 {@link GameSession} 绑定；由 Controller 在逻辑变更后调用刷新。
 */
public class GameplayViewCoordinator {

    private final GameplayUiBundle zones;
    private final ScenePaneResolver sceneResolver;
    private Consumer<Card> onHandCardPick = c -> {};

    public GameplayViewCoordinator(GameplayUiBundle zones) {
        this.zones = zones;
        this.sceneResolver = new ScenePaneResolver(zones);
    }

    /** 玩家在手牌区点击某张领域牌时回调（用于选中 / 准备打出）。 */
    public void setOnHandCardPick(Consumer<Card> handler) {
        this.onHandCardPick = handler != null ? handler : c -> {};
    }

    public GameplayUiBundle zones() {
        return zones;
    }

    public ScenePaneResolver sceneResolver() {
        return sceneResolver;
    }

    public MotionContext motionContext() {
        return MotionContext.forTable(
                zones.deckPane(), zones.handPane(), zones.discardPane(), zones.actionLayer());
    }

    /**
     * 根据会话刷新牌堆区与手牌区；手牌使用 {@link CardView} 显示卡图。
     *
     * @param selectedInHand 出牌阶段：当前选中的单张手牌；弃牌阶段忽略（由 discardSelections 决定高亮）
     * @param discardSelections 弃牌阶段：待弃掉的多选手牌（引用须属于当前手牌）
     */
    public void refreshFromSession(
            GameSession session, Card selectedInHand, Collection<Card> discardSelections) {
        Pane handPane = zones.handPane();
        handPane.getChildren().clear();
        zones.deckPane().getChildren().clear();
        zones.discardPane().getChildren().clear();

        if (session == null) {
            return;
        }

        Label deckLbl = new Label("抽牌堆: " + session.getDrawPile().size());
        zones.deckPane().getChildren().add(deckLbl);

        Label discLbl = new Label("弃牌堆: " + session.getDiscardPile().size());
        zones.discardPane().getChildren().add(discLbl);

        refreshTableZones(session);

        Player cp = localPlayer(session);
        if (cp == null) {
            cp = session.getCurrentPlayer();
        }
        if (cp == null) {
            return;
        }

        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 6, 12, 6));

        GameState.Phase phase = session.getGameState().getPhase();
        boolean inDiscard = phase == GameState.Phase.DISCARD_PHASE;
        Collection<Card> disc = discardSelections != null ? discardSelections : List.of();

        List<Card> handCards = cp.getHand().getCards();
        int n = handCards.size();
        for (int i = 0; i < n; i++) {
            Card card = handCards.get(i);
            boolean sel =
                    inDiscard ? disc.contains(card) : Objects.equals(card, selectedInHand);
            CardView view = HandCardKit.createHandCard(card, sel, () -> onHandCardPick.accept(card));
            StackPane wrap = new StackPane(view);
            wrap.setPrefSize(TableCardKit.TABLE_CARD_BASE_W, TableCardKit.TABLE_CARD_BASE_H);
            if (sel) {
                wrap.setScaleX(1.1);
                wrap.setScaleY(1.1);
            }
            HandCardKit.applyFanPose(wrap, i, n);
            row.getChildren().add(wrap);
        }

        if (row.getChildren().isEmpty()) {
            row.getChildren().add(new Label("手牌为空（牌库或尚未实现发牌）"));
        }

        ScrollPane scroll = new ScrollPane(row);
        scroll.setFitToHeight(true);
        scroll.setFocusTraversable(false);
        scroll.setPannable(false);
        row.setFocusTraversable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:transparent;");
        handPane.getChildren().add(scroll);
    }

    private void refreshTableZones(GameSession session) {
        if (zones.opponentsPane() != null) zones.opponentsPane().getChildren().clear();
        if (zones.selfBoardPane() != null) zones.selfBoardPane().getChildren().clear();
        Player current = localPlayer(session);
        if (current == null) {
            current = session.getCurrentPlayer();
        }
        if (current == null || zones.opponentsPane() == null || zones.selfBoardPane() == null) return;

        List<Player> players = session.getPlayers();
        VBox tableRows = new VBox(8);
        tableRows.setPadding(new Insets(8, 8, 8, 8));
        int playerCount = Math.max(1, players.size());
        for (Player p : players) {
            VBox row = buildPlayerBoardPanel(p, !p.equals(current));
            row.setMaxWidth(Double.MAX_VALUE);
            row.prefHeightProperty().bind(zones.opponentsPane().heightProperty().subtract(16 + (playerCount - 1) * 8.0).divide(playerCount));
            row.minHeightProperty().bind(row.prefHeightProperty());
            row.maxHeightProperty().bind(row.prefHeightProperty());
            tableRows.getChildren().add(row);
        }
        ScrollPane tableScroll = wrapVerticalScroll(tableRows);
        tableScroll.setFitToWidth(true);
        tableScroll.setVbarPolicy(players.size() > 5 ? ScrollPane.ScrollBarPolicy.AS_NEEDED : ScrollPane.ScrollBarPolicy.NEVER);
        zones.opponentsPane().getChildren().add(tableScroll);
        tableScroll.prefWidthProperty().bind(zones.opponentsPane().widthProperty());
        tableScroll.prefHeightProperty().bind(zones.opponentsPane().heightProperty());

        zones.selfBoardPane().setVisible(false);
        zones.selfBoardPane().setManaged(false);
    }

    private static final double PROP_SCALE_OPP = 0.58;
    private static final double BANK_SCALE_OPP = 0.52;
    private static final double PROP_SCALE_SELF = 0.80;
    private static final double BANK_SCALE_SELF = 0.66;

    /** 统一的玩家牌桌面板：顶部信息 + 左（房产列）/ 右（银行流式铺排）。 */
    private VBox buildPlayerBoardPanel(Player p, boolean isOpponent) {
        int players = Math.max(1, com.example.monopoly_deal_game.controller.AppContext.get().gameEngine().getCurrentSession() != null ? com.example.monopoly_deal_game.controller.AppContext.get().gameEngine().getCurrentSession().getPlayers().size() : 2);
        double propScale = players >= 5 ? 0.44 : players == 4 ? 0.50 : players == 3 ? 0.56 : 0.62;
        double bankScale = players >= 5 ? 0.40 : players == 4 ? 0.45 : players == 3 ? 0.50 : 0.56;
        double cardW = TableCardKit.TABLE_CARD_BASE_W * propScale;

        VBox board = new VBox(5);
        board.setPadding(new Insets(4, 8, 5, 8));
        board.setStyle("-fx-background-color: rgba(255,255,255,0.42); -fx-background-radius: 10; -fx-border-color: rgba(76,175,80,0.35); -fx-border-radius: 10;");

        String head =
                p.getName()
                        + (isOpponent ? "  |  手牌 " + p.getHand().size() + " 张" : "（你）")
                        + "  |  物业组 "
                        + p.getProperties().size()
                        + "  成套 "
                        + p.getFullSetCount()
                        + "  银行 "
                        + sumBank(p)
                        + "M";
        Label header = new Label(head);
        header.setStyle(
                "-fx-font-weight:bold; -fx-font-size:" + (isOpponent ? "11px" : "13px") + ";");
        board.getChildren().add(header);

        HBox split = new HBox(isOpponent ? 10 : 14);
        split.setAlignment(Pos.TOP_LEFT);

        // ── Left: property columns ─────────────────────────────────────────────
        VBox propOuter = new VBox(6);
        Label propTitle = new Label("房产区");
        propTitle.setStyle(
                "-fx-font-weight:bold; -fx-font-size:"
                        + (isOpponent ? "11px" : "12px")
                        + "; -fx-text-fill:#4a148c;");
        propOuter.getChildren().add(propTitle);

        HBox propColumns = new HBox(12);
        propColumns.setAlignment(Pos.TOP_LEFT);
        if (p.getProperties().isEmpty()) {
            propColumns.getChildren().add(new Label("—"));
        } else {
            for (Property group : p.getProperties()) {
                VBox col = new VBox(6);
                col.setAlignment(Pos.TOP_LEFT);
                col.setPadding(new Insets(4, 6, 4, 6));
                col.setStyle(
                        "-fx-background-color: rgba(106,27,154,0.06); "
                                + "-fx-background-radius: 8; -fx-border-color: rgba(106,27,154,0.25); "
                                + "-fx-border-radius: 8;");

                String colName =
                        isOpponent
                                ? shortColorLabel(group.getEffectiveColor())
                                : shortColorLabel(group.getEffectiveColor())
                                        + "  "
                                        + group.getCards().size()
                                        + "/"
                                        + (group.getCards().isEmpty()
                                                ? "?"
                                                : String.valueOf(
                                                        group.getCards()
                                                                .get(0)
                                                                .getFullSetThreshold()))
                                        + (group.isMonopoly() ? " · 垄断" : "");
                Label colLabel = new Label(colName);
                colLabel.setWrapText(true);
                colLabel.setMaxWidth(cardW * 2.6 + 24);
                colLabel.setStyle(
                        "-fx-font-size:"
                                + (isOpponent ? "10px" : "11px")
                                + "; -fx-font-weight:bold; -fx-text-fill:#4a148c;");
                col.getChildren().add(colLabel);

                FlowPane cardFlow = new FlowPane();
                cardFlow.setHgap(6);
                cardFlow.setVgap(6);
                cardFlow.setPrefWrapLength(Math.max(160, cardW * 2.35 + 18));

                for (PropertyCard pc : group.getCards()) {
                    cardFlow.getChildren().add(TableCardKit.createReadOnlyCard(pc, propScale));
                }
                double bScale = propScale * 0.92;
                for (Card b : group.getBuildingCards()) {
                    cardFlow.getChildren().add(TableCardKit.createReadOnlyCard(b, bScale));
                }
                col.getChildren().add(cardFlow);
                propColumns.getChildren().add(col);
            }
        }
        propOuter.getChildren().add(propColumns);

        ScrollPane propScroll = wrapVerticalScroll(propOuter);
        propScroll.setFitToWidth(true);
        propScroll.setMinWidth(200);
        HBox.setHgrow(propScroll, Priority.ALWAYS);
        split.getChildren().add(propScroll);

        // ── Right: bank flow ───────────────────────────────────────────────────
        VBox bankCol = new VBox(6);
        bankCol.setAlignment(Pos.TOP_LEFT);
        bankCol.setMinWidth(isOpponent ? 120 : 200);
        bankCol.setMaxWidth(isOpponent ? 280 : 400);
        bankCol.setPadding(new Insets(0, 0, 0, 6));

        Label bankTitle = new Label("资产（银行）");
        bankTitle.setStyle(
                "-fx-font-weight:bold; -fx-font-size:"
                        + (isOpponent ? "11px" : "12px")
                        + "; -fx-text-fill:#1b5e20;");
        bankCol.getChildren().add(bankTitle);

        FlowPane bankFlow = new FlowPane();
        bankFlow.setFocusTraversable(false);
        bankFlow.setHgap(7);
        bankFlow.setVgap(7);
        bankFlow.setAlignment(Pos.TOP_LEFT);
        bankFlow.setPrefWrapLength(TableCardKit.TABLE_CARD_BASE_W * bankScale * 3.25 + 24);

        for (Card c : p.getBank().getCards()) {
            bankFlow.getChildren().add(TableCardKit.createReadOnlyCard(c, bankScale));
        }
        if (bankFlow.getChildren().isEmpty()) {
            Label emptyBank = new Label("（空）");
            emptyBank.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
            bankFlow.getChildren().add(emptyBank);
        }

        ScrollPane bankScroll = new ScrollPane(bankFlow);
        bankScroll.setFitToWidth(true);
        bankScroll.setPrefViewportHeight(72);
        // JavaFX ScrollPane 无 setMaxViewportHeight；用 Region.maxHeight 限制可视区整体高度
        bankScroll.setMinHeight(54);
        bankScroll.setMaxHeight(92);
        bankScroll.setFocusTraversable(false);
        bankScroll.setPannable(false);
        bankScroll.setStyle("-fx-background: transparent;");

        bankCol.getChildren().add(bankScroll);

        Label bankTotal = new Label("合计 " + sumBank(p) + "M");
        bankTotal.setStyle(
                "-fx-font-size:"
                        + (isOpponent ? "10px" : "12px")
                        + "; -fx-font-weight:bold; -fx-text-fill:#1b5e20;");
        bankCol.getChildren().add(bankTotal);

        split.getChildren().add(bankCol);
        HBox.setHgrow(propScroll, Priority.ALWAYS);
        HBox.setHgrow(bankCol, Priority.NEVER);
        board.getChildren().add(split);
        return board;
    }

    private static Player localPlayer(GameSession session) {
        if (session == null) {
            return null;
        }
        String localName = com.example.monopoly_deal_game.controller.AppContext.get().networkLobbyState().getLocalPlayerName();
        if (localName == null || localName.isBlank()) {
            return null;
        }
        for (Player player : session.getPlayers()) {
            if (localName.equals(player.getName())) {
                return player;
            }
        }
        return null;
    }

    private static String shortColorLabel(CardColor c) {
        if (c == null) {
            return "?";
        }
        return switch (c) {
            case BROWN -> "棕";
            case LIGHT_BLUE -> "浅蓝";
            case PURPLE -> "紫";
            case ORANGE -> "橙";
            case RED -> "红";
            case YELLOW -> "黄";
            case GREEN -> "绿";
            case BLUE -> "深蓝";
            case RAILROAD -> "铁路";
            case UTILITY -> "公共";
            case WILD -> "混色";
            case NONE -> "—";
        };
    }

    private static ScrollPane wrapVerticalScroll(VBox content) {
        content.setFocusTraversable(false);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFocusTraversable(false);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background:transparent;");
        return scroll;
    }

    private static int sumBank(Player p) {
        int s = 0;
        for (Card c : p.getBank().getCards()) {
            s += Math.max(0, c.getValue());
        }
        return s;
    }
}
