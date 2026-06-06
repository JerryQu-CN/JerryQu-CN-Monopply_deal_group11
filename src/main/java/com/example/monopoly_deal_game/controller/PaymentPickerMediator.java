package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.logic.payment.PaymentRequest;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.List;
import java.util.Optional;

/**
 * Payment request bridge: in local hot-seat mode, directly opens the payer's selection window;
 * in network mode, the UI bridge can forward the request to the payer's client.
 */
public final class PaymentPickerMediator {

    @FunctionalInterface
    public interface Ui {
        Optional<List<Card>> chooseCardsToPay(PaymentRequest req);
    }

    private static volatile Ui ui;

    private PaymentPickerMediator() {}

    public static void installUi(Ui bridge) {
        ui = bridge;
    }

    public static void clearUi() {
        ui = null;
    }

    public static Ui getUiOrNull() {
        return ui;
    }
}
