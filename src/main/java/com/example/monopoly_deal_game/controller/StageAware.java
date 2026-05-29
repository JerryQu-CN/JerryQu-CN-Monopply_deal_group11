package com.example.monopoly_deal_game.controller;

import javafx.stage.Stage;

/** Controllers that need the main stage reference for scene switching via {@link ScreenNavigation}. */
public interface StageAware {
    void setStage(Stage stage);
}
