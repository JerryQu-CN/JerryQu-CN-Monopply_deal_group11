package com.example.monopoly_deal_game.game.state;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;

/**
 * Targeted action state — a player is locked by another player's action
 * and may accept or refuse (via Just Say No).
 */
public class ActionStatePlayerTargeted extends ActionState {
    @Serial
    private static final long serialVersionUID = 1L;

    private SerializablePlayerAction onAccepted;

    public ActionStatePlayerTargeted(Player actionOwner, Player actionTarget, String status) {
        super(actionOwner, actionTarget, status);
    }

    public ActionStatePlayerTargeted(Player actionOwner, Player actionTarget) {
        super(actionOwner, actionTarget);
    }

    public void setOnAccepted(SerializablePlayerAction action) {
        this.onAccepted = action;
    }

    public boolean hasOnAccepted() {
        return onAccepted != null;
    }

    /** Execute the post-acceptance business logic (payment, property transfer, etc.). Each target should only be called once. */
    public void executeOnAccepted(Player player) {
        if (onAccepted != null) {
            onAccepted.execute(player);
            onAccepted = null;
        }
    }

    @Override
    public void tryExecuteOnAccepted(Player target) {
        if (hasOnAccepted()) executeOnAccepted(target);
    }
}