package com.example.monopoly_deal_game.game.state;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;
import java.io.Serializable;

/**
 * Functional interface for deferred post-acceptance action callbacks, serializable for network transfer.
 */
@FunctionalInterface
public interface SerializablePlayerAction extends Serializable {
    @Serial
    long serialVersionUID = 1L;

    void execute(Player player);
}