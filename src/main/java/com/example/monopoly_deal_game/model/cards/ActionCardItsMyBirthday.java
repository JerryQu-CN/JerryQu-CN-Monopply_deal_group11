package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.ActionStateRent;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.payment.PaymentRequest;
import com.example.monopoly_deal_game.logic.payment.PaymentService;
import com.example.monopoly_deal_game.logic.payment.RentCalculator;
import com.example.monopoly_deal_game.logic.payment.RentRules;
import com.example.monopoly_deal_game.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * It's My Birthday action card — collects 2M from all other players.
 */
public class ActionCardItsMyBirthday extends ActionCard {

    public ActionCardItsMyBirthday(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public String getImageFileName() { return "birthday.png"; }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) { return true; }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        List<Player> targets = new ArrayList<>();
        for (Player p : new ArrayList<>(session.getPlayers())) {
            if (!p.equals(player)) targets.add(p);
        }
        if (!targets.isEmpty()) {
            ActionStateRent state = new ActionStateRent(player, targets, 2);
            state.setOnAccepted(target -> {
                String label = player.getName() + " plays Birthday: pay 2M.";
                PaymentService.payFromTo(new PaymentRequest(target, player, 2, session, label));
            });
            state.setStatus(player.getName() + " played It's My Birthday!");
            session.getGameState().addActionState(state);
        }
        session.discardCard(this);
    }
}