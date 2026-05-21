package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.network.NetworkMessage;
import javafx.application.Platform;

public final class NetworkGameController {
    private NetworkGameController() {}
    public static void handleIncoming(NetworkMessage msg) {
        if (msg == null) return;
        Platform.runLater(() -> {
            AppContext.get().networkLobbyState().sync(msg);
        });
    }
}
