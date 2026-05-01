package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.model.Player;

/**
 * 钞票类卡牌（M 面值等）。
 *
 * <p>职责：
 * <ol>
 *   <li>存储货币面值。</li>
 *   <li>仅能存入银行区或作为费用支付，不具备任何行动效果。</li>
 * </ol>
 */
public class BankCard extends Card {

    /**
     * @param id    卡牌唯一编号
     * @param name  名称（例如 "1M", "5M"）
     * @param value 面额（实际在支付时计算的数值）
     */
    public BankCard(int id, String name, int value) {
        super(id, name, value, "Currency used for paying debts and rent.");
    }

    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("TODO(model+logic): 作为现金入账或支付");
    }
}
