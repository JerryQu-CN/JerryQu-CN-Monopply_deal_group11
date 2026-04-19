package com.example.monopoly_deal_game.model;

/**
 * 所有手牌/牌堆中的卡牌基类（设计图：抽象 Card）。
 *
 * TODO(model): 定义编号、名称、卡面描述、是否计入「每回合 3 张」限制等；
 * {@link #use(Player, Player)} 可仅作钩子，真正结算在 {@link com.example.monopoly_deal_game.logic.CardEffectExecutor}。
 */
public abstract class Card {

    /**
     * @param user   出牌玩家
     * @param target 目标玩家（部分行动牌可为 null）
     */
    public abstract void use(Player user, Player target);
}
