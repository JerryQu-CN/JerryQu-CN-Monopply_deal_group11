package com.example.monopoly_deal_game.network;

import com.example.monopoly_deal_game.game.model.GameSession;

import java.io.Serial;
import java.io.Serializable;

/** Multiplayer session snapshot wrapper. */
public class NetworkState implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private final String roomId;
    private final GameSession session;
    private final boolean ready;

    public NetworkState(String roomId, GameSession session, boolean ready) {
        this.roomId = roomId;
        this.session = session;
        this.ready = ready;
    }
    public String getRoomId() { return roomId; }
    public GameSession getSession() { return session; }
    public boolean isReady() { return ready; }
}
