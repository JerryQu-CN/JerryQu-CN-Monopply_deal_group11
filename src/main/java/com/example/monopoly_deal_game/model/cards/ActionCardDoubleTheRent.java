package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

public class ActionCardDoubleTheRent extends ActionCard {

    public ActionCardDoubleTheRent(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        GameState st = session.getGameState();
        st.setDoubleNextRent(true);
        st.setDoubleRentCount(st.getDoubleRentCount() + 1);
        session.discardCard(this);
    }
}