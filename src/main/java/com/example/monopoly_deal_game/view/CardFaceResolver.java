package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.logic.CardImageMapper;
import com.example.monopoly_deal_game.model.cards.Card;

/**
 * Maps domain model {@link Card} to PNG file names under the resource directory {@code images/cards}.
 */
public final class CardFaceResolver {

    private CardFaceResolver() {}

    public static String imageFileFor(Card card) {
        return CardImageMapper.imageFileFor(card);
    }
}
