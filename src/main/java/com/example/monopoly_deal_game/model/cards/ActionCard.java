package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

public abstract class ActionCard extends Card {

    private final boolean countsTowardLimit;

    protected ActionCard(int id, String name, int value, boolean countsTowardLimit) {
        super(id, name, value, "Action Card: " + name);
        this.countsTowardLimit = countsTowardLimit;
    }

    @Override
    public CardType getCardType() {
        return CardType.ACTION;
    }

    @Override
    public boolean isCountsTowardLimit() {
        return countsTowardLimit;
    }

    /** Execute this action card's effect. Called by GameLogic after removing card from hand. */
    public abstract void doPlay(Player player, GameSession session, CardPlayOptions options);
}