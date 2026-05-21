package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.MonopolyDealOfficialDeck;

import java.util.List;

/**
 * 牌库工厂（设计图 CardFactory）。
 */
public class CardFactory {

    /** 标准 110 张牌库；开局时由 {@link GameLogic#initGame} 滤除 4 张规则说明书卡。 */
    public List<Card> createFullDeck() {
        return MonopolyDealOfficialDeck.createFullDeck();
    }
}
