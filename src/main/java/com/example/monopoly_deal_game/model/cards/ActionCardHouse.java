package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;

/**
 * House building action card — placed on a full set for a +3 rent bonus.
 */
public class ActionCardHouse extends ActionCard {

    public ActionCardHouse(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override public boolean isBuilding() { return true; }
    @Override public boolean isHouse() { return true; }
    @Override public int getBuildingRentBonus() { return 3; }

    @Override
    public String getImageFileName() { return "house.png"; }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) {
        return PropertyQuery.findHouseTarget(actor, true) != null;
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        Property row = PropertyQuery.findHouseTarget(player, true);
        if (row == null || !row.addBuildingCard(this)) {
            session.discardCard(this);
        }
    }
}