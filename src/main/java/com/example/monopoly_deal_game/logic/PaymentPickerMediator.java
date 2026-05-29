package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
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
        /**
         * @return the table assets selected by the payer; Optional.empty means the current
         *         endpoint cannot handle the request or the player cancelled.
         */
        Optional<List<Card>> chooseCardsToPay(
                Player payer, int amountDueM, Player receiver, GameSession session, String reasonText);
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
