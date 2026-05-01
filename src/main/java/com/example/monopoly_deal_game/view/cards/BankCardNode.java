package com.example.monopoly_deal_game.view.cards;

import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;

/**
 * 钞票类牌面（面额大字、配色）。
 */
public class BankCardNode extends AbstractCardNode {

    public BankCardNode() {}

    @Override
    public void renderFrom(Card card) {
        if (!(card instanceof BankCard bc)) {
            throw new IllegalArgumentException("Expected BankCard, got " + card.getClass());
        }
        // TODO(view+model): 面额 M 与颜色
    }
}
