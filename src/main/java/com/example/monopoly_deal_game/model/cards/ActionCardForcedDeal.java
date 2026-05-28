package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.logic.PropertyPlayHelper;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;

public class ActionCardForcedDeal extends ActionCard {

    public ActionCardForcedDeal(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        PropertyCard mine = options != null ? options.sourcePropertyCard() : null;
        if (mine == null) mine = PropertyQuery.firstTableProperty(player);
        Player opp = PlayEligibility.resolvedActionTarget(player, session, options);
        PropertyCard theirs = options != null ? options.targetPropertyCard() : null;
        if (theirs == null && opp != null) theirs = PropertyQuery.firstTableProperty(opp);
        if (mine == null || opp == null || theirs == null) {
            session.discardCard(this);
            return;
        }
        ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(player, opp,
                player.getName() + " used Forced Deal against " + opp.getName());
        final Player o = opp;
        final PropertyCard m = mine;
        final PropertyCard t = theirs;
        state.setOnAccepted(target -> {
            PropertyPlayHelper.removePropertyCardFromBoard(player, m, session);
            PropertyPlayHelper.removePropertyCardFromBoard(o, t, session);
            PropertyPlayHelper.placePropertyCard(player, t);
            PropertyPlayHelper.placePropertyCard(o, m);
        });
        session.getGameState().addActionState(state);
        session.discardCard(this);
    }
}