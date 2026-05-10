```java
package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.GameSession;

import java.util.Collections;
import java.util.List;

public class CardManager {

    public void shuffleDrawPileFromDiscard(GameSession session) {
        List<Card> discardPile = session.getDiscardPile();
        List<Card> drawPile = session.getDrawPile();
        
        if (discardPile == null || discardPile.isEmpty()) {
            return;
        }
        
        drawPile.addAll(discardPile);
        discardPile.clear();
        Collections.shuffle(drawPile);
    }
    
    public Card drawOne(GameSession session) {
        List<Card> drawPile = session.getDrawPile();
        List<Card> discardPile = session.getDiscardPile();
        
        if (drawPile == null || drawPile.isEmpty()) {
            if (discardPile != null && !discardPile.isEmpty()) {
                shuffleDrawPileFromDiscard(session);
            } else {
                return null;
            }
        }
        
        if (drawPile.isEmpty()) {
            return null;
        }
        
        return drawPile.remove(drawPile.size() - 1);
    }
    
    public List<Card> drawMultiple(GameSession session, int cardCount) {
        List<Card> drawnCards = new java.util.ArrayList<>();
        
        for (int i = 0; i < cardCount; i++) {
            Card card = drawOne(session);
            if (card == null) {
                break;
            }
            drawnCards.add(card);
        }
        
        return drawnCards;
    }
    
    public void discardCard(GameSession session, Card card) {
        if (card == null) {
            return;
        }
        List<Card> discardPile = session.getDiscardPile();
        if (discardPile != null) {
            discardPile.add(card);
        }
    }
    
    public void discardCards(GameSession session, List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        List<Card> discardPile = session.getDiscardPile();
        if (discardPile != null) {
            discardPile.addAll(cards);
        }
    }
}
```
