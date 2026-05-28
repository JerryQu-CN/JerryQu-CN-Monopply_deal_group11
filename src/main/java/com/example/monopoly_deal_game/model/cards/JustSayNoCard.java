package com.example.monopoly_deal_game.model.cards;

/**
 * Just Say No 牌：被动响应牌，当对手对你打出行动牌时可选择使用，抵消该行动牌效果。
 * 使用后本牌进入弃牌堆。主动回合只能存入银行。
 * @deprecated Use {@link ActionCardJustSayNo} directly instead.
 */
@Deprecated
public class JustSayNoCard extends ActionCardJustSayNo {



    public JustSayNoCard(int id) {
        super(id, "Just Say No", 4);
    }
}