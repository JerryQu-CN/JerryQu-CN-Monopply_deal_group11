package com.example.monopoly_deal_game.logic.payment;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.model.Player;

/**
 * Encapsulates payment parameters — payer, receiver, amount, session, and description.
 */
public record PaymentRequest(Player payer, Player receiver, int amountM,
                              GameSession session, String description) {

    public PaymentRequest {
        if (payer == null) throw new NullPointerException("payer");
        if (session == null) throw new NullPointerException("session");
    }
}
