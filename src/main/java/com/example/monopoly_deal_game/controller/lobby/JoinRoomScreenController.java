package com.example.monopoly_deal_game.controller.lobby;

import com.example.monopoly_deal_game.network.NetworkClient;
import com.example.monopoly_deal_game.network.NetworkMessage;
import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.controller.ScreenNavigation;
import com.example.monopoly_deal_game.controller.StageAware;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the join-room screen — connects to a host by room ID
 * and transitions into the game when the host starts.
 */
public class JoinRoomScreenController implements StageAware, Initializable {
    @FXML private TextField nicknameField;
    @FXML private TextField joinRoomField;
    @FXML private TextField joinHostField;
    @FXML private TextField joinPortField;
    @FXML private Label roomInfoLabel;
    @FXML private Label joinStatusLabel;
    @FXML private Button joinButton;
    @FXML private ListView<String> joinedPlayersList;

    private Stage stage;
    private boolean pendingStartNavigation;
    private volatile boolean navigationDone;
    private NetworkClient client;
    private final ObservableList<String> joinedPlayers = FXCollections.observableArrayList();

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        if (!navigationDone && (pendingStartNavigation || AppContext.get().networkLobbyState().getSession() != null)) {
            pendingStartNavigation = false;
            navigationDone = true;
            ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        joinedPlayersList.setItems(joinedPlayers);
        joinedPlayers.setAll(List.of("Waiting to join..."));
        AppContext.get().networkLobbyState().addListener(msg -> {
            if (msg != null) Platform.runLater(() -> syncFromMessage(msg));
        });
        NetworkMessage snapshot = AppContext.get().networkLobbyState().getSession() != null
                ? NetworkMessage.builder(NetworkMessage.Type.SESSION_SNAPSHOT)
                        .session(AppContext.get().networkLobbyState().getSession())
                        .roomId(AppContext.get().networkLobbyState().getRoomId())
                        .build()
                : null;
        if (snapshot != null && stage != null) {
            Platform.runLater(() -> syncFromMessage(snapshot));
        }
    }

    @FXML
    void onJoinRoom(ActionEvent event) {
        String nick = text(nicknameField);
        String roomId = text(joinRoomField);
        String host = text(joinHostField);
        String portText = text(joinPortField);
        if (nick.isEmpty() || roomId.isEmpty() || host.isEmpty() || portText.isEmpty()) {
            setStatus("Please enter nickname, room ID, host IP and port.");
            return;
        }
        try {
            int port = Integer.parseInt(portText);
            closeClient();
            setStatus("Connecting...");
            client = new NetworkClient(host, port, msg -> Platform.runLater(() -> AppContext.get().networkLobbyState().sync(msg)));
            AppContext.get().networkLobbyState().setClient(client);
            AppContext.get().networkLobbyState().setHost(false);
            AppContext.get().networkLobbyState().setLocalPlayerName(nick);
            client.send(NetworkMessage.builder(NetworkMessage.Type.HELLO).roomId(roomId).playerName(nick).build());
            client.send(NetworkMessage.builder(NetworkMessage.Type.JOIN_ROOM).roomId(roomId).playerName(nick).build());
            AppContext.get().networkLobbyState().setRoomId(roomId);
            setStatus("Join request sent, waiting for host...");
        } catch (Exception ex) {
            setStatus("Join failed: " + ex.getMessage());
        }
    }

    @FXML
    void onBack(ActionEvent event) {
        closeClient();
        ScreenNavigation.show(stage, "StartScreen.fxml");
    }

    private void syncFromMessage(NetworkMessage msg) {
        if (msg.getType() == NetworkMessage.Type.ERROR) {
            String errorText = msg.getText() != null ? msg.getText() : "Unknown error";
            if (errorText.startsWith("ROOM_FULL|")) {
                String displayText = errorText.substring("ROOM_FULL|".length());
                Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Room Full");
                    alert.setHeaderText("Cannot join room");
                    alert.setContentText(displayText);
                    if (stage != null) alert.initOwner(stage);
                    alert.showAndWait();
                    joinedPlayers.setAll(List.of("(Room is full)"));
                    roomInfoLabel.setText("Room: CLOSED (full)");
                });
                closeClient();
            }
            setStatus(errorText);
            return;
        }
        boolean readyRoom = msg.getType() == NetworkMessage.Type.ROOM_STATE && msg.getText() != null && msg.getText().startsWith("READY");
        if (msg.getRoomId() != null) {
            roomInfoLabel.setText("Room info: " + msg.getRoomId() + (readyRoom ? " (started)" : ""));
        }
        if (msg.getPlayers() != null && !msg.getPlayers().isEmpty()) {
            joinedPlayers.setAll(msg.getPlayers());
        }
        if (msg.getSession() != null) {
            AppContext.get().networkLobbyState().setSession(msg.getSession());
            AppContext.get().gameEngine().resumeSession(msg.getSession());
            if (!navigationDone) {
                if (stage != null) {
                    navigationDone = true;
                    ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
                } else {
                    pendingStartNavigation = true;
                }
            }
            return;
        }

        if (msg.getType() == NetworkMessage.Type.START_GAME || msg.getType() == NetworkMessage.Type.SESSION_SNAPSHOT || readyRoom) {
            if (AppContext.get().networkLobbyState().getSession() == null) {
                setStatus("Host started. Waiting for session snapshot...");
                return;
            }
            AppContext.get().gameEngine().resumeSession(AppContext.get().networkLobbyState().getSession());
            if (!navigationDone) {
                if (stage != null) {
                    navigationDone = true;
                    ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
                } else {
                    pendingStartNavigation = true;
                }
            }
        }
    }

    private void setStatus(String text) {
        if (joinStatusLabel != null) joinStatusLabel.setText(text);
    }

    private void closeClient() {
        if (client != null) {
            try { client.close(); } catch (Exception ignored) {}
            client = null;
        }
    }

    private static String text(TextField tf) {
        return tf.getText() == null ? "" : tf.getText().strip();
    }
}
