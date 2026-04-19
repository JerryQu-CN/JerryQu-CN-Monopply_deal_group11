package com.example.monopoly_deal_game.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 人机大厅：机器人数量与开始游戏（需求 1.2：机器人 1–4，总人数 ≤5）。
 *
 * TODO(controller+logic): 调用 {@link com.example.monopoly_deal_game.logic.GameEngine#startLocalGame} 创建 {@link com.example.monopoly_deal_game.model.ComputerPlayer} 列表。
 * TODO(controller+ai): 将 {@link com.example.monopoly_deal_game.ai.BotPolicy} 注入各机器人玩家。
 */
public class AddBotPlayerScreenController implements StageAware, Initializable {

    @FXML
    private ComboBox<Integer> botCountCombo;

    @FXML
    private Button startGameButton;

    private Stage stage;

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 需求：机器人 1–4 台（总人数 ≤5 在点击开始时再校验）
        botCountCombo.getItems().setAll(1, 2, 3, 4);
        startGameButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> botCountCombo.getValue() == null,
                        botCountCombo.valueProperty()));
    }

    @FXML
    void onStartGame(ActionEvent event) {
        // TODO(controller+logic): 根据 Combo解析「人类玩家数 + 机器人数」并校验 ≤5
        ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
    }

    @FXML
    void onBack(ActionEvent event) {
        ScreenNavigation.show(stage, "StartScreen.fxml");
    }
}
