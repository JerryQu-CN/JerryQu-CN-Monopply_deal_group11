package com.example.monopoly_deal_game.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * Full game interface, corresponding to {@code GameplayScreen.fxml} (includes optional chat area).
 */
public class GameplayScreenController extends AbstractGameplayScreenController {

    @FXML
    private Pane chatPane;

    @Override
    protected Pane chatPaneOrNull() {
        return chatPane;
    }
}
