package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

public class ActionCardPassGo extends ActionCard {

    public ActionCardPassGo(int id, String name, int value) {
        super(id, name, value, true);
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        var cm = AppContext.get().gameEngine().getGameLogic().getCardManager();
        for (int i = 0; i < 2; i++) {
            Card d = cm.drawOne(session);
            if (d != null) player.getHand().addCard(d);
        }
        session.discardCard(this);
    }
}