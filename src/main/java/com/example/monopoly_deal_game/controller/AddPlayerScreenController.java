package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkMessage;
import javafx.application.Platform;
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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

public class AddPlayerScreenController implements StageAware, Initializable {
    @FXML private ListView<String> joinedPlayersList;
    @FXML private ComboBox<Integer> maxPlayersCombo;
    @FXML private Label roomIdLabel;
    @FXML private Label roomConnectionLabel;
    @FXML private Label joinStatusLabel;
    @FXML private Button startGameButton;
    @FXML private TextField hostNameField;
    @FXML private Button hostRoomButton;

    private Stage stage;
    private String roomId;
    private boolean hosting;
    private final ObservableList<String> joinedPlayers = FXCollections.observableArrayList();

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        maxPlayersCombo.getItems().setAll(2, 3, 4, 5);
        maxPlayersCombo.getSelectionModel().select(Integer.valueOf(4));
        joinedPlayers.setAll(List.of("Waiting for host..."));
        joinedPlayersList.setItems(joinedPlayers);
        updateModeUi();
        HostLobbyBridge.setListener(msg -> Platform.runLater(() -> syncLobbyFromServer(msg)));
    }

    @FXML
    void onHostRoom(ActionEvent event) {
        hosting = true;
        String nickname = nicknameOrHost();
        AppContext.get().networkLobbyState().setLocalPlayerName(nickname);
        AppContext.get().networkLobbyState().setHost(true);
        Integer cap = maxPlayersCombo.getSelectionModel().getSelectedItem();
        if (cap == null) cap = 4;
        GameServer.Room room = AppContext.get().gameServer().createRoom(nickname, cap);
        roomId = room.getRoomId();
        String lanIp = detectLanIpAddress();
        roomIdLabel.setText("Room ID: " + roomId + " (host)");
        if (roomConnectionLabel != null) {
            roomConnectionLabel.setText("Host IP: " + lanIp + "   Port: " + room.getPort());
        }
        joinedPlayers.setAll(room.getPlayers());
        AppContext.get().networkLobbyState().setRoomId(roomId);
        AppContext.get().networkLobbyState().setReady(false);
        AppContext.get().networkLobbyState().getPlayers().clear();
        AppContext.get().networkLobbyState().getPlayers().addAll(room.getPlayers());
        updateModeUi();
    }

    @FXML
    void onStartGame(ActionEvent event) {
        if (!hosting || roomId == null || joinedPlayers.size() < 2) {
            setStatus("Only host can start after 2+ players join.");
            return;
        }
        try {
            AppContext.get().gameServer().startRoom(roomId);
            GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
            if (room != null) {
                AppContext.get().gameEngine().resumeSession(room.getSession());
                AppContext.get().networkLobbyState().setReady(room.isReady());
                AppContext.get().networkLobbyState().setSession(room.getSession());
                AppContext.get().networkLobbyState().getPlayers().clear();
                AppContext.get().networkLobbyState().getPlayers().addAll(room.getPlayers());
            }
            ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            setStatus("Start failed: " + ex.getMessage());
        }
    }

    @FXML
    void onBack(ActionEvent event) {
        ScreenNavigation.show(stage, "StartScreen.fxml");
    }

    private void syncLobbyFromServer(NetworkMessage msg) {
        if (msg == null) return;
        if (msg.getType() == NetworkMessage.Type.ERROR) {
            setStatus(msg.getText());
            return;
        }
        if (msg.getRoomId() != null) {
            roomId = msg.getRoomId();
            roomIdLabel.setText("Room ID: " + roomId + (msg.getText() != null && msg.getText().startsWith("READY") ? " (ready)" : " (lobby)"));
        }
        if (msg.getPlayers() != null && !msg.getPlayers().isEmpty()) {
            joinedPlayers.setAll(msg.getPlayers());
            startGameButton.setDisable(!hosting || msg.getPlayers().size() < 2);
        }
        if (msg.getSession() != null) {
            GameSession session = msg.getSession();
            AppContext.get().networkLobbyState().setSession(session);
            AppContext.get().gameEngine().resumeSession(session);
        }
    }

    private void updateModeUi() {
        startGameButton.setDisable(!hosting || joinedPlayers.size() < 2);
    }

    private void setStatus(String text) {
        if (joinStatusLabel != null) joinStatusLabel.setText(text);
    }

    private String nicknameOrHost() {
        String n = hostNameField.getText() == null ? "" : hostNameField.getText().strip();
        return n.isEmpty() ? "Host" : n;
    }

    private String detectLanIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        String hostAddress = address.getHostAddress();
                        if (hostAddress.startsWith("192.168.") || hostAddress.startsWith("10.") || is172PrivateAddress(hostAddress)) {
                            return hostAddress;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[AddPlayerScreen] failed to detect LAN IP: " + ex.getMessage());
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
            return "127.0.0.1";
        }
    }

    private boolean is172PrivateAddress(String hostAddress) {
        if (hostAddress == null || !hostAddress.startsWith("172.")) {
            return false;
        }
        String[] parts = hostAddress.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
