package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.game.model.GameSession;

import java.util.List;

/**
 * 抽牌堆 / 弃牌堆 / 洗牌回补（需求 1.5、2.4）。
 *
 * TODO(logic): 抽牌中途耗尽则暂停→洗弃牌→继续摸完应摸张数；保证无牌丢失（可靠性需求）。
 */
public class CardManager {

    public void shuffleDrawPileFromDiscard(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): 弃牌洗回抽牌堆");
    }

    /**
     * 需求 1.1: 初始化牌堆。
     * @param cards 由 CardFactory 生成的全量卡牌列表
     */
    public void initDeck(List<Card> cards) {
        // TODO: 实现洗牌并存入 drawPile
    }

    /**
     * 需求 2: 抽取单张卡牌。
     * @param session 用于获取当前对局上下文（如牌堆耗尽需重洗弃牌堆）
     * @return 抽到的卡牌
     */
    public Card drawOne(GameSession session) {
        // TODO: 实现摸牌逻辑
        return null;
    }
}
