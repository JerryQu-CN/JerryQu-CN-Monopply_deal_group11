package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;

/**
 * 行动牌效果执行（需求 8–15：Rent、Deal Breaker、Force、Sly、生日、收债、Just Say No 链、House/Hotel）。
 *
 * TODO(logic): 每张行动牌一个私有方法或策略表；支付流程与 {@link GameLogic} 协作。
 */
public class CardEffectExecutor {

    public void execute(GameSession session, ActionCard card, Player user, Player target) {
        throw new UnsupportedOperationException("TODO(logic):按 card 类型分发");
    }
}
