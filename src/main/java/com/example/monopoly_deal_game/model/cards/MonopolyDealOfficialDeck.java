package com.example.monopoly_deal_game.model.cards;

import java.util.ArrayList;
import java.util.List;

public final class MonopolyDealOfficialDeck {

    private MonopolyDealOfficialDeck() {}

    public static List<Card> createFullDeck() {
        List<Card> deck = new ArrayList<>(110);
        int id = 1;
        id = appendMoney(deck, id);
        id = appendPlainProperties(deck, id);
        id = appendWildProperties(deck, id);
        id = appendRentCards(deck, id);
        id = appendActionCards(deck, id);
        id = appendRuleCards(deck, id);
        if (deck.size() != 110 || id != 111) {
            throw new IllegalStateException("Deck size mismatch: " + deck.size() + ", nextId=" + id);
        }
        return deck;
    }

    private static int appendMoney(List<Card> d, int id) {
        id = addBankMany(d, id, 6, 1, "1M");
        id = addBankMany(d, id, 5, 2, "2M");
        id = addBankMany(d, id, 3, 3, "3M");
        id = addBankMany(d, id, 3, 4, "4M");
        id = addBankMany(d, id, 2, 5, "5M");
        id = addBankMany(d, id, 1, 10, "10M");
        return id;
    }

    private static int addBankMany(List<Card> d, int startId, int copies, int value, String label) {
        int id = startId;
        for (int i = 0; i < copies; i++) d.add(new BankCard(id++, label, value));
        return id;
    }

    private static int appendPlainProperties(List<Card> d, int id) {
        id = prop(d, id, "Brown A", 1, CardColor.BROWN, new int[] {1, 2});
        id = prop(d, id, "Brown B", 1, CardColor.BROWN, new int[] {1, 2});
        id = prop(d, id, "Light Blue A", 1, CardColor.LIGHT_BLUE, new int[] {1, 2, 3});
        id = prop(d, id, "Light Blue B", 1, CardColor.LIGHT_BLUE, new int[] {1, 2, 3});
        id = prop(d, id, "Light Blue C", 1, CardColor.LIGHT_BLUE, new int[] {1, 2, 3});
        id = prop(d, id, "Purple A", 2, CardColor.PURPLE, new int[] {1, 2, 4});
        id = prop(d, id, "Purple B", 2, CardColor.PURPLE, new int[] {1, 2, 4});
        id = prop(d, id, "Purple C", 2, CardColor.PURPLE, new int[] {1, 2, 4});
        id = prop(d, id, "Orange A", 2, CardColor.ORANGE, new int[] {1, 3, 5});
        id = prop(d, id, "Orange B", 2, CardColor.ORANGE, new int[] {1, 3, 5});
        id = prop(d, id, "Orange C", 2, CardColor.ORANGE, new int[] {1, 3, 5});
        id = prop(d, id, "Red A", 3, CardColor.RED, new int[] {2, 3, 6});
        id = prop(d, id, "Red B", 3, CardColor.RED, new int[] {2, 3, 6});
        id = prop(d, id, "Red C", 3, CardColor.RED, new int[] {2, 3, 6});
        id = prop(d, id, "Yellow A", 3, CardColor.YELLOW, new int[] {2, 4, 6});
        id = prop(d, id, "Yellow B", 3, CardColor.YELLOW, new int[] {2, 4, 6});
        id = prop(d, id, "Yellow C", 3, CardColor.YELLOW, new int[] {2, 4, 6});
        id = prop(d, id, "Green A", 4, CardColor.GREEN, new int[] {2, 4, 7});
        id = prop(d, id, "Green B", 4, CardColor.GREEN, new int[] {2, 4, 7});
        id = prop(d, id, "Green C", 4, CardColor.GREEN, new int[] {2, 4, 7});
        id = prop(d, id, "Dark Blue A", 3, CardColor.BLUE, new int[] {3, 8});
        id = prop(d, id, "Dark Blue B", 3, CardColor.BLUE, new int[] {3, 8});
        id = prop(d, id, "Railroad A", 2, CardColor.RAILROAD, new int[] {1, 2, 3, 4});
        id = prop(d, id, "Railroad B", 2, CardColor.RAILROAD, new int[] {1, 2, 3, 4});
        id = prop(d, id, "Railroad C", 2, CardColor.RAILROAD, new int[] {1, 2, 3, 4});
        id = prop(d, id, "Railroad D", 2, CardColor.RAILROAD, new int[] {1, 2, 3, 4});
        id = prop(d, id, "Utility A", 2, CardColor.UTILITY, new int[] {1, 2});
        id = prop(d, id, "Utility B", 2, CardColor.UTILITY, new int[] {1, 2});
        return id;
    }

    private static int prop(List<Card> d, int id, String name, int value, CardColor color, int[] rent) {
        d.add(new PropertyCard(id++, name, value, color, rent));
        return id;
    }

