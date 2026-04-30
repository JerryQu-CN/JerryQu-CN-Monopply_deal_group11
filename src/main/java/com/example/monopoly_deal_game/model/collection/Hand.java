package com.example.monopoly_deal_game.model.collection;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 玩家手牌区（数据结构由组员 Player 改版引入）；具体摸牌逻辑在 logic 层。 */
public final class Hand {

    private final List<Card> cards = new ArrayList<>();
    private Player owner;

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public void addCard(Card card) {
        if (card != null) {
            cards.add(card);
        }
    }

    public boolean removeCard(Card card) {
        return cards.remove(card);
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
