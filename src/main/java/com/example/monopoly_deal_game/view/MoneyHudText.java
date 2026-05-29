package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.List;

/**
 * Left sidebar "money snapshot" text: compresses cash-related model fields from {@link Player} into a couple of lines for HUD display.
 */
public final class MoneyHudText {

    private MoneyHudText() {}

    /** Bank area + hand card value + property set count summary; returns placeholder when no player. */
    public static String forPlayer(Player p) {
        if (p == null) {
            return "Funds —\n—";
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
                "Bank %dM - Hand %dM (cash %dM)\nProperty %d rows - Full sets %d",
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