    /** 11 张彩色/多色万能房产：其中 2 张彩虹万能可适配任意颜色。 */
    private static int appendWildProperties(List<Card> d, int id) {
        int[] wild03 = new int[] {1, 2, 3};
        d.add(new PropertyCard(id++, "Wild (Pur-Org)", 0, CardColor.PURPLE, CardColor.ORANGE, wild03, true));
        d.add(new PropertyCard(id++, "Wild (Pur-Org)", 0, CardColor.PURPLE, CardColor.ORANGE, wild03, true));
        d.add(new PropertyCard(id++, "Wild (LB-Bro)", 0, CardColor.LIGHT_BLUE, CardColor.BROWN, wild03, true));
        d.add(new PropertyCard(id++, "Wild (LB-RR)", 0, CardColor.LIGHT_BLUE, CardColor.RAILROAD, wild03, true));
        d.add(new PropertyCard(id++, "Wild (DB-G)", 0, CardColor.BLUE, CardColor.GREEN, wild03, true));
        d.add(new PropertyCard(id++, "Wild (RR-G)", 0, CardColor.RAILROAD, CardColor.GREEN, wild03, true));
        d.add(new PropertyCard(id++, "Wild (Red-Yel)", 0, CardColor.RED, CardColor.YELLOW, wild03, true));
        d.add(new PropertyCard(id++, "Wild (Red-Yel)", 0, CardColor.RED, CardColor.YELLOW, wild03, true));
        d.add(new PropertyCard(id++, "Wild (Util-RR)", 0, CardColor.UTILITY, CardColor.RAILROAD, wild03, true));
        d.add(new PropertyCard(id++, "Rainbow Wild", 0, CardColor.WILD, CardColor.WILD, new int[] {1, 1, 1}, true));
        d.add(new PropertyCard(id++, "Rainbow Wild", 0, CardColor.WILD, CardColor.WILD, new int[] {1, 1, 1}, true));
        return id;
    }

    private static int appendRentCards(List<Card> d, int id) {
        d.add(new RentCard(id++, "Rent Pur-Org", 1, List.of(CardColor.PURPLE, CardColor.ORANGE), false));
        d.add(new RentCard(id++, "Rent Pur-Org", 1, List.of(CardColor.PURPLE, CardColor.ORANGE), false));
        d.add(new RentCard(id++, "Rent RR-Util", 1, List.of(CardColor.RAILROAD, CardColor.UTILITY), false));
        d.add(new RentCard(id++, "Rent RR-Util", 1, List.of(CardColor.RAILROAD, CardColor.UTILITY), false));
        d.add(new RentCard(id++, "Rent Gr-DB", 1, List.of(CardColor.GREEN, CardColor.BLUE), false));
        d.add(new RentCard(id++, "Rent Gr-DB", 1, List.of(CardColor.GREEN, CardColor.BLUE), false));
        d.add(new RentCard(id++, "Rent Bro-LB", 1, List.of(CardColor.BROWN, CardColor.LIGHT_BLUE), false));
        d.add(new RentCard(id++, "Rent Bro-LB", 1, List.of(CardColor.BROWN, CardColor.LIGHT_BLUE), false));
        d.add(new RentCard(id++, "Rent Red-Yel", 1, List.of(CardColor.RED, CardColor.YELLOW), false));
        d.add(new RentCard(id++, "Rent Red-Yel", 1, List.of(CardColor.RED, CardColor.YELLOW), false));
        d.add(new RentCard(id++, "Wild Rent", 3, List.of(), true));
        d.add(new RentCard(id++, "Wild Rent", 3, List.of(), true));
        d.add(new RentCard(id++, "Wild Rent", 3, List.of(), true));
        return id;
    }

    private static int appendActionCards(List<Card> d, int id) {
        id = actMany(d, id, 2, "Deal Breaker", 5, ActionCard.ActionType.DEAL_BREAKER);
        id = actMany(d, id, 3, "Sly Deal", 3, ActionCard.ActionType.SLY_DEAL);
        id = actMany(d, id, 4, "Forced Deal", 3, ActionCard.ActionType.FORCE_DEAL);
        id = actMany(d, id, 3, "Just Say No", 4, ActionCard.ActionType.JUST_SAY_NO);
        id = actMany(d, id, 3, "Debt Collector", 3, ActionCard.ActionType.DEBT_COLLECTOR);
        id = actMany(d, id, 3, "It's My Birthday!", 2, ActionCard.ActionType.ITS_MY_BIRTHDAY);
        id = actMany(d, id, 8, "Pass Go", 1, ActionCard.ActionType.PASS_GO);
        id = actMany(d, id, 3, "House", 3, ActionCard.ActionType.HOUSE);
        id = actMany(d, id, 3, "Hotel", 4, ActionCard.ActionType.HOTEL);
        id = actMany(d, id, 2, "Double the Rent!", 1, ActionCard.ActionType.DOUBLE_RENT);
        return id;
    }

    private static int actMany(List<Card> d, int start, int n, String name, int value, ActionCard.ActionType type) {
        int id = start;
        for (int i = 0; i < n; i++) d.add(new ActionCard(id++, name, value, type));
        return id;
    }

    private static int appendRuleCards(List<Card> d, int id) {
        for (int i = 0; i < 4; i++) d.add(new RuleCard(id++, "Quick Rules", "See Monopoly Deal instruction booklet."));
        return id;
    }
}
