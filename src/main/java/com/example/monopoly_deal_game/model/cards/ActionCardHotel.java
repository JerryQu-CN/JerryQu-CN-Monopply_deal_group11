package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;

/**
 * Hotel building action card — placed on a full set with a house for a +4 rent bonus.
 */
public class ActionCardHotel extends ActionCard {

    public ActionCardHotel(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override public boolean isBuilding() { return true; }
    @Override public boolean isHotel() { return true; }
    @Override public int getBuildingRentBonus() { return 4; }

    @Override
    public String getImageFileName() { return "hotel.png"; }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) {
        return PropertyQuery.findHouseTarget(actor, false) != null;
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        Property row = PropertyQuery.findHouseTarget(player, false);
        if (row == null || !row.addBuildingCard(this)) {
            session.discardCard(this);
        }
    }
}