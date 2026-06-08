package com.example.monopoly_deal_game.controller.dialog;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.payment.PaymentRequest;
import com.example.monopoly_deal_game.logic.payment.PaymentService;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkMessage;
import com.example.monopoly_deal_game.controller.NetworkSyncHelper;
import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.controller.PaymentPickerMediator;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages payment-request dialogs and network forwarding — collects payment
 * selections and submits them to the payment service.
 */
public class PaymentHandler {
    private final Stage stage;
    private final Consumer<String> feedback;
    private final NetworkSyncHelper networkSync;
    private final AtomicBoolean dialogBusy;
    private final BiConsumer<String, List<Integer>> paymentCardSaver;

    public PaymentHandler(Stage stage, Consumer<String> feedback,
                          NetworkSyncHelper networkSync,
                          AtomicBoolean dialogBusy,
                          BiConsumer<String, List<Integer>> paymentCardSaver) {
        this.stage = stage;
        this.feedback = feedback;
        this.networkSync = networkSync;
        this.dialogBusy = dialogBusy;
        this.paymentCardSaver = paymentCardSaver;
    }

    public void installIntoMediator() {
        PaymentPickerMediator.installUi(this::choosePaymentCards);
        PaymentService.setManualPaymentPicker(this::choosePaymentCards);
    }

    public void uninstallFromMediator() {
        PaymentPickerMediator.clearUi();
        PaymentService.setManualPaymentPicker(null);
    }

    public void handleRequestMessage(NetworkMessage msg) {
        Platform.runLater(() -> {
            String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
            if (localName == null || !localName.equals(msg.getPayerName())) return;

            GameSession session = AppContext.get().gameEngine().getCurrentSession();
            if (session == null) return;

            Player payer = session.findPlayerByName(msg.getPayerName());
            Player receiver = session.findPlayerByName(msg.getReceiverName());
            if (payer == null || receiver == null) return;

            PaymentRequest req = new PaymentRequest(payer, receiver, msg.getAmountM(), session, msg.getText());
            var picked = this.choosePaymentCards(req);
            networkSync.sendPaymentResponse(msg, localName, payer, receiver,
                    picked.orElse(List.of()), picked.isPresent());
        });
    }

    /**
     * Show the payment picker dialog for the local payer, guarded by
     * {@code dialogBusy} to prevent nested dialogs.  Selected card IDs are
     * saved via {@code paymentCardSaver} so they can be replayed on a deferred
     * snapshot if one arrives while the dialog is open.
     */
    private Optional<List<Card>> choosePaymentCards(PaymentRequest req) {
        if (req.payer() == null) return Optional.empty();

        Player local = AppContext.get().gameEngine().getCurrentSession().localPlayer(
                AppContext.get().networkLobbyState().getLocalPlayerName());
        if (NetworkSyncHelper.shouldForwardToRemote(local, req.payer())) {
            String roomId = AppContext.get().networkLobbyState().getRoomId();
            if (AppContext.get().networkLobbyState().isHost()) {
                GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
                if (room != null) {
                    room.broadcastPaymentRequest(UUID.randomUUID().toString(), req);
                }
            } else {
                networkSync.sendPaymentRequest(roomId, UUID.randomUUID().toString(), req);
            }
            return Optional.empty();
        }

        if (dialogBusy.get()) return Optional.empty();
        dialogBusy.set(true);
        try {
            Optional<List<Card>> result = PaymentPickDialogs.choosePaymentCards(stage, req);
            result.ifPresent(cards -> {
                if (req.receiver() != null && paymentCardSaver != null) {
                    paymentCardSaver.accept(req.receiver().getName(),
                            cards.stream().map(Card::getId).collect(Collectors.toList()));
                }
            });
            return result;
        } finally {
            dialogBusy.set(false);
        }
    }

}