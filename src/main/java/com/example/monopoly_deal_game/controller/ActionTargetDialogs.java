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

/** 多人对局时由出牌者选择「针对哪一位对手」结算。 */
final class ActionTargetDialogs {

    private ActionTargetDialogs() {}

    static Optional<Player> chooseOpponent(
            Stage owner, String title, String header, List<Player> eligible) {
        if (eligible == null || eligible.isEmpty()) {
            return Optional.empty();
        }
        List<Player> copy = new ArrayList<>(eligible);
        ChoiceDialog<Player> dlg = new ChoiceDialog<>(copy.get(0), copy);
        dlg.setTitle(title != null ? title : "选择玩家");
        dlg.setHeaderText(header != null ? header : "请选择本轮效果作用的目标玩家");
        dlg.setContentText("目标玩家：");
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
        dlg.setTitle(title != null ? title : "选择房产");
        dlg.setHeaderText(header != null ? header : "请选择一张房产牌");
        dlg.setContentText("房产：");
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
        dlg.setTitle(title != null ? title : "选择地产组");
        dlg.setHeaderText(header != null ? header : "请选择要夺取的完整成套");
        dlg.setContentText("地产组：");
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
