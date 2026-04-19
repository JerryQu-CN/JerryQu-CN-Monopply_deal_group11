package com.example.monopoly_deal_game.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 真人联机大厅占位界面（需求 1 的座位与房间 UI 可在此扩展）。
 *
 * TODO(controller+network): 房间列表、加入房间改为 {@link com.example.monopoly_deal_game.network.NetworkClient} 真实回调。
 * TODO(controller+logic): 点击「开始游戏」时调用 {@link com.example.monopoly_deal_game.logic.GameEngine#startLanGame()} 或 {@code initGame} 传参。
 */
public class AddPlayerScreenController implements StageAware, Initializable {

    @FXML
    private ListView<String> joinedPlayersList;

    @FXML
    private TextField joinRoomField;

    @FXML
    private Button joinRoomButton;

    @FXML
    private ComboBox<Integer> maxPlayersCombo;

    @FXML
    private Label roomIdLabel;

    @FXML
    private Button startGameButton;

    private Stage stage;
    private String roomId;
    private final ObservableList<String> joinedPlayers = FXCollections.observableArrayList();

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roomId = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        roomIdLabel.setText("Room ID: " + roomId + " (LAN placeholder)");

        maxPlayersCombo.getItems().setAll(2, 3, 4, 5);
        maxPlayersCombo.getSelectionModel().select(Integer.valueOf(4));

        joinedPlayers.setAll(List.of("You (host)"));
        joinedPlayersList.setItems(joinedPlayers);

        startGameButton.disableProperty().bind(Bindings.size(joinedPlayers).lessThan(2));
    }

    @FXML
    void onJoinRoom(ActionEvent event) {
        String code = joinRoomField.getText() == null ? "" : joinRoomField.getText().strip();
        if (code.isEmpty()) {
            return;
        }
        Integer cap = maxPlayersCombo.getSelectionModel().getSelectedItem();
        if (cap == null || joinedPlayers.size() >= cap) {
            return;
        }
        // TODO(controller): 替换为网络层加入成功后的玩家列表刷新
        joinedPlayers.add("Player " + joinedPlayers.size() + " (room " + code + ", placeholder)");
    }

    @FXML
    void onStartGame(ActionEvent event) {
        // TODO(controller+logic): GameEngine.startLanGame(...) 或 Host 上 startLocalGame(...)
        ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
    }

    @FXML
    void onBack(ActionEvent event) {
        ScreenNavigation.show(stage, "StartScreen.fxml");
    }
}
