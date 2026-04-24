package com.example.monopoly_deal_game.view.cards;

import com.example.monopoly_deal_game.model.Card;
import com.example.monopoly_deal_game.model.PropertyCard;

/**
 * 物业牌（含双色系 / Wild）牌面。
 */
public class PropertyCardNode extends AbstractCardNode {

    public PropertyCardNode() {}

    @Override
    public void renderFrom(Card card) {
        if (!(card instanceof PropertyCard pc)) {
            throw new IllegalArgumentException("Expected PropertyCard, got " + card.getClass());
        }
        // TODO(view+model): 颜色条、租金档位、整套标记
    }
}
