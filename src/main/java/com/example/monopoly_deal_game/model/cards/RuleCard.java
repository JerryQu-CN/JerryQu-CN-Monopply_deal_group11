package com.example.monopoly_deal_game.model.cards;

/**
 * Rule reference card: the official Monopoly Deal deck contains 4 copies; some tabletop games remove them from the deck at startup.
 * In digital implementations they may remain in the deck or be filtered at start; they do not count toward the 3-card play limit per turn.
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
