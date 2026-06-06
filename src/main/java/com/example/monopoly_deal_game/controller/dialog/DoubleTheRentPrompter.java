package com.example.monopoly_deal_game.controller.dialog;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCardDoubleTheRent;
import com.example.monopoly_deal_game.model.cards.Card;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Prompts the player whether to use a Double The Rent card alongside their chosen rent card.
 */
public final class DoubleTheRentPrompter {

    private DoubleTheRentPrompter() {}

    public static Card findDoubleTheRent(Player player) {
        for (Card c : player.getHand().getCards()) {
            if (c instanceof ActionCardDoubleTheRent) return c;
        }
        return null;
    }

    public static boolean showPrompt(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Double The Rent");
        alert.setHeaderText("You have a \"Double The Rent\" card in hand.");
        alert.setContentText("Use it to double this rent?\n(Does NOT count toward your play limit.)");
        Window activeOwner = ActionTargetDialogs.resolveOwnerWindow(stage);
        if (activeOwner != null) {
            alert.initOwner(activeOwner);
        }
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
