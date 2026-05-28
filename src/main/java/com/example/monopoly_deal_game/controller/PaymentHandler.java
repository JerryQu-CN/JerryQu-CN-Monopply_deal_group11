package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.JustSayNoMediator;
import com.example.monopoly_deal_game.logic.PaymentPickerMediator;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkMessage;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PaymentHandler {
    private final Stage stage;
    private final Consumer<String> feedback;
    private final NetworkSyncHelper networkSync;

    public PaymentHandler(Stage stage, Consumer<String> feedback,
                          NetworkSyncHelper networkSync) {
        this.stage = stage;
        this.feedback = feedback;
        this.networkSync = networkSync;
    }

    public void installIntoMediator() {
        PaymentPickerMediator.installUi(this::choosePaymentCards);
    }

    public void uninstallFromMediator() {
        PaymentPickerMediator.clearUi();
    }

    public void handleRequestMessage(NetworkMessage msg) {
        Platform.runLater(() -> {
            String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
            if (localName == null || !localName.equals(msg.getPayerName())) return;

            GameSession session = msg.getSession() != null
                    ? msg.getSession()
                    : AppContext.get().gameEngine().getCurrentSession();
            if (session == null) return;

            Player payer = session.findPlayerByName(msg.getPayerName());
            Player receiver = session.findPlayerByName(msg.getReceiverName());
            if (payer == null || receiver == null) return;

            if (JustSayNoMediator.tryBlockAgainstPlayer(payer, receiver, session, msg.getText())) {
                networkSync.sendPaymentResponse(msg, localName, payer, receiver, List.of(), false);
                return;
            }

            var picked = PaymentPickDialogs.choosePaymentCards(
                    stage, payer, msg.getAmountM(), receiver, session, msg.getText());
            networkSync.sendPaymentResponse(msg, localName, payer, receiver,
                    picked.orElse(List.of()), picked.isPresent());
        });
    }

    private Optional<List<Card>> choosePaymentCards(
            Player payer, int amountDueM, Player receiver,
            GameSession session, String reasonText) {
        if (payer == null) return Optional.empty();

        String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
        String roomId = AppContext.get().networkLobbyState().getRoomId();

        // Only forward to remote clients when there are actual remote clients connected.
        // In a single-machine game, show the dialog locally regardless of which player is the payer.
        boolean shouldForward = false;
        if (roomId != null && !roomId.isBlank()
                && localName != null && !localName.isBlank()
                && !localName.equals(payer.getName())) {
            if (AppContext.get().networkLobbyState().isHost()) {
                GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
                shouldForward = room != null && room.hasRemoteClients();
            } else {
                shouldForward = true;
            }
        }
        if (shouldForward) {
            if (AppContext.get().networkLobbyState().isHost()) {
                GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
                if (room != null) {
                    room.broadcastPaymentRequest(
                            UUID.randomUUID().toString(), payer, receiver,
                            amountDueM, session, reasonText);
                }
            } else {
                networkSync.sendPaymentRequest(roomId, UUID.randomUUID().toString(),
                        payer, receiver, amountDueM, session, reasonText);
            }
            return Optional.empty();
        }

        return PaymentPickDialogs.choosePaymentCards(
                stage, payer, amountDueM, receiver, session, reasonText);
    }

}