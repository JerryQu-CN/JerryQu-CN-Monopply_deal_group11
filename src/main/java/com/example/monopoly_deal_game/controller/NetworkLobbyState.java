package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.network.NetworkClient;
import com.example.monopoly_deal_game.network.NetworkMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class NetworkLobbyState {
    private volatile String roomId;
    private volatile boolean ready;
    private volatile GameSession session;
    private volatile String localPlayerName;
    private volatile boolean host;
    private volatile NetworkClient client;
    private final List<String> players = new CopyOnWriteArrayList<>();
    private final List<Consumer<NetworkMessage>> listeners = new CopyOnWriteArrayList<>();

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
    public GameSession getSession() { return session; }
    public void setSession(GameSession session) { this.session = session; }
    public List<String> getPlayers() { return players; }
    public String getLocalPlayerName() { return localPlayerName; }
    public void setLocalPlayerName(String localPlayerName) { this.localPlayerName = localPlayerName; }
    public boolean isHost() { return host; }
    public void setHost(boolean host) { this.host = host; }
    public NetworkClient getClient() { return client; }
    public void setClient(NetworkClient client) { this.client = client; }

    public void addListener(Consumer<NetworkMessage> listener) {
        if (listener != null) listeners.add(listener);
    }

    public void sync(NetworkMessage msg) {
        if (msg == null) return;
        this.roomId = msg.getRoomId() != null ? msg.getRoomId() : roomId;
        if (msg.getText() != null) {
            this.ready = msg.getText().startsWith("READY") || msg.getText().contains("START");
        }
        if (msg.getSession() != null) this.session = msg.getSession();
        if (msg.getPlayers() != null && !msg.getPlayers().isEmpty()) {
            players.clear();
            players.addAll(msg.getPlayers());
        }
        for (Consumer<NetworkMessage> listener : listeners) {
            listener.accept(msg);
        }
    }
}
