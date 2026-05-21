package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 抽牌堆 / 弃牌堆 / 洗牌回补（需求 1.5、2.4）。
 */
public class CardManager {

    /** 将全量卡牌洗牌后放入 session 的摸牌堆。 */
    public void initDeck(GameSession session, List<Card> cards) {
        List<Card> draw = session.getDrawPile();
        draw.clear();
        if (cards != null && !cards.isEmpty()) {
            draw.addAll(cards);
            Collections.shuffle(draw, ThreadLocalRandom.current());
        }
        session.getDiscardPile().clear();
    }

    /**
     * 摸一张：牌堆空则先将弃牌洗回，再摸；仍无牌则返回 {@code null}。
     */
    public Card drawOne(GameSession session) {
        List<Card> draw = session.getDrawPile();
        if (draw.isEmpty()) {
            shuffleDrawPileFromDiscard(session);
        }
        if (draw.isEmpty()) {
            return null;
        }
        return draw.removeLast();
    }

    public void shuffleDrawPileFromDiscard(GameSession session) {
        List<Card> draw = session.getDrawPile();
        List<Card> discard = session.getDiscardPile();
        if (discard.isEmpty()) {
            return;
        }
        draw.addAll(discard);
        discard.clear();
        Collections.shuffle(draw, ThreadLocalRandom.current());
    }
}
