package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.payment.PaymentRequest;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkMessage;

import java.util.List;
import java.util.function.Consumer;

/**
 * Encapsulates network synchronization — publishes session changes, forwards
 * payment requests, and broadcasts game state to remote clients.
 */
public class NetworkSyncHelper {
    private final Consumer<String> feedback;

    public NetworkSyncHelper(Consumer<String> feedback) {
        this.feedback = feedback;
    }

    public void publishSessionChange(GameSession session) {
        publishSessionChange(session, null);
    }

    public void publishSessionChange(GameSession session, String logText) {
        if (session == null) return;
        AppContext.get().networkLobbyState().setSession(session);
        String roomId = AppContext.get().networkLobbyState().getRoomId();
        if (roomId == null || roomId.isBlank()) return;
        if (AppContext.get().networkLobbyState().isHost()) {
            GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
            if (room != null) {
                if (logText != null && !logText.isBlank()) {
                    room.broadcastSessionSnapshot(logText);
                } else {
                    room.broadcastSessionSnapshot();
                }
            }
            return;
        }
        var client = AppContext.get().networkLobbyState().getClient();
        if (client == null) {
            feedback.accept("Sync failed: not connected to the host. Please join the room from the Join Room page.");
            return;
        }
        try {
            var builder = NetworkMessage.builder(NetworkMessage.Type.PLAYER_ACTION)
                    .roomId(roomId)
                    .playerName(AppContext.get().networkLobbyState().getLocalPlayerName())
                    .session(session);
            if (logText != null && !logText.isBlank()) {
                builder.logText(logText);
            }
            client.send(builder.build());
            feedback.accept("Action submitted, waiting for host sync...");
        } catch (Exception ex) {
            feedback.accept("Sync operation failed: " + ex.getMessage());
        }
    }

    public void sendPaymentResponse(NetworkMessage msg, String localName,
                                     Player payer, Player receiver,
                                     List<Card> cards, boolean accepted) {
        send(NetworkMessage.builder(NetworkMessage.Type.PAYMENT_RESPONSE)
                .roomId(msg.getRoomId())
                .requestId(msg.getRequestId())
                .playerName(localName)
                .payerName(payer.getName())
                .receiverName(receiver.getName())
                .selectedCards(cards)
                .accepted(accepted)
                .build(), "Failed to send payment response");
    }

    public void sendPaymentRequest(String roomId, String requestId, PaymentRequest req) {
        send(NetworkMessage.builder(NetworkMessage.Type.PAYMENT_REQUEST)
                .roomId(roomId)
                .requestId(requestId)
                .payerName(req.payer().getName())
                .receiverName(req.receiver() != null ? req.receiver().getName() : null)
                .amountM(req.amountM())
                .session(req.session())
                .text(req.description())
                .build(), "Failed to send payment request");
    }

    public void sendJustSayNoRequest(String roomId, String requestId,
                                      Player respondent, Player activator,
                                      GameSession session, String situation) {
        send(NetworkMessage.builder(NetworkMessage.Type.JUST_SAY_NO_REQUEST)
                .roomId(roomId)
                .requestId(requestId)
                .payerName(respondent.getName())
                .receiverName(activator != null ? activator.getName() : null)
                .session(session)
                .text(situation)
                .build(), "Failed to send Just Say No request");
    }

    public void sendJustSayNoResponse(NetworkMessage msg, String localName,
                                       Player respondent, Player activator, boolean accepted) {
        send(NetworkMessage.builder(NetworkMessage.Type.JUST_SAY_NO_RESPONSE)
                .roomId(msg.getRoomId())
                .requestId(msg.getRequestId())
                .playerName(localName)
                .payerName(respondent.getName())
                .receiverName(activator != null ? activator.getName() : null)
                .accepted(accepted)
                .build(), "Failed to send decline response");
    }

    private void send(NetworkMessage msg, String errorPrefix) {
        try {
            var client = AppContext.get().networkLobbyState().getClient();
            if (client != null) client.send(msg);
        } catch (Exception ex) {
            feedback.accept(errorPrefix + ": " + ex.getMessage());
        }
    }

    public static boolean hasRemoteClients() {
        String roomId = AppContext.get().networkLobbyState().getRoomId();
        if (roomId == null || roomId.isBlank()) return false;
        if (AppContext.get().networkLobbyState().isHost()) {
            GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
            return room != null && room.hasRemoteClients();
        }
        return true;
    }

    public static boolean shouldForwardToRemote(Player localPlayer, Player targetPlayer) {
        if (localPlayer == null || targetPlayer == null) return false;
        if (localPlayer.getName().equals(targetPlayer.getName())) return false;
        return hasRemoteClients();
    }
}