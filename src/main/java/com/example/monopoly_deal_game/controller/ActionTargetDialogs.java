package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.model.Player;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;

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
        if (owner != null) {
            dlg.initOwner(owner);
        }
        return dlg.showAndWait();
    }
}
