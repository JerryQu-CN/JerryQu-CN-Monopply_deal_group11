package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardManager;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

/**
 * Pass Go action card — draws two extra cards from the draw pile.
 */
public class ActionCardPassGo extends ActionCard {

    public ActionCardPassGo(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public String getImageFileName() { return "passGo.png"; }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) { return true; }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        for (int i = 0; i < 2; i++) {
            Card d = CardManager.drawOne(session);
            if (d != null) player.getHand().addCard(d);
        }
        session.discardCard(this);
    }
}