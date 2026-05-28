package com.example.monopoly_deal_game.network;

import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.controller.HostLobbyBridge;
import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.PaymentService;
import com.example.monopoly_deal_game.model.Player;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class GameServer {

    private static GameEngine gameEngine = null;
    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();

    public GameServer(GameEngine gameEngine) { GameServer.gameEngine = Objects.requireNonNull(gameEngine); }

    public Room createRoom(String hostName, int maxPlayers) {
        if (maxPlayers < 2 || maxPlayers > 5) throw new IllegalArgumentException("maxPlayers must be between 2 and 5");
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Room room = new Room(roomId, hostName, maxPlayers);
        ROOMS.put(roomId, room);
        room.startAcceptLoop();
        return room;
    }

    public Room joinRoom(String roomId, String playerName) {
        Room room = ROOMS.get(roomId);
        if (room == null) throw new IllegalArgumentException("Room not found");
        room.addLocalPlayer(playerName);
        room.broadcast(Room.snapshot(room));
        return room;
    }

    public void startRoom(String roomId) {
        Room room = ROOMS.get(roomId);
        if (room == null) throw new IllegalArgumentException("Room not found");
        room.startGame();
    }

    public Room getRoom(String roomId) { return ROOMS.get(roomId); }

    public static final class Room {
        private final String roomId;
        private final String hostName;
        private final int maxPlayers;
        private final List<String> players = new CopyOnWriteArrayList<>();
        private final List<ClientConnection> clients = new CopyOnWriteArrayList<>();
        private volatile boolean ready;
        private volatile boolean started;
        private volatile GameSession session;
        private volatile ServerSocket serverSocket;
        private volatile int port;
        private final Map<String, GameSession> pendingRequestSnapshots = new ConcurrentHashMap<>();

        private Room(String roomId, String hostName, int maxPlayers) {
            this.roomId = roomId; this.hostName = hostName; this.maxPlayers = maxPlayers; this.players.add(hostName);
        }
        public String getRoomId(){return roomId;} public String getHostName(){return hostName;} public int getMaxPlayers(){return maxPlayers;} public int getPort(){return port;} public List<String> getPlayers(){return Collections.unmodifiableList(players);} public boolean isReady(){return ready;} public boolean isStarted(){return started;} public GameSession getSession(){return session;}
        public void broadcastSessionSnapshot() {
            if (session == null) return;
            NetworkMessage msg = NetworkMessage.builder(NetworkMessage.Type.SESSION_SNAPSHOT)
                    .roomId(roomId)
                    .text("SESSION_SNAPSHOT")
                    .players(players)
                    .port(port)
                    .session(session)
                    .build();
            broadcast(msg);
            HostLobbyBridge.emit(msg);
        }

        private void startAcceptLoop() {
            try {
                serverSocket = new ServerSocket(0);
                port = serverSocket.getLocalPort();
            } catch (Exception e) {
                System.err.println("[GameServer] Failed to create ServerSocket: " + e.getMessage());
                e.printStackTrace(System.err);
                return;
            }
            Thread accept = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientConnection conn = new ClientConnection(socket, this);
                        clients.add(conn);
                        conn.send(snapshot(this));
                        conn.start();
                    } catch (Exception ex) {
                        if (serverSocket.isClosed()) break;
                        System.err.println("[GameServer] accept error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                        ex.printStackTrace(System.err);
                    }
                }
            }, "room-accept-" + roomId);
            accept.setDaemon(true); accept.start();
        }

        private void addLocalPlayer(String playerName) {
            if (started) throw new IllegalStateException("Room already started");
            if (players.size() >= maxPlayers) throw new IllegalStateException("Room full");
            if (!players.contains(playerName)) {
                players.add(playerName);
                pushRoomState();
                com.example.monopoly_deal_game.controller.HostLobbyBridge.emit(
                        NetworkMessage.builder(NetworkMessage.Type.ROOM_STATE)
                                .roomId(roomId)
                                .players(players)
                                .maxPlayers(maxPlayers)
                                .port(port)
                                .text((ready ? "READY" : "LOBBY") + "|" + hostName)
                                .session(session)
                                .build());
            }
        }

        private void startGame() {
            if (players.size() < 2) throw new IllegalStateException("Need at least 2 players");
            session = new GameSession();
            for (String name : players) session.getPlayers().add(new Player(name, false));
            gameEngine.resumeSession(session);
            gameEngine.getGameLogic().initGame(session);
            ready = true;
            started = true;
            broadcastSessionSnapshot();
        }

        public void broadcastJustSayNoRequest(String requestId, Player respondent, Player activator, GameSession baseSession, String reasonText) {
            if (respondent == null) return;
            pendingRequestSnapshots.put(requestId, baseSession != null ? baseSession : session);
            NetworkMessage msg = NetworkMessage.builder(NetworkMessage.Type.JUST_SAY_NO_REQUEST)
                    .roomId(roomId)
                    .requestId(requestId)
                    .payerName(respondent.getName())
                    .receiverName(activator != null ? activator.getName() : null)
                    .session(baseSession != null ? baseSession : session)
                    .text(reasonText)
                    .build();
            broadcast(msg);
            HostLobbyBridge.emit(msg);
        }

        public void broadcastPaymentRequest(String requestId, Player payer, Player receiver, int amountM, GameSession baseSession, String reasonText) {
            if (payer == null || receiver == null) return;
            pendingRequestSnapshots.put(requestId, baseSession != null ? baseSession : session);
            NetworkMessage msg = NetworkMessage.builder(NetworkMessage.Type.PAYMENT_REQUEST)
                    .roomId(roomId)
                    .requestId(requestId)
                    .payerName(payer.getName())
                    .receiverName(receiver.getName())
                    .amountM(amountM)
                    .session(baseSession != null ? baseSession : session)
                    .text(reasonText)
                    .build();
            broadcast(msg);
            HostLobbyBridge.emit(msg);
        }

        private void handleMessage(ClientConnection conn, NetworkMessage msg) {
            if (msg == null) return;
            switch (msg.getType()) {
                case JOIN_ROOM -> {
                    if (started) {
                        conn.send(NetworkMessage.builder(NetworkMessage.Type.ERROR)
                                .roomId(roomId)
                                .text("Room already started")
                                .build());
                        return;
                    }
                    if (msg.getRoomId() != null && !roomId.equalsIgnoreCase(msg.getRoomId().trim())) {
                        conn.send(NetworkMessage.builder(NetworkMessage.Type.ERROR)
                                .roomId(roomId)
                                .text("Room not found")
                                .build());
                        return;
                    }
                    if (msg.getPlayerName() != null) addLocalPlayer(msg.getPlayerName());
                    conn.send(snapshot(this));
                    pushRoomState();
                    HostLobbyBridge.emit(snapshot(this));
                    return;
                }
                case HOST_ROOM, HELLO -> {
                    conn.send(snapshot(this));
                    return;
                }
                case READY -> ready = true;
                case START_GAME -> startGame();
                case PAYMENT_REQUEST -> {
                    if (session != null && msg.getPayerName() != null && msg.getReceiverName() != null) {
                        if (msg.getRequestId() != null) pendingRequestSnapshots.put(msg.getRequestId(), session);
                        NetworkMessage forward = NetworkMessage.builder(NetworkMessage.Type.PAYMENT_REQUEST)
                                .roomId(roomId)
                                .requestId(msg.getRequestId())
                                .payerName(msg.getPayerName())
                                .receiverName(msg.getReceiverName())
                                .amountM(msg.getAmountM())
                                .session(session)
                                .text(msg.getText())
                                .build();
                        broadcast(forward);
                        HostLobbyBridge.emit(forward);
                    }
                    return;
                }
                case JUST_SAY_NO_REQUEST -> {
                    if (session != null && msg.getPayerName() != null) {
                        if (msg.getRequestId() != null) pendingRequestSnapshots.put(msg.getRequestId(), session);
                        NetworkMessage forward = NetworkMessage.builder(NetworkMessage.Type.JUST_SAY_NO_REQUEST)
                                .roomId(roomId)
                                .requestId(msg.getRequestId())
                                .payerName(msg.getPayerName())
                                .receiverName(msg.getReceiverName())
                                .session(session)
                                .text(msg.getText())
                                .build();
                        broadcast(forward);
                        HostLobbyBridge.emit(forward);
                    }
                    return;
                }
                case JUST_SAY_NO_RESPONSE -> {
                    return;
                }
                case PAYMENT_RESPONSE -> {
                    if (session != null && msg.isAccepted()) {
                        GameSession base = msg.getRequestId() != null ? pendingRequestSnapshots.remove(msg.getRequestId()) : null;
                        if (base != null) {
                            session = base;
                        }
                        Player payer = playerByName(session, msg.getPayerName());
                        Player receiver = playerByName(session, msg.getReceiverName());
                        if (payer != null && receiver != null) {
                            PaymentService.applyChosenPayment(payer, receiver, msg.getSelectedCards(), session);
                            gameEngine.resumeSession(session);
                            broadcastSessionSnapshot();
                        }
                    }
                    return;
                }
                case PLAYER_ACTION -> {
                    if (msg.getSession() != null && isValidTurnProgression(msg.getPlayerName(), msg.getSession())) {
                        session = msg.getSession();
                        gameEngine.resumeSession(session);
                        System.out.println("[GameServer] accepted PLAYER_ACTION from " + msg.getPlayerName()
                                + ", current=" + (session.getCurrentPlayer() != null ? session.getCurrentPlayer().getName() : "?")
                                + ", phase=" + session.getGameState().getPhase());
                        broadcastSessionSnapshot();
                        return;
                    }
                    if (session != null) {
                        System.err.println("[GameServer] rejected PLAYER_ACTION from " + msg.getPlayerName()
                                + ", authoritativeCurrent=" + (session.getCurrentPlayer() != null ? session.getCurrentPlayer().getName() : "?"));
                        conn.send(NetworkMessage.builder(NetworkMessage.Type.SESSION_SNAPSHOT)
                                .roomId(roomId)
                                .text("SESSION_SNAPSHOT")
                                .players(players)
                                .port(port)
                                .session(session)
                                .build());
                    }
                    return;
                }
                case DISCONNECT -> clients.remove(conn);
                default -> { }
            }
            broadcast(snapshot(this));
        }

        private static boolean removeJustSayNoFromPlayer(Player player, GameSession session) {
            for (var c : new ArrayList<>(player.getHand().getCards())) {
                if (c instanceof com.example.monopoly_deal_game.model.cards.ActionCardJustSayNo) {
                    if (player.getHand().removeCard(c)) {
                        session.discardCard(c);
                        return true;
                    }
                }
            }
            for (var c : new ArrayList<>(player.getBank().getCards())) {
                if (c instanceof com.example.monopoly_deal_game.model.cards.ActionCardJustSayNo) {
                    if (player.getBank().removeCard(c)) {
                        session.discardCard(c);
                        return true;
                    }
                }
            }
            return false;
        }

        private static Player playerByName(GameSession session, String name) {
            if (session == null || name == null) return null;
            for (Player p : session.getPlayers()) {
                if (p != null && name.equals(p.getName())) return p;
            }
            return null;
        }

        private boolean isValidTurnProgression(String playerName, GameSession candidate) {
            if (playerName == null || playerName.isBlank() || session == null || candidate == null) {
                return false;
            }
            Player authoritativeCurrent = session.getCurrentPlayer();
            if (authoritativeCurrent == null || !playerName.equals(authoritativeCurrent.getName())) {
                return false;
            }
            if (candidate.getPlayers().size() != session.getPlayers().size()) {
                return false;
            }
            for (int i = 0; i < session.getPlayers().size(); i++) {
                if (!session.getPlayers().get(i).getName().equals(candidate.getPlayers().get(i).getName())) {
                    return false;
                }
            }
            return true;
        }

        private void broadcast(NetworkMessage msg) { for (ClientConnection c : new ArrayList<>(clients)) c.send(msg); }

        private void pushRoomState() {
            broadcast(snapshot(this));
        }

        private static NetworkMessage snapshot(Room room) {
            return NetworkMessage.builder(NetworkMessage.Type.ROOM_STATE)
                    .roomId(room.roomId).players(room.players).maxPlayers(room.maxPlayers).port(room.port)
                    .text((room.ready ? "READY" : "LOBBY") + "|" + room.hostName).session(room.session).build();
        }
    }

    public static final class ClientConnection {
        private final Socket socket; private final ObjectOutputStream out; private final ObjectInputStream in; private final Room room;
        public ClientConnection(Socket socket, Room room) throws Exception { this.socket = socket; this.room = room; this.out = new ObjectOutputStream(socket.getOutputStream()); this.in = new ObjectInputStream(socket.getInputStream()); }
        private void start() {
            Thread reader = new Thread(() -> {
                try { while (!socket.isClosed()) { Object obj = in.readObject(); if (obj instanceof NetworkMessage msg) room.handleMessage(this, msg); } } catch (Exception ex) {
                    System.err.println("[GameServer] client reader failed: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                    ex.printStackTrace(System.err);
                }
            }, "room-client-reader"); reader.setDaemon(true); reader.start();
        }
        public void send(NetworkMessage msg) {
            try {
                out.reset();
                out.writeObject(msg);
                out.flush();
            } catch (Exception ex) {
                System.err.println("[GameServer] send failed: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                ex.printStackTrace(System.err);
                close();
            }
        }
        public void close() { try { socket.close(); } catch (Exception ignored) { } }
    }
}
