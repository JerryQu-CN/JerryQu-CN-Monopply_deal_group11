package com.example.monopoly_deal_game.model;

/**
 * 钞票类卡牌（M 面值等）。
 *
 * TODO(model): 面额、放入银行区逻辑由 logic 调用。
 */
public class BankCard extends Card {

    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("TODO(model+logic): 作为现金入账或支付");
    }
}
