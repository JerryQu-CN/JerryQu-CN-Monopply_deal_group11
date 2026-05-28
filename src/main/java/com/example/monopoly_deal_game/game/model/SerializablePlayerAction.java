package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;
import java.io.Serializable;

@FunctionalInterface
public interface SerializablePlayerAction extends Serializable {
    @Serial
    long serialVersionUID = 1L;

    void execute(Player player);
}