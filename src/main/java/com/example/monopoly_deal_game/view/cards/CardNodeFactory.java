package com.example.monopoly_deal_game.view.cards;

import com.example.monopoly_deal_game.model.ActionCard;
import com.example.monopoly_deal_game.model.BankCard;
import com.example.monopoly_deal_game.model.Card;
import com.example.monopoly_deal_game.model.PropertyCard;

/**
 * 从领域模型生成对应 {@link AbstractCardNode}，集中 {@code instanceof}，避免散落。
 */
public final class CardNodeFactory {

    private CardNodeFactory() {}

    public static AbstractCardNode from(Card card) {
        return switch (card) {
            case PropertyCard pc -> {
                var n = new PropertyCardNode();
                n.renderFrom(pc);
                yield n;
            }
            case ActionCard ac -> {
                var n = new ActionCardNode();
                n.renderFrom(ac);
                yield n;
            }
            case BankCard bc -> {
                var n = new BankCardNode();
                n.renderFrom(bc);
                yield n;
            }
            default -> throw new IllegalArgumentException("Unknown card type: " + card.getClass());
        };
    }
}
