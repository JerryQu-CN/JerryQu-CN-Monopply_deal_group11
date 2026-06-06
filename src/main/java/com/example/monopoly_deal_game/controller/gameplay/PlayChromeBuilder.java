package com.example.monopoly_deal_game.controller.gameplay;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.*;
import com.example.monopoly_deal_game.logic.payment.RentCalculator;
import com.example.monopoly_deal_game.logic.payment.RentRules;
import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * Builds the contextual action bar below the hand — Play, Bank, Property, Rent,
 * and Double The Rent buttons based on the selected card and game state.
 */
public class PlayChromeBuilder {
    private static final double BOTTOM_INSET = 216;
    private InvalidationListener layoutListener;

    public VBox build(GameSession session, Card selectedCard,
                      Consumer<CardPlayOptions> onPlay, Runnable refreshUi) {
        if (session == null || selectedCard == null) return null;

        GameState st = session.getGameState();
        if (st.isGameOver() || st.getPhase() != GameState.Phase.PLAY_PHASE
                || !requiresExplicitPlayMode(selectedCard)) {
            return null;
        }

        GameLogic logic = AppContext.get().gameEngine().getGameLogic();
        Player me = session.getCurrentPlayer();
        if (me == null) return null;

        VBox dock = new VBox(10);
        dock.setAlignment(Pos.CENTER);
        dock.setPadding(new Insets(10, 16, 10, 16));
        dock.setPickOnBounds(true);
        dock.getStyleClass().add("action-dock");

        Label bar = new Label("Play Options (click another hand card to cancel or reselect)");
        bar.getStyleClass().add("action-dock-label");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);

        switch (selectedCard.getCardType()) {
            case RENT -> buildRentChrome(dock, bar, row, (RentCard) selectedCard, me, session, st, onPlay);
            case PROPERTY -> buildPropertyChrome(dock, bar, row, (PropertyCard) selectedCard, selectedCard, onPlay, refreshUi);
            case ACTION -> buildActionChrome(dock, bar, row, (ActionCard) selectedCard, session, logic, onPlay);
            default -> { return null; }
        }

