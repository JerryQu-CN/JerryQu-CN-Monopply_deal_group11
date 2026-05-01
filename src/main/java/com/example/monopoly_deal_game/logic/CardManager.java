package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.game.model.GameSession;

/**
 * 抽牌堆 / 弃牌堆 / 洗牌回补（需求 1.5、2.4）。
 *
 * TODO(logic): 抽牌中途耗尽则暂停→洗弃牌→继续摸完应摸张数；保证无牌丢失（可靠性需求）。
 */
public class CardManager {

    public void shuffleDrawPileFromDiscard(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): 弃牌洗回抽牌堆");
    }

    public Card drawOne(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): drawOne");
    }
}
