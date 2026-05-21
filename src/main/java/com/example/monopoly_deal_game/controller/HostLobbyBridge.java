package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.network.NetworkMessage;

/**
 * Host/lobby status bridge so host UI can react to remote join events.
 */
public final class HostLobbyBridge {
    private static volatile java.util.function.Consumer<NetworkMessage> listener;

    private HostLobbyBridge() {}

    public static void setListener(java.util.function.Consumer<NetworkMessage> l) {
        listener = l;
    }

    public static void emit(NetworkMessage msg) {
        java.util.function.Consumer<NetworkMessage> l = listener;
        if (l != null && msg != null) l.accept(msg);
    }
}
