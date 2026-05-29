package com.example.monopoly_deal_game.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Main menu / entry point, corresponding to part of the Lobby flow in the design.
 *
 * TODO(controller): Wire up with {@link com.example.monopoly_deal_game.game.engine.GameEngine#launchGame()} (if a singleton engine is needed).
 */
public class StartScreenController implements StageAware {

    @FXML
    private Button joinRoomButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button loadingButton;

    @FXML
    private Button realPlayerButton;

    private Stage stage;

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    void onRealPlayer(ActionEvent event) {
        ScreenNavigation.show(stage, "AddPlayerScreen.fxml");
    }

    @FXML
    void onJoinRoom(ActionEvent event) {
        ScreenNavigation.show(stage, "JoinRoomScreen.fxml");
    }

    @FXML
    void onLoadingGame(ActionEvent event) {
        ScreenNavigation.show(stage, "LoadGameScreen.fxml");
    }

    @FXML
    void onExit(ActionEvent event) {
        Platform.exit();
    }
}
