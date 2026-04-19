package com.example.monopoly_deal_game.model;

/**
 * 行动牌：Rent、Deal Breaker、Sly Deal、生日、收债、Just Say No、House/Hotel 等。
 *
 * TODO(model): 用枚举或子类型区分具体行动；需求 6–15 的区分在此打标签，效果在 {@link com.example.monopoly_deal_game.logic.CardEffectExecutor}。
 */
public class ActionCard extends Card {

    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("TODO(logic): 由 CardEffectExecutor 分发");
    }
}
