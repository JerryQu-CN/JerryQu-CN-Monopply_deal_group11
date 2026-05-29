package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Draw pile / discard pile / reshuffle refill (requirements 1.5, 2.4).
 */
public class CardManager {

    /** Shuffle all cards and place them into the session's draw pile. */
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
     * Draw one card: if the draw pile is empty, first shuffle the discard pile back in;
     * if still empty, return {@code null}.
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
