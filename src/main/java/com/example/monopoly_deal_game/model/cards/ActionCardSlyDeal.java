package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.logic.PropertyPlayHelper;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;

public class ActionCardSlyDeal extends ActionCard {

    public ActionCardSlyDeal(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        Player victim = PlayEligibility.resolvedActionTarget(player, session, options);
        PropertyCard stolen = options != null ? options.targetPropertyCard() : null;
        if (stolen == null && victim != null) stolen = PropertyQuery.firstStealableProperty(victim);
        if (victim == null || stolen == null) {
            session.discardCard(this);
            return;
        }
        ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(player, victim,
                player.getName() + " used Sly Deal against " + victim.getName());
        final Player v = victim;
        final PropertyCard s = stolen;
        state.setOnAccepted(target -> {
            PropertyPlayHelper.removePropertyCardFromBoard(v, s, session);
            PropertyPlayHelper.placePropertyCard(player, s);
        });
        session.getGameState().addActionState(state);
        session.discardCard(this);
    }
}