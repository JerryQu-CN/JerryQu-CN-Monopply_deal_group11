package com.example.monopoly_deal_game.model;

/**
 * 钞票类卡牌（M 面值等）。
<<<<<<< HEAD
 *
 * TODO(model): 面额、放入银行区逻辑由 logic 调用。
 */
public class BankCard extends Card {

    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("TODO(model+logic): 作为现金入账或支付");
    }
}
=======
 * * 职责：
 * 1. 存储货币面值。
 * 2. 仅能存入银行区或作为费用支付，不具备任何行动效果。
 */
public class BankCard extends Card {

    /**
     * @param id    卡牌唯一编号
     * @param name  名称（例如 "1M", "5M"）
     * @param value 面额（实际在支付时计算的数值）
     */
    public BankCard(int id, String name, int value) {
        // 货币卡的 description 可以固定
        super(id, name, value, "Currency used for paying debts and rent.");
    }

    @Override
    public void use(Player user, Player target) {
        // 根据规则 4：存入银行
        // 将此卡从 user.getHand() 移动到 user.getBankArea()
        throw new UnsupportedOperationException("TODO(model+logic): 作为现金入账或支付");
    }
}
>>>>>>> ec928dc (Initial commit: rename folder and add all files)
