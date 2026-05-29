package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.MonopolyDealOfficialDeck;

import java.util.List;

/**
 * Card deck factory (blueprint CardFactory).
 */
public class CardFactory {

    /** Standard 110-card deck; the 4 rule cards are filtered out at game start by {@link GameLogic#initGame}. */
    public List<Card> createFullDeck() {
        return MonopolyDealOfficialDeck.createFullDeck();
    }
}
