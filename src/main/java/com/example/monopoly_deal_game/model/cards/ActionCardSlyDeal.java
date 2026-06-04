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
 * Sly Deal action card — steals a single non-monopoly property card from an opponent.
 */
public class ActionCardSlyDeal extends ActionCard {

    public ActionCardSlyDeal(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public String getImageFileName() { return "slyDeal.png"; }

    @Override
    public boolean needsChosenOpponent() { return true; }
    @Override
    public SubTarget subTarget() { return SubTarget.STEALABLE_PROPERTY; }

    @Override
    public List<Player> eligibleOpponents(Player actor, GameSession session) {
        List<Player> ok = new ArrayList<>();
        for (Player o : otherPlayers(actor, session)) {
            if (PropertyQuery.firstStealableProperty(o) != null) ok.add(o);
        }
        return ok;
    }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) {
        Player t = PlayEligibility.resolvedActionTarget(actor, session, opt);
        return t != null && PropertyQuery.firstStealableProperty(t) != null;
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
        final Player v = victim;
        final PropertyCard s = stolen;
        pushTargetedState(player, victim, "Sly Deal", session, target -> {
            PropertyPlayHelper.removePropertyCardFromBoard(v, s, session);
            PropertyPlayHelper.placePropertyCard(player, s);
        });
    }
}