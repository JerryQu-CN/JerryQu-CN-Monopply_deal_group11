package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

/**
 * Double The Rent action card — doubles the amount of the next rent card played.
 */
public class ActionCardDoubleTheRent extends ActionCard {

    public ActionCardDoubleTheRent(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override public boolean isOnlyBankable() { return true; }

    @Override
    public String getImageFileName() { return "doubleTheRent.png"; }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) { return true; }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        GameState st = session.getGameState();
        st.setDoubleNextRent(true);
        st.setDoubleRentCount(st.getDoubleRentCount() + 1);
        session.discardCard(this);
    }
}