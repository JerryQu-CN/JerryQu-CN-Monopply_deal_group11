package com.example.monopoly_deal_game.model.collection;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Money/cards deposited in the bank (revised version from the team Player class); display and deposit rules are triggered by logic/UI. */
public final class Bank implements Serializable {
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
