package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;

public class ActionCardHotel extends ActionCard {

    public ActionCardHotel(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        Property row = PropertyQuery.findHouseTarget(player, false);
        if (row == null || !row.addBuildingCard(this)) {
            session.discardCard(this);
        }
    }
}