package com.example.monopoly_deal_game.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * 完整对局界面，对应 {@code GameplayScreen.fxml}（含可选聊天区）。
 */
public class GameplayScreenController extends AbstractGameplayScreenController {

    @FXML
    private Pane chatPane;

    @Override
    protected Pane chatPaneOrNull() {
        return chatPane;
    }
}
