package com.example.monopoly_deal_game.model.collection;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 玩家手牌区（数据结构由组员 Player 改版引入）；具体摸牌逻辑在 logic 层。 */
public final class Hand implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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

    /** 与 {@link #addCard(Card)} 同义，供 {@link GameLogic} 与分支习惯用法调用。 */
    public void add(Card card) {
        addCard(card);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
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
