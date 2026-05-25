package com.example.monopoly_deal_game.network;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class NetworkMessage implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public enum Type {
        HELLO,
        HOST_ROOM,
        JOIN_ROOM,
        ROOM_STATE,
        START_GAME,
        SESSION_SNAPSHOT,
        PLAYER_ACTION,
        PAYMENT_REQUEST,
        PAYMENT_RESPONSE,
        JUST_SAY_NO_REQUEST,
        JUST_SAY_NO_RESPONSE,
        READY,
        ERROR,
        DISCONNECT,
        PING
    }

    private final Type type;
    private final String roomId;
    private final String playerName;
    private final String hostName;
    private final String text;
    private final int maxPlayers;
    private final int port;
    private final GameSession session;
    private final List<String> players;
    private final String requestId;
    private final String payerName;
    private final String receiverName;
    private final int amountM;
    private final List<Card> selectedCards;
    private final boolean accepted;

    private NetworkMessage(Builder b) {
        type = b.type;
        roomId = b.roomId;
        playerName = b.playerName;
        hostName = b.hostName;
        text = b.text;
        maxPlayers = b.maxPlayers;
        port = b.port;
        session = b.session;
        players = b.players == null ? List.of() : List.copyOf(b.players);
        requestId = b.requestId;
        payerName = b.payerName;
        receiverName = b.receiverName;
        amountM = b.amountM;
        selectedCards = b.selectedCards == null ? List.of() : List.copyOf(b.selectedCards);
        accepted = b.accepted;
    }

    public static Builder builder(Type type) { return new Builder(type); }
    public Type getType() { return type; }
    public String getRoomId() { return roomId; }
    public String getPlayerName() { return playerName; }
    public String getHostName() { return hostName; }
    public String getText() { return text; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getPort() { return port; }
    public GameSession getSession() { return session; }
    public List<String> getPlayers() { return players; }
    public String getRequestId() { return requestId; }
    public String getPayerName() { return payerName; }
    public String getReceiverName() { return receiverName; }
    public int getAmountM() { return amountM; }
    public List<Card> getSelectedCards() { return selectedCards; }
    public boolean isAccepted() { return accepted; }

    public static final class Builder {
        private final Type type;
        private String roomId;
        private String playerName;
        private String hostName;
        private String text;
        private int maxPlayers;
        private int port;
        private GameSession session;
        private List<String> players;
        private String requestId;
        private String payerName;
        private String receiverName;
        private int amountM;
        private List<Card> selectedCards;
        private boolean accepted;

        private Builder(Type type) { this.type = type; }
        public Builder roomId(String v) { roomId = v; return this; }
        public Builder playerName(String v) { playerName = v; return this; }
        public Builder hostName(String v) { hostName = v; return this; }
        public Builder text(String v) { text = v; return this; }
        public Builder maxPlayers(int v) { maxPlayers = v; return this; }
        public Builder port(int v) { port = v; return this; }
        public Builder session(GameSession v) { session = v; return this; }
        public Builder players(List<String> v) { players = v; return this; }
        public Builder requestId(String v) { requestId = v; return this; }
        public Builder payerName(String v) { payerName = v; return this; }
        public Builder receiverName(String v) { receiverName = v; return this; }
        public Builder amountM(int v) { amountM = v; return this; }
        public Builder selectedCards(List<Card> v) { selectedCards = v; return this; }
        public Builder accepted(boolean v) { accepted = v; return this; }
        public NetworkMessage build() { return new NetworkMessage(this); }
    }

    public static List<String> extractPlayerNames(GameSession session) {
        if (session == null) return List.of();
        List<String> out = new ArrayList<>();
        for (Player p : session.getPlayers()) out.add(p.getName());
        return out;
    }
}
