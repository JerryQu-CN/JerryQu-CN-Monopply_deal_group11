package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.ActionStateRent;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PaymentService;
import com.example.monopoly_deal_game.logic.RentCalculator;
import com.example.monopoly_deal_game.logic.RentRules;
import com.example.monopoly_deal_game.model.Player;

import java.util.ArrayList;
import java.util.List;

public class ActionCardItsMyBirthday extends ActionCard {

    public ActionCardItsMyBirthday(int id, String name, int value) {
        super(id, name, value, true);
    }

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
                PaymentService.payFromTo(target, player, 2, session, player, label);
            });
            state.setStatus(player.getName() + " played It's My Birthday!");
            session.getGameState().addActionState(state);
        }
        session.discardCard(this);
    }
}