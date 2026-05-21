package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.view.CardView;
import com.example.monopoly_deal_game.view.GameplayUiBundle;
import com.example.monopoly_deal_game.view.HandCardKit;
import com.example.monopoly_deal_game.view.TableCardKit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GameplayViewCoordinator {

    private final GameplayUiBundle zones;
    private Consumer<Card> onHandCardPick = c -> {};

    public GameplayViewCoordinator(GameplayUiBundle zones) {
        this.zones = zones;
    }

    public void setOnHandCardPick(Consumer<Card> handler) {
        this.onHandCardPick = handler != null ? handler : c -> {};
    }

    public GameplayUiBundle zones() {
        return zones;
    }

    public void refreshFromSession(GameSession session, Card selectedInHand, Collection<Card> discardSelections) {
        zones.handPane().getChildren().clear();
        zones.deckPane().getChildren().clear();
        zones.discardPane().getChildren().clear();
        zones.opponentsPane().getChildren().clear();
        zones.selfBoardPane().getChildren().clear();

        if (session == null) {
            return;
        }

        zones.deckPane().getChildren().add(new Label("抽牌堆: " + session.getDrawPile().size()));
        zones.discardPane().getChildren().add(new Label("弃牌堆: " + session.getDiscardPile().size()));

        refreshTableZones(session);
        renderHand(session, selectedInHand, discardSelections);
    }

    private void renderHand(GameSession session, Card selectedInHand, Collection<Card> discardSelections) {
        Player cp = session.getCurrentPlayer();
        if (cp == null) return;
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 6, 12, 6));
        boolean inDiscard = session.getGameState().getPhase() == GameState.Phase.DISCARD_PHASE;
        Collection<Card> disc = discardSelections != null ? discardSelections : List.of();
        List<Card> handCards = cp.getHand().getCards();
        for (int i = 0; i < handCards.size(); i++) {
            Card card = handCards.get(i);
            boolean sel = inDiscard ? disc.contains(card) : Objects.equals(card, selectedInHand);
            CardView view = HandCardKit.createHandCard(card, sel, () -> onHandCardPick.accept(card));
            StackPane wrap = new StackPane(view);
            wrap.setPrefSize(TableCardKit.TABLE_CARD_BASE_W, TableCardKit.TABLE_CARD_BASE_H);
            if (sel) {
                wrap.setScaleX(1.1);
                wrap.setScaleY(1.1);
            }
            HandCardKit.applyFanPose(wrap, i, handCards.size());
            row.getChildren().add(wrap);
        }
        if (row.getChildren().isEmpty()) row.getChildren().add(new Label("手牌为空"));
        ScrollPane scroll = new ScrollPane(row);
        scroll.setFitToHeight(true);
        scroll.setFocusTraversable(false);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        zones.handPane().getChildren().add(scroll);
    }

    private void refreshTableZones(GameSession session) {
        Player current = session.getCurrentPlayer();
        if (current == null) return;
        int playerCount = session.getPlayers().size();
        double opponentPaneWidth = zones.opponentsPane().getWidth();
        double sideWidth = Math.max(180, opponentPaneWidth / Math.max(1, playerCount - 1));
        HBox opponentsRow = new HBox(10);
        opponentsRow.setAlignment(Pos.TOP_LEFT);
        opponentsRow.setPadding(new Insets(6, 6, 6, 6));
        for (Player p : session.getPlayers()) {
            if (!p.equals(current)) {
                VBox panel = buildPlayerBoardPanel(p, true, sideWidth);
                panel.setMinWidth(sideWidth);
                panel.setPrefWidth(sideWidth);
                panel.setMaxWidth(sideWidth);
                opponentsRow.getChildren().add(panel);
            }
        }
        if (opponentsRow.getChildren().isEmpty()) opponentsRow.getChildren().add(new Label("无其他玩家"));
        zones.opponentsPane().getChildren().add(wrapHorizontalScroll(opponentsRow));

        VBox selfVBox = new VBox(8);
        selfVBox.setPadding(new Insets(6, 4, 6, 4));
        selfVBox.getChildren().add(buildPlayerBoardPanel(current, false, Math.max(280, zones.selfBoardPane().getWidth() - 20)));
        zones.selfBoardPane().getChildren().add(wrapVerticalScroll(selfVBox));
    }

    private VBox buildPlayerBoardPanel(Player p, boolean isOpponent, double targetWidth) {
        VBox board = new VBox(8);
        board.setPadding(new Insets(2, 2, 4, 2));
        board.setPrefWidth(targetWidth);
        Label header = new Label(p.getName() + (isOpponent ? " | 手牌 " + p.getHand().size() : "（你）") + " | 物业组 " + p.getProperties().size() + " | 成套 " + p.getFullSetCount() + " | 银行 " + sumBank(p) + "M");
        header.setWrapText(true);
        board.getChildren().add(header);

        HBox split = new HBox(10);
        split.setAlignment(Pos.TOP_LEFT);

        VBox propOuter = new VBox(6);
        propOuter.setPrefWidth(Math.max(140, targetWidth * 0.62));
        propOuter.getChildren().add(new Label("房产区"));
        FlowPane propFlow = new FlowPane();
        propFlow.setHgap(8);
        propFlow.setVgap(8);
        propFlow.setPrefWrapLength(Math.max(120, targetWidth * 0.58));
        if (p.getProperties().isEmpty()) {
            propFlow.getChildren().add(new Label("—"));
        } else {
            for (Property group : p.getProperties()) {
                VBox col = new VBox(6);
                col.setPadding(new Insets(4));
                col.setStyle("-fx-background-color: rgba(106,27,154,0.06); -fx-background-radius: 8; -fx-border-color: rgba(106,27,154,0.25); -fx-border-radius: 8;");
                Label colLabel = new Label(shortColorLabel(group.getEffectiveColor()) + " " + group.getCards().size() + "/" + (group.getCards().isEmpty() ? "?" : group.getCards().get(0).getFullSetThreshold()) + (group.isMonopoly() ? " · 垄断" : ""));
                colLabel.setWrapText(true);
                col.getChildren().add(colLabel);
                FlowPane cardFlow = new FlowPane();
                cardFlow.setHgap(6);
                cardFlow.setVgap(6);
                for (PropertyCard pc : group.getCards()) cardFlow.getChildren().add(TableCardKit.createReadOnlyCard(pc, 0.62));
                for (Card b : group.getBuildingCards()) cardFlow.getChildren().add(TableCardKit.createReadOnlyCard(b, 0.58));
                col.getChildren().add(cardFlow);
                propFlow.getChildren().add(col);
            }
        }
        propOuter.getChildren().add(propFlow);
        split.getChildren().add(wrapVerticalScroll(propOuter));

        VBox bankCol = new VBox(6);
        bankCol.setPrefWidth(Math.max(110, targetWidth * 0.34));
        bankCol.getChildren().add(new Label("资产（银行）"));
        FlowPane bankFlow = new FlowPane();
        bankFlow.setHgap(7);
        bankFlow.setVgap(7);
        bankFlow.setPrefWrapLength(Math.max(100, targetWidth * 0.3));
        for (Card c : p.getBank().getCards()) bankFlow.getChildren().add(TableCardKit.createReadOnlyCard(c, 0.55));
        if (bankFlow.getChildren().isEmpty()) bankFlow.getChildren().add(new Label("（空）"));
        bankCol.getChildren().add(new ScrollPane(bankFlow));
        bankCol.getChildren().add(new Label("合计 " + sumBank(p) + "M"));
        split.getChildren().add(bankCol);

        board.getChildren().add(split);
        return board;
    }

    private static String shortColorLabel(CardColor c) {
        if (c == null) return "?";
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
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFocusTraversable(false);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    private static ScrollPane wrapHorizontalScroll(HBox content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToHeight(true);
        scroll.setFocusTraversable(false);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    private static int sumBank(Player p) {
        int s = 0;
        for (Card c : p.getBank().getCards()) s += Math.max(0, c.getValue());
        return s;
    }
}
