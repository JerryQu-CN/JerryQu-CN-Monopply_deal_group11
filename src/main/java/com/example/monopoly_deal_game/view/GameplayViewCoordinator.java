package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.logic.CardColorLabel;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.view.animation.MotionContext;
import com.example.monopoly_deal_game.view.ScenePaneResolver;
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
 * Binds {@link GameplayUiBundle} to the game {@link GameSession}; called by Controller to refresh after logic changes.
 */
public class GameplayViewCoordinator {

    private final GameplayUiBundle zones;
    private final ScenePaneResolver sceneResolver;
    private Consumer<Card> onHandCardPick = c -> {};
    private Consumer<PropertyCard> onTableWildCardClick = c -> {};
    private String localPlayerName;

    public GameplayViewCoordinator(GameplayUiBundle zones) {
        this.zones = zones;
        this.sceneResolver = new ScenePaneResolver(zones);
    }

    public void setLocalPlayerName(String name) {
        this.localPlayerName = name;
    }

    /** Callback when a player clicks a card in the hand area (for selection / preparing to play). */
    public void setOnHandCardPick(Consumer<Card> handler) {
        this.onHandCardPick = handler != null ? handler : c -> {};
    }

    /** Callback when the local player clicks a wild property card on their own table to switch its color. */
    public void setOnTableWildCardClick(Consumer<PropertyCard> handler) {
        this.onTableWildCardClick = handler != null ? handler : c -> {};
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
     * Refreshes the deck and hand areas based on the session; hand cards use {@link CardView} to display card images.
     *
     * @param selectedInHand play phase: the currently selected single hand card; ignored during discard phase (highlighting determined by discardSelections)
     * @param discardSelections discard phase: multiple hand cards to be discarded (references must belong to the current hand)
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

        Label deckLbl = new Label("Draw pile: " + session.getDrawPile().size());
        zones.deckPane().getChildren().add(deckLbl);

        Label discLbl = new Label("Discard pile: " + session.getDiscardPile().size());
        zones.discardPane().getChildren().add(discLbl);

        refreshTableZones(session);

        String localName = this.localPlayerName;
        Player cp = session.localPlayer(localName);
        if (cp == null) {
            cp = session.getCurrentPlayer();
        }
        if (cp == null) {
            return;
        }

        buildHandFanContent(handPane, cp, session, selectedInHand, discardSelections);
    }

    private void buildHandFanContent(Pane handPane, Player cp, GameSession session,
                                      Card selectedInHand, Collection<Card> discardSelections) {
        GameState.Phase phase = session.getGameState().getPhase();
        boolean inDiscard = phase == GameState.Phase.DISCARD_PHASE;
        Collection<Card> disc = discardSelections != null ? discardSelections : List.of();

        List<Card> handCards = cp.getHand().getCards();
        int n = handCards.size();

        double handWidth = handPane.getWidth();
        if (handWidth <= 0) handWidth = handPane.getPrefWidth() > 0 ? handPane.getPrefWidth() : 1100;
        double cardW = TableCardKit.TABLE_CARD_BASE_W;

        double preferredExposure = 55;
        double minExposure = 35;
        double sidePad = 12;
        double avail = handWidth - sidePad * 2;

        double neededPref = n <= 1 ? cardW : cardW + (n - 1) * preferredExposure;
        double neededMin = n <= 1 ? cardW : cardW + (n - 1) * minExposure;

        double exposure;
        if (neededPref <= avail) {
            exposure = preferredExposure;
        } else if (neededMin <= avail) {
            exposure = Math.max(minExposure, (avail - cardW) / Math.max(1, n - 1));
        } else {
            exposure = minExposure;
        }

        double totalSpread = n <= 1 ? cardW : cardW + (n - 1) * exposure;
        boolean needsScroll = totalSpread > avail;
        double startX = needsScroll ? sidePad : (avail - totalSpread) / 2.0 + sidePad;

        Pane handContent = new Pane();
        handContent.setFocusTraversable(false);

        for (int i = 0; i < n; i++) {
            Card card = handCards.get(i);
            boolean sel = inDiscard ? disc.contains(card) : Objects.equals(card, selectedInHand);
            CardView view = HandCardKit.createHandCard(card, sel, () -> onHandCardPick.accept(card));
            StackPane wrap = new StackPane(view);
            wrap.setPrefSize(cardW, TableCardKit.TABLE_CARD_BASE_H);
            wrap.setFocusTraversable(false);
            if (sel) {
                wrap.setScaleX(1.1);
                wrap.setScaleY(1.1);
            }
            double x = startX + i * exposure;
            wrap.setLayoutX(x);
            wrap.setLayoutY(6);
            HandCardKit.applyFanPose(wrap, i, n);
            handContent.getChildren().add(wrap);
        }

        if (handContent.getChildren().isEmpty()) {
            Label empty = new Label("Hand is empty");
            empty.setStyle("-fx-text-fill:#8D6E63; -fx-font-size:13px;");
            empty.setLayoutX(sidePad);
            empty.setLayoutY(80);
            handContent.getChildren().add(empty);
        }

        if (needsScroll) {
            handContent.setPrefWidth(totalSpread + sidePad * 2);
            ScrollPane scroll = new ScrollPane(handContent);
            scroll.setFitToHeight(true);
            scroll.setFocusTraversable(false);
            scroll.setPannable(false);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.getStyleClass().add("scroll-pane-transparent");
            handPane.getChildren().add(scroll);
        } else {
            handContent.prefWidthProperty().bind(handPane.widthProperty());
            handPane.getChildren().add(handContent);
        }
    }

