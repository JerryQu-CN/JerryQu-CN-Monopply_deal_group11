package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.logic.PropertyPlayHelper;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Forced Deal action card — swaps a property card with an opponent.
 */
public class ActionCardForcedDeal extends ActionCard {

    public ActionCardForcedDeal(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public String getImageFileName() { return "forcedDeal.png"; }

    @Override
    public boolean needsChosenOpponent() { return true; }
    @Override
    public SubTarget subTarget() { return SubTarget.FORCED_DEAL_PROPERTIES; }

    @Override
    public List<Player> eligibleOpponents(Player actor, GameSession session) {
        if (PropertyQuery.firstTableProperty(actor) == null) return List.of();
        List<Player> ok = new ArrayList<>();
        for (Player o : otherPlayers(actor, session)) {
            if (PropertyQuery.firstTableProperty(o) != null) ok.add(o);
        }
        return ok;
    }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) {
        if (PropertyQuery.firstTableProperty(actor) == null) return false;
        Player opp = PlayEligibility.resolvedActionTarget(actor, session, opt);
        return opp != null && PropertyQuery.firstTableProperty(opp) != null;
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
        final Player o = opp;
        final PropertyCard m = mine;
        final PropertyCard t = theirs;
        pushTargetedState(player, opp, "Forced Deal", session, target -> {
            PropertyPlayHelper.removePropertyCardFromBoard(player, m, session);
            PropertyPlayHelper.removePropertyCardFromBoard(o, t, session);
            PropertyPlayHelper.placePropertyCard(player, t);
            PropertyPlayHelper.placePropertyCard(o, m);
        });
    }
}