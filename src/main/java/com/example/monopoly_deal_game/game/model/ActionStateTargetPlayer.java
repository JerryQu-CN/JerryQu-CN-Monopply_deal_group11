package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;

/**
 * State where the actionOwner is waiting to select a target player.
 * Subclasses override playerSelected() to decide what to do upon selection.
 */
public abstract class ActionStateTargetPlayer extends ActionState {
    @Serial
    private static final long serialVersionUID = 1L;

    public ActionStateTargetPlayer(Player actionOwner, String status) {
        super(actionOwner, status);
    }

    public ActionStateTargetPlayer(Player actionOwner) {
        super(actionOwner);
    }

    /**
     * Called when the action owner selects a target player.
     */
    public abstract void playerSelected(Player target);

    @Override
    public boolean isFinished() {
        return false; // Subclasses must explicitly transition
    }
}