    private void refreshTableZones(GameSession session) {
        if (zones.opponentsPane() != null) zones.opponentsPane().getChildren().clear();
        if (zones.selfBoardPane() != null) zones.selfBoardPane().getChildren().clear();
        String localName = this.localPlayerName;
        Player current = session.localPlayer(localName);
        if (current == null) {
            current = session.getCurrentPlayer();
        }
        if (current == null || zones.opponentsPane() == null || zones.selfBoardPane() == null) return;

        List<Player> players = session.getPlayers();
        VBox tableRows = new VBox(8);
        tableRows.setPadding(new Insets(8, 8, 8, 8));
        int playerCount = Math.max(1, players.size());
        for (Player p : players) {
            VBox row = buildPlayerBoardPanel(p, !p.equals(current), playerCount);
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

    /** Unified player board panel: top info + left (property columns) / right (bank flow layout). */
    private VBox buildPlayerBoardPanel(Player p, boolean isOpponent, int players) {
        double propScale = players >= 5 ? 0.44 : players == 4 ? 0.50 : players == 3 ? 0.56 : 0.62;
        double bankScale = players >= 5 ? 0.40 : players == 4 ? 0.45 : players == 3 ? 0.50 : 0.56;
        double cardW = TableCardKit.TABLE_CARD_BASE_W * propScale;

        VBox board = new VBox(5);
        board.setPadding(new Insets(4, 8, 5, 8));
        board.getStyleClass().add("player-board");
        board.getChildren().add(buildPlayerHeader(p, isOpponent));

        HBox split = new HBox(isOpponent ? 10 : 14);
        split.setAlignment(Pos.TOP_LEFT);
        split.getChildren().add(buildPropertySection(p, isOpponent, propScale, cardW));
        split.getChildren().add(buildBankSection(p, isOpponent, bankScale));
        HBox.setHgrow(split.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(split.getChildren().get(1), Priority.NEVER);
        board.getChildren().add(split);
        return board;
    }

    private Label buildPlayerHeader(Player p, boolean isOpponent) {
        String head = p.getName()
                + (isOpponent ? "  |  Hand " + p.getHand().size() + " cards" : " (You)")
                + "  |  Property sets " + p.getProperties().size()
                + "  full sets " + p.getFullSetCount()
                + "  bank " + sumBank(p) + "M";
        Label header = new Label(head);
        header.getStyleClass().add("player-board-header");
        header.getStyleClass().add(isOpponent ? "player-board-header-opponent" : "player-board-header-self");
        return header;
    }

    private ScrollPane buildPropertySection(Player p, boolean isOpponent, double propScale, double cardW) {
        VBox propOuter = new VBox(6);
        Label propTitle = new Label("Properties");
        propTitle.getStyleClass().add("property-column-label");
        if (!isOpponent) propTitle.setStyle("-fx-font-size:12px;");
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
                col.getStyleClass().add("property-column");

                String colName = isOpponent
                        ? CardColorLabel.shortLabel(group.getEffectiveColor())
                        : CardColorLabel.shortLabel(group.getEffectiveColor())
                                + "  " + group.getCards().size() + "/"
                                + (group.getCards().isEmpty() ? "?" : String.valueOf(group.getCards().get(0).getFullSetThreshold()))
                                + (group.isMonopoly() ? " - Monopoly" : "");
                Label colLabel = new Label(colName);
                colLabel.setWrapText(true);
                colLabel.setMaxWidth(cardW * 2.6 + 24);
                colLabel.getStyleClass().add("property-column-label");
                if (isOpponent) colLabel.setStyle("-fx-font-size:10px;");
                col.getChildren().add(colLabel);

                FlowPane cardFlow = new FlowPane();
                cardFlow.setHgap(6);
                cardFlow.setVgap(6);
                cardFlow.setPrefWrapLength(Math.max(160, cardW * 2.35 + 18));

                for (PropertyCard pc : group.getCards()) {
                    if (!isOpponent && (pc.isBiColor() || pc.isMultiColorWild())) {
                        cardFlow.getChildren().add(TableCardKit.createClickableWildCard(pc, propScale, () -> onTableWildCardClick.accept(pc)));
                    } else {
                        cardFlow.getChildren().add(TableCardKit.createReadOnlyCard(pc, propScale));
                    }
                }
                double bScale = propScale * 0.92;
                for (Card b : group.getBuildingCards()) {
                    cardFlow.getChildren().add(TableCardKit.createReadOnlyCard(b, bScale));
                }
                col.getChildren().add(cardFlow);
                propColumns.getChildren().add(col);
            }
        }
        ScrollPane propHScroll = new ScrollPane(propColumns);
        propHScroll.setFocusTraversable(false);
        propHScroll.setPannable(false);
        propHScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        propHScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        propHScroll.getStyleClass().add("scroll-pane-transparent");
        propOuter.getChildren().add(propHScroll);

        ScrollPane propScroll = wrapVerticalScroll(propOuter);
        propScroll.setFitToWidth(true);
        propScroll.setMinWidth(200);
        HBox.setHgrow(propScroll, Priority.ALWAYS);
        return propScroll;
    }

    private VBox buildBankSection(Player p, boolean isOpponent, double bankScale) {
        VBox bankCol = new VBox(6);
        bankCol.setAlignment(Pos.TOP_LEFT);
        bankCol.setMinWidth(isOpponent ? 120 : 200);
        bankCol.setMaxWidth(isOpponent ? 280 : 400);
        bankCol.setPadding(new Insets(0, 0, 0, 6));

        Label bankTitle = new Label("Assets (Bank)");
        bankTitle.getStyleClass().add("bank-section-header");
        if (!isOpponent) bankTitle.setStyle("-fx-font-size:12px;");
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
            Label emptyBank = new Label("(Empty)");
            emptyBank.setStyle("-fx-text-fill:#8D6E63; -fx-font-size:11px;");
            bankFlow.getChildren().add(emptyBank);
        }

        ScrollPane bankScroll = new ScrollPane(bankFlow);
        bankScroll.setFitToWidth(true);
        bankScroll.setPrefViewportHeight(72);
        bankScroll.setMinHeight(54);
        bankScroll.setMaxHeight(92);
        bankScroll.setFocusTraversable(false);
        bankScroll.setPannable(false);
        bankScroll.getStyleClass().add("scroll-pane-transparent");
        bankCol.getChildren().add(bankScroll);

        Label bankTotal = new Label("Total " + sumBank(p) + "M");
        bankTotal.getStyleClass().add("bank-section-total");
        if (!isOpponent) bankTotal.setStyle("-fx-font-size:12px;");
        bankCol.getChildren().add(bankTotal);
        return bankCol;
    }

    private static ScrollPane wrapVerticalScroll(VBox content) {
        content.setFocusTraversable(false);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFocusTraversable(false);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("scroll-pane-transparent");
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
