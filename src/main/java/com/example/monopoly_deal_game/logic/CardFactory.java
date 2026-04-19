package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.Card;

import java.util.List;

/**
 * 牌库工厂（设计图 CardFactory）。
 *
 * TODO(logic): createPropertyCard / createActionCard / createBankCard；生成完整牌列表供 {@link GameLogic#initGame} 洗牌。
 */
public class CardFactory {

    public List<Card> createFullDeck() {
        throw new UnsupportedOperationException("TODO(logic): 完整牌表");
    }
}
