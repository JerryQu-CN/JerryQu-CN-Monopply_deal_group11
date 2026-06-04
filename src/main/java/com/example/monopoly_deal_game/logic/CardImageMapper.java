package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.Card;

/**
 * Maps a {@link Card} to a PNG file name under the {@code images/cards} resource directory.
 * Delegates to the polymorphic {@link Card#getImageFileName()} method.
 */
public final class CardImageMapper {

    private CardImageMapper() {}

    public static String imageFileFor(Card card) {
        return card != null ? card.getImageFileName() : "propertyWildCard.png";
    }
}
