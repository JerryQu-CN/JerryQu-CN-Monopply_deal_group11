package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.logic.CardImageMapper;
import com.example.monopoly_deal_game.model.cards.Card;

/**
 * 将领域模型 {@link Card} 映射到资源目录 {@code images/cards} 下的 PNG 文件名。
 */
public final class CardFaceResolver {

    private CardFaceResolver() {}

    public static String imageFileFor(Card card) {
        return CardImageMapper.imageFileFor(card);
    }
}
