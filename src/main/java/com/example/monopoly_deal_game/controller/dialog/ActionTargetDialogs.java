package com.example.monopoly_deal_game.controller.dialog;

import com.example.monopoly_deal_game.logic.CardColorLabel;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** In multiplayer games, the card player selects which opponent to target for resolution. */
public final class ActionTargetDialogs {

    private ActionTargetDialogs() {}

    /** Resolve the active window from a stage, falling back to any showing window. */
    static Window resolveOwnerWindow(Stage owner) {
        if (owner != null) return owner;
        if (!Window.getWindows().isEmpty()) {
            return Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        }
        return null;
    }

    public static Optional<Player> chooseOpponent(
            Stage owner, String title, String header, List<Player> eligible) {
        if (eligible == null || eligible.isEmpty()) {
            return Optional.empty();
        }
        List<Player> copy = new ArrayList<>(eligible);
        ChoiceDialog<Player> dlg = new ChoiceDialog<>(copy.get(0), copy);
        dlg.setTitle(title != null ? title : "Select Player");
        dlg.setHeaderText(header != null ? header : "Please select the target player for this round's effect");
        dlg.setContentText("Target Player:");
        Window activeOwner = resolveOwnerWindow(owner);
        if (activeOwner != null) {
            dlg.initOwner(activeOwner);
        }
        return dlg.showAndWait();
    }

    public static Optional<PropertyCard> choosePropertyCard(
            Stage owner, String title, String header, List<PropertyCard> eligible) {
        if (eligible == null || eligible.isEmpty()) {
            return Optional.empty();
        }
        List<PropertyCard> copy = new ArrayList<>(eligible);
        ChoiceDialog<PropertyCard> dlg = new ChoiceDialog<>(copy.get(0), copy);
        dlg.setTitle(title != null ? title : "Select Property");
        dlg.setHeaderText(header != null ? header : "Please select a property card");
        dlg.setContentText("Property:");
        Window activeOwner = resolveOwnerWindow(owner);
        if (activeOwner != null) {
            dlg.initOwner(activeOwner);
        }
        return dlg.showAndWait();
    }

    public static Optional<CardColor> chooseColor(
            Stage owner, String title, String header, List<CardColor> colors) {
        if (colors == null || colors.isEmpty()) return Optional.empty();
        List<CardColor> sorted = new ArrayList<>(colors);
        sorted.sort(Comparator.comparing(CardColorLabel::shortLabel));
        ChoiceDialog<CardColor> dlg = new ChoiceDialog<>(sorted.get(0), sorted);
        dlg.setTitle(title != null ? title : "Select Color");
        dlg.setHeaderText(header != null ? header : "Choose a color for this property card");
        dlg.setContentText("Color:");
        Window activeOwner = resolveOwnerWindow(owner);
        if (activeOwner != null) {
            dlg.initOwner(activeOwner);
        }
        return dlg.showAndWait();
    }

    public static Optional<Property> choosePropertyGroup(
            Stage owner, String title, String header, List<Property> eligible) {
        if (eligible == null || eligible.isEmpty()) {
            return Optional.empty();
        }
        List<Property> copy = new ArrayList<>(eligible);
        ChoiceDialog<Property> dlg = new ChoiceDialog<>(copy.get(0), copy);
        dlg.setTitle(title != null ? title : "Select Property Group");
        dlg.setHeaderText(header != null ? header : "Please select the complete set to take");
        dlg.setContentText("Property Group:");
        Window activeOwner = resolveOwnerWindow(owner);
        if (activeOwner != null) {
            dlg.initOwner(activeOwner);
        }
        return dlg.showAndWait();
    }
}
