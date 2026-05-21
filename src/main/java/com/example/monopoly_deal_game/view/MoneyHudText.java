package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.List;

/**
 * 左侧栏「资金短线」文案：把 {@link Player} 上与现金相关的模型字段压成两三行，供 HUD 显示。
 */
public final class MoneyHudText {

    private MoneyHudText() {}

    /** 银行区 + 手牌面额 + 物业套数摘要；无玩家时返回占位。 */
    public static String forPlayer(Player p) {
        if (p == null) {
            return "资金 —\n—";
        }
        int bankM = sumCardValues(p.getBank().getCards());
        var handCards = p.getHand().getCards();
        int handAllM = handCards.stream().mapToInt(Card::getValue).sum();
        int handCashM = handCards.stream()
                .filter(c -> c instanceof BankCard)
                .mapToInt(Card::getValue)
                .sum();
        int monoSets = p.getFullSetCount();
        int propRows = p.getProperties().size();
        return String.format(
                "银行 %dM · 手牌 %dM (钞%dM)\n物业组 %d 行 · 成套 %d",
                bankM, handAllM, handCashM, propRows, monoSets);
    }

    private static int sumCardValues(List<Card> cards) {
        int t = 0;
        for (Card c : cards) {
            t += c.getValue();
        }
        return t;
    }
}
