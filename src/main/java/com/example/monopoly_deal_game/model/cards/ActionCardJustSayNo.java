package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.ActionState;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

/**
 * Just Say No action card — cancels an opponent's action card effect against you.
 */
public class ActionCardJustSayNo extends ActionCard {

    public ActionCardJustSayNo(int id, String name, int value) {
        super(id, name, value, false); // JSN does NOT count toward play limit
    }

    @Override public boolean isOnlyBankable() { return true; }

    @Override
    public String getImageFileName() { return "justSayNo.png"; }

    @Override
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) { return true; }

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