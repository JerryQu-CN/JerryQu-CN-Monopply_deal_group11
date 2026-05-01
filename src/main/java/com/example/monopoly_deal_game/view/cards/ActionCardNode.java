package com.example.monopoly_deal_game.view.cards;

import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;

/**
 * 行动牌牌面（Rent、Deal Breaker、Just Say No 等共用壳，具体文案由 model 标签区分后绘制）。
 */
public class ActionCardNode extends AbstractCardNode {

    public ActionCardNode() {}

    @Override
    public void renderFrom(Card card) {
        if (!(card instanceof ActionCard ac)) {
            throw new IllegalArgumentException("Expected ActionCard, got " + card.getClass());
        }
        // TODO(view+model): 按行动类型显示标题与规则摘要
    }
}
