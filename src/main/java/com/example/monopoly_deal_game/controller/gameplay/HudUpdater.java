package com.example.monopoly_deal_game.controller.gameplay;

import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.GameConfig;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.view.GameActionLogger;
import com.example.monopoly_deal_game.view.MoneyHudText;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * Updates the HUD — turn indicator, moves count, money display, action button state,
 * and deck/discard pile counts.
 */
public class HudUpdater {

    private final Label topbarTitle;
    private final Label movesLabel;
    private final Label moneyHudLabel;
    private final Button primaryActionButton;
    private final Label feedbackLabel;
    private final Consumer<String> feedback;
    private final HandCardPicker handCardPicker;
    private final GameActionLogger logger;
    private final StackPane gameOverOverlay;

    public HudUpdater(Label topbarTitle, Label movesLabel, Label moneyHudLabel,
                      Button primaryActionButton, Label feedbackLabel,
                      Consumer<String> feedback,
                      HandCardPicker handCardPicker, GameActionLogger logger,
                      StackPane gameOverOverlay) {
        this.topbarTitle = topbarTitle;
        this.movesLabel = movesLabel;
        this.moneyHudLabel = moneyHudLabel;
        this.primaryActionButton = primaryActionButton;
        this.feedbackLabel = feedbackLabel;
        this.feedback = feedback;
        this.handCardPicker = handCardPicker;
        this.logger = logger;
        this.gameOverOverlay = gameOverOverlay;
    }

    public void applyHud(GameSession session) {
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
        String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
        Player local = session.localPlayer(localName);
        String who = current != null ? current.getName() : "?";
        boolean myTurn = local != null && current != null && local.equals(current);

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

        logger.logStateTransition(session);

        topbarTitle.setText("Turn: " + who + (myTurn ? " (You)" : " (Waiting)")
                + "  |  " + GameActionLogger.formatPhase(state.getPhase()));
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
                    int remaining = must - sel;
                    if (remaining > 0) {
                        feedback.accept("Must discard " + must + " card(s)! Currently selected: "
                                + sel + " — pick " + remaining + " more.");
                    } else {
                        feedback.accept("Selected " + sel + " card(s) to discard (need " + must
                                + "). Press button to confirm.");
                    }
                }
            }
            case WAITING_FOR_SELECTION -> {
                primaryActionButton.setText("Waiting for response...");
                primaryActionButton.setDisable(true);
            }
        }
    }
}
