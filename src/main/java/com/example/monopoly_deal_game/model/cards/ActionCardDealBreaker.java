package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.GameConfig;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.logic.PropertyPlayHelper;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Deal Breaker action card — steals a complete monopoly set from an opponent.
 */
public class ActionCardDealBreaker extends ActionCard {

    public ActionCardDealBreaker(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public String getImageFileName() { return "dealBreaker.png"; }

    @Override
    public boolean needsChosenOpponent() { return true; }
    @Override
    public SubTarget subTarget() { return SubTarget.MONOPOLY_GROUP; }

    @Override
    public List<Player> eligibleOpponents(Player actor, GameSession session) {
        List<Player> ok = new ArrayList<>();
        for (Player o : otherPlayers(actor, session)) {
            if (PropertyQuery.firstMonopolyOfPlayer(o) != null) ok.add(o);
        }
        return ok;
    }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) {
        Player t = PlayEligibility.resolvedActionTarget(actor, session, opt);
        return t != null && PropertyQuery.firstMonopolyOfPlayer(t) != null;
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        Player targetPlayer = PlayEligibility.resolvedActionTarget(player, session, options);
        Property complete = options != null ? options.targetPropertyGroup() : null;
        if (complete == null && targetPlayer != null)
            complete = PropertyQuery.firstMonopolyOfPlayer(targetPlayer);
        if (targetPlayer == null || complete == null) {
            session.discardCard(this);
            return;
        }
        final Player tp = targetPlayer;
        final Property comp = complete;
        pushTargetedState(player, tp, "Deal Breaker", session, target -> {
            if (GameConfig.DEAL_BREAKERS_DISCARD_SETS) {
                List<PropertyCard> propCards = new ArrayList<>(comp.getCards());
                for (PropertyCard pc : propCards) {
                    PropertyPlayHelper.removePropertyCardFromBoard(tp, pc, session);
                    session.discardCard(pc);
                }
                List<Card> buildings = comp.takeAllBuildings();
                for (Card b : buildings) session.discardCard(b);
            } else {
                PropertyPlayHelper.transferPropertyGroup(tp, player, comp, session);
            }
        });
    }
}