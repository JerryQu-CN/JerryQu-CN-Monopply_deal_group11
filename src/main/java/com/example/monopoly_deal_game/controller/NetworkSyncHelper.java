package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkMessage;

import java.util.List;
import java.util.function.Consumer;

public class NetworkSyncHelper {
    private final Consumer<String> feedback;

    public NetworkSyncHelper(Consumer<String> feedback) {
        this.feedback = feedback;
    }

    public void publishSessionChange(GameSession session) {
        if (session == null) return;
        AppContext.get().networkLobbyState().setSession(session);
        String roomId = AppContext.get().networkLobbyState().getRoomId();
        if (roomId == null || roomId.isBlank()) return;
        if (AppContext.get().networkLobbyState().isHost()) {
            GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
            if (room != null) room.broadcastSessionSnapshot();
            return;
        }
        var client = AppContext.get().networkLobbyState().getClient();
        if (client == null) {
            feedback.accept("同步失败：未连接到房主。请确认从 Join Room 页面加入房间。");
            return;
        }
        try {
            client.send(NetworkMessage.builder(NetworkMessage.Type.PLAYER_ACTION)
                    .roomId(roomId)
                    .playerName(AppContext.get().networkLobbyState().getLocalPlayerName())
                    .session(session)
                    .build());
            feedback.accept("操作已提交，等待房主同步...");
        } catch (Exception ex) {
            feedback.accept("同步操作失败：" + ex.getMessage());
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
                .build(), "发送支付响应失败");
    }

    public void sendPaymentRequest(String roomId, String requestId,
                                    Player payer, Player receiver,
                                    int amountM, GameSession session, String reason) {
        send(NetworkMessage.builder(NetworkMessage.Type.PAYMENT_REQUEST)
                .roomId(roomId)
                .requestId(requestId)
                .payerName(payer.getName())
                .receiverName(receiver != null ? receiver.getName() : null)
                .amountM(amountM)
                .session(session)
                .text(reason)
                .build(), "发送支付请求失败");
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
                .build(), "发送 Just Say No 请求失败");
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
                .build(), "发送反对响应失败");
    }

    private void send(NetworkMessage msg, String errorPrefix) {
        try {
            var client = AppContext.get().networkLobbyState().getClient();
            if (client != null) client.send(msg);
        } catch (Exception ex) {
            feedback.accept(errorPrefix + "：" + ex.getMessage());
        }
    }
}