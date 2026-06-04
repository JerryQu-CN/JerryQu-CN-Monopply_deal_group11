package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.payment.PaymentRequest;
import com.example.monopoly_deal_game.logic.payment.PaymentService;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.model.Player;

import java.util.List;

/**
 * Debt Collector action card — forces a chosen opponent to pay 5M.
 */
public class ActionCardDebtCollector extends ActionCard {

    public ActionCardDebtCollector(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public String getImageFileName() { return "debtCollector.png"; }

    @Override
    public boolean needsChosenOpponent() { return true; }

    @Override
    public List<Player> eligibleOpponents(Player actor, GameSession session) {
        return otherPlayers(actor, session);
    }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) {
        return PlayEligibility.resolvedActionTarget(actor, session, opt) != null;
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        Player victim = PlayEligibility.resolvedActionTarget(player, session, options);
        if (victim != null) {
            pushTargetedState(player, victim, "Debt Collector", session, target -> {
                String label = player.getName() + " plays Debt Collector: pay 5M.";
                PaymentService.payFromTo(new PaymentRequest(target, player, 5, session, label));
            });
        } else {
            session.discardCard(this);
        }
    }
}