package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.network.NetworkMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Host/lobby status bridge so host UI can react to remote join and in-game session events.
 */
public final class HostLobbyBridge {
    private static final List<Consumer<NetworkMessage>> listeners = new CopyOnWriteArrayList<>();

    private HostLobbyBridge() {}

    public static void setListener(Consumer<NetworkMessage> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public static void emit(NetworkMessage msg) {
        if (msg == null) return;
        for (Consumer<NetworkMessage> listener : listeners) {
            listener.accept(msg);
        }
    }
}
