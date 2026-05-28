package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.logic.PropertyPlayHelper;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;

import java.util.ArrayList;
import java.util.List;

public class ActionCardDealBreaker extends ActionCard {

    public ActionCardDealBreaker(int id, String name, int value) {
        super(id, name, value, true);
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
        ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(player, tp,
                player.getName() + " used Deal Breaker against " + tp.getName());
        state.setOnAccepted(target -> {
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
        session.getGameState().addActionState(state);
        session.discardCard(this);
    }
}