        return dock;
    }

    public void attach(Pane actionLayer, VBox dock) {
        if (dock == null || actionLayer == null) return;
        detach(actionLayer);
        actionLayer.getChildren().add(dock);
        actionLayer.setMouseTransparent(false);
        actionLayer.setPickOnBounds(false);
        layoutListener = obs -> layoutDock(actionLayer, dock);
        actionLayer.widthProperty().addListener(layoutListener);
        actionLayer.heightProperty().addListener(layoutListener);
        layoutDock(actionLayer, dock);
    }

    public void detach(Pane actionLayer) {
        if (layoutListener != null && actionLayer != null) {
            actionLayer.widthProperty().removeListener(layoutListener);
            actionLayer.heightProperty().removeListener(layoutListener);
            layoutListener = null;
        }
        if (actionLayer != null) {
            actionLayer.getChildren().clear();
            actionLayer.setMouseTransparent(true);
            actionLayer.setPickOnBounds(false);
        }
    }

    public static boolean requiresExplicitPlayMode(Card c) {
        return switch (c.getCardType()) {
            case ACTION, RENT, PROPERTY -> true;
            default -> false;
        };
    }

    // --- private builders ---

    private void buildRentChrome(VBox dock, Label bar, HBox row, RentCard rc,
                                  Player me, GameSession session, GameState st,
                                  Consumer<CardPlayOptions> onPlay) {
        Button toBank = new Button("Deposit to Bank (value: " + Math.max(0, rc.getValue()) + "M)");
        toBank.getStyleClass().add("button-chrome-action");
        toBank.setOnAction(e -> onPlay.accept(CardPlayOptions.bankOnly()));
        row.getChildren().add(toBank);

        List<CardColor> colors = RentRules.eligibleChargeColors(rc, me);
        FlowPane fp = new FlowPane();
        fp.setAlignment(Pos.CENTER);
        fp.setHgap(8);
        fp.setVgap(8);

        if (colors.isEmpty()) {
            Label empty = new Label("No colors available for rent collection right now");
            empty.setStyle("-fx-text-fill: #FFF8E1;");
            fp.getChildren().add(empty);
        } else {
            boolean dbl = st.isDoubleNextRent();
            for (CardColor col : colors) {
                int base = RentCalculator.rentOnColor(me, col);
                int display = dbl ? base * 2 : base;
                Button cb = new Button("Rent " + CardColorLabel.shortLabel(col) + " -> " + display + "M");
                cb.getStyleClass().add("button-chrome-action");
                cb.setDisable(!RentRules.canUseRentEffectForUi(rc, me, col, session));
                CardColor cap = col;
                cb.setOnAction(e -> onPlay.accept(CardPlayOptions.rentWithColor(cap)));
                fp.getChildren().add(cb);
            }
        }
        dock.getChildren().addAll(bar, row, fp);
    }

    private void buildPropertyChrome(VBox dock, Label bar, HBox row,
                                      PropertyCard pc, Card sel,
                                      Consumer<CardPlayOptions> onPlay, Runnable refreshUi) {
        Button toTable = new Button("Place on Table (current: " + CardColorLabel.shortLabel(pc.getCurrentColor()) + ")");
        toTable.getStyleClass().add("button-chrome-action");
        toTable.setOnAction(e -> onPlay.accept(CardPlayOptions.auto()));

        Button toBank = new Button("Deposit to Bank (value: " + Math.max(0, sel.getValue()) + "M)");
        toBank.getStyleClass().add("button-chrome-action");
        toBank.setOnAction(e -> onPlay.accept(CardPlayOptions.bankOnly()));

        row.getChildren().addAll(toTable, toBank);
        dock.getChildren().addAll(bar, row);

        if (pc.canFlipWildDualColor()) {
            FlowPane colorChoices = new FlowPane();
            colorChoices.setAlignment(Pos.CENTER);
            colorChoices.setHgap(8);
            colorChoices.setVgap(8);
            for (CardColor color : pc.getSelectableColors()) {
                Button choose = new Button("As " + CardColorLabel.shortLabel(color));
                choose.setDisable(color == pc.getCurrentColor());
                choose.getStyleClass().add("button-chrome-action");
                choose.setOnAction(e -> { pc.alignToDeclaredColor(color); refreshUi.run(); });
                colorChoices.getChildren().add(choose);
            }
            dock.getChildren().add(colorChoices);
        }
    }

    private void buildActionChrome(VBox dock, Label bar, HBox row,
                                    ActionCard ac, GameSession session,
                                    GameLogic logic, Consumer<CardPlayOptions> onPlay) {
        if (ac.isOnlyBankable()) {
            Button toBank = new Button("Deposit to Bank (value: " + Math.max(0, ac.getValue()) + "M)");
            toBank.getStyleClass().add("button-chrome-action");
            toBank.setOnAction(e -> onPlay.accept(CardPlayOptions.bankOnly()));
            row.getChildren().add(toBank);
        } else {
            Button use = new Button("Use Card Effect");
            use.getStyleClass().add("button-chrome-action");
            boolean can = logic.getEffectExecutor().canUseActionEffectForUi(ac, session);
            use.setDisable(!can);
            if (!can) use.setTooltip(new Tooltip("No valid targets or prerequisites not met; you can deposit this card as cash to the bank."));
            use.setOnAction(e -> onPlay.accept(CardPlayOptions.auto()));

            Button toBank = new Button("Deposit to Bank (value: " + Math.max(0, ac.getValue()) + "M)");
            toBank.getStyleClass().add("button-chrome-action");
            toBank.setOnAction(e -> onPlay.accept(CardPlayOptions.bankOnly()));

            row.getChildren().addAll(use, toBank);
        }
        dock.getChildren().addAll(bar, row);
    }

    private void layoutDock(Pane actionLayer, VBox dock) {
        if (dock == null || !actionLayer.getChildren().contains(dock)) return;
        double pw = actionLayer.getWidth();
        double ph = actionLayer.getHeight();
        if (pw <= 1 || ph <= 1) return;
        double maxW = Math.max(200, pw - 40);
        double dw = dock.prefWidth(maxW);
        dock.setMaxWidth(maxW);
        dock.setLayoutX(Math.max(20, (pw - Math.min(dw, maxW)) / 2));
        double y = ph - BOTTOM_INSET - dock.prefHeight(dw);
        dock.setLayoutY(Math.max(40, y));
    }
}