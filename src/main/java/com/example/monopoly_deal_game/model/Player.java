package com.example.monopoly_deal_game.model;

/**
 * 玩家（人类或机器人统一抽象）。
 *
 * TODO(model): {@code PlayerHand}、{@code PlayerBank}、{@code PropertyArea} 可拆内部类或独立类型；
 * 需求 2（回合开始手牌数）、3（出牌0–3、Just Say No 不计数）、7（支付租金）均依赖本结构。
 */
public class Player {

    // TODO(model): id、显示名、是否本地主控、手牌/银行/物业容器

    public boolean isComputer() {
        return false;
    }
}
