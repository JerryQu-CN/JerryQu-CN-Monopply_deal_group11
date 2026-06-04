package com.example.monopoly_deal_game.model.cards;

/**
 * Rule reference card from the official deck — does not count toward the play limit per turn.
 */
public final class RuleCard extends Card {

    public RuleCard(int id, String title, String description) {
        super(id, title, 0, description);
    }

    @Override
    public CardType getCardType() {
        return CardType.RULE;
    }

    @Override
    public boolean isCountsTowardLimit() {
        return false;
    }

}
