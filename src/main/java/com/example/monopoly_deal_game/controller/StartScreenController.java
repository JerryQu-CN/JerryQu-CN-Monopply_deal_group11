package com.example.monopoly_deal_game.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * 主菜单 / 入口，对应设计图中 Lobby 流程的一部分。
 *
 * TODO(controller): 与 {@link com.example.monopoly_deal_game.game.engine.GameEngine#launchGame()} 衔接（若需全局单例引擎）。
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
