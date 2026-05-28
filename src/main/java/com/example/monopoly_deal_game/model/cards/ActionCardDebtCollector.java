package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PaymentService;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.model.Player;

public class ActionCardDebtCollector extends ActionCard {

    public ActionCardDebtCollector(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        Player victim = PlayEligibility.resolvedActionTarget(player, session, options);
        if (victim != null) {
            ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(player, victim,
                    player.getName() + " used Debt Collector against " + victim.getName());
            state.setOnAccepted(target -> {
                String label = player.getName() + " plays Debt Collector: pay 5M.";
                PaymentService.payFromTo(target, player, 5, session, player, label);
            });
            session.getGameState().addActionState(state);
        }
        session.discardCard(this);
    }
}