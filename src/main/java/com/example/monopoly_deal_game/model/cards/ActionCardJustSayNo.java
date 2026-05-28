package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.ActionState;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

public class ActionCardJustSayNo extends ActionCard {

    public ActionCardJustSayNo(int id, String name, int value) {
        super(id, name, value, false); // JSN does NOT count toward play limit
    }

    @Override
    public void doPlay(Player player, GameSession session, CardPlayOptions options) {
        GameState gs = session.getGameState();
        ActionState as = gs.getActionState();
        if (as != null && as != gs.getTurnState()) {
            as.refuse(player, as.getActionOwner());
        }
        session.discardCard(this);
    }
}