package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.model.Player;

/**
 * 规则说明书卡：官方 Monopoly Deal 牌组含 4 张，部分桌游开局会从牌库移除。
 * 数字实现中可留在牌库或开局过滤；不计入每回合 3 张出牌限制。
 */
public final class RuleCard extends Card {

    public RuleCard(int id, String title, String description) {
        super(id, title, 0, description);
    }

    @Override
    public CardType getCardType() {
        return CardType.RULE;
    }

    @Override
    public boolean isCountsTowardLimit() {
        return false;
    }

    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("规则卡不通过通常出牌流程结算；请开局移出或由 logic 定义处理。");
    }
}
