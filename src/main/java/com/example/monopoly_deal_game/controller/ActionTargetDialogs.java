package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** In multiplayer games, the card player selects which opponent to target for resolution. */
final class ActionTargetDialogs {

    private ActionTargetDialogs() {}

    static Optional<Player> chooseOpponent(
            Stage owner, String title, String header, List<Player> eligible) {
        if (eligible == null || eligible.isEmpty()) {
            return Optional.empty();
        }
        List<Player> copy = new ArrayList<>(eligible);
        ChoiceDialog<Player> dlg = new ChoiceDialog<>(copy.get(0), copy);
        dlg.setTitle(title != null ? title : "Select Player");
        dlg.setHeaderText(header != null ? header : "Please select the target player for this round's effect");
        dlg.setContentText("Target Player:");
        Window activeOwner = owner;
        if (activeOwner == null && !Window.getWindows().isEmpty()) {
            activeOwner = Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        }
        if (activeOwner != null) {
            dlg.initOwner(activeOwner);
        }
        return dlg.showAndWait();
    }

    static Optional<PropertyCard> choosePropertyCard(
            Stage owner, String title, String header, List<PropertyCard> eligible) {
        if (eligible == null || eligible.isEmpty()) {
            return Optional.empty();
        }
        List<PropertyCard> copy = new ArrayList<>(eligible);
        ChoiceDialog<PropertyCard> dlg = new ChoiceDialog<>(copy.get(0), copy);
        dlg.setTitle(title != null ? title : "Select Property");
        dlg.setHeaderText(header != null ? header : "Please select a property card");
        dlg.setContentText("Property:");
        Window activeOwner = owner;
        if (activeOwner == null && !Window.getWindows().isEmpty()) {
            activeOwner = Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        }
        if (activeOwner != null) {
            dlg.initOwner(activeOwner);
        }
        return dlg.showAndWait();
    }

    static Optional<Property> choosePropertyGroup(
            Stage owner, String title, String header, List<Property> eligible) {
        if (eligible == null || eligible.isEmpty()) {
            return Optional.empty();
        }
        List<Property> copy = new ArrayList<>(eligible);
        ChoiceDialog<Property> dlg = new ChoiceDialog<>(copy.get(0), copy);
        dlg.setTitle(title != null ? title : "Select Property Group");
        dlg.setHeaderText(header != null ? header : "Please select the complete set to take");
        dlg.setContentText("Property Group:");
        Window activeOwner = owner;
        if (activeOwner == null && !Window.getWindows().isEmpty()) {
            activeOwner = Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        }
        if (activeOwner != null) {
            dlg.initOwner(activeOwner);
        }
        return dlg.showAndWait();
    }
}
