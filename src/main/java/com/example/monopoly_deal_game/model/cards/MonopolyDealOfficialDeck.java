package com.example.monopoly_deal_game.model.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Monopoly Deal 官方 110 张牌组。
 * 对齐 Monopoly-Deal-main 中 oldmana.md.server.card.collection.deck.VanillaDeck 的数据。
 */
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

    // ---- Money Cards (20 张) ----
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
        for (int i = 0; i < copies; i++) {
            BankCard bc = new BankCard(id++, label, value);
            bc.setCountsTowardLimit(true);
            bc.setUndoable(true);
            d.add(bc);
        }
        return id;
    }

    // ---- Plain Property Cards (28 张) ----
    private static int appendPlainProperties(List<Card> d, int id) {
        // Brown: 2 cards, rent [1, 2]
        id = prop(d, id, "Brown A", 1, CardColor.BROWN, new int[]{1, 2});
        id = prop(d, id, "Brown B", 1, CardColor.BROWN, new int[]{1, 2});
        // Light Blue: 3 cards, rent [1, 2, 3]
        id = prop(d, id, "Light Blue A", 1, CardColor.LIGHT_BLUE, new int[]{1, 2, 3});
        id = prop(d, id, "Light Blue B", 1, CardColor.LIGHT_BLUE, new int[]{1, 2, 3});
        id = prop(d, id, "Light Blue C", 1, CardColor.LIGHT_BLUE, new int[]{1, 2, 3});
        // Purple (Magenta): 3 cards, rent [1, 2, 4]
        id = prop(d, id, "Purple A", 2, CardColor.PURPLE, new int[]{1, 2, 4});
        id = prop(d, id, "Purple B", 2, CardColor.PURPLE, new int[]{1, 2, 4});
        id = prop(d, id, "Purple C", 2, CardColor.PURPLE, new int[]{1, 2, 4});
        // Orange: 3 cards, rent [1, 3, 5]
        id = prop(d, id, "Orange A", 2, CardColor.ORANGE, new int[]{1, 3, 5});
        id = prop(d, id, "Orange B", 2, CardColor.ORANGE, new int[]{1, 3, 5});
        id = prop(d, id, "Orange C", 2, CardColor.ORANGE, new int[]{1, 3, 5});
        // Red: 3 cards, rent [2, 3, 6]
        id = prop(d, id, "Red A", 3, CardColor.RED, new int[]{2, 3, 6});
        id = prop(d, id, "Red B", 3, CardColor.RED, new int[]{2, 3, 6});
        id = prop(d, id, "Red C", 3, CardColor.RED, new int[]{2, 3, 6});
        // Yellow: 3 cards, rent [2, 4, 6]
        id = prop(d, id, "Yellow A", 3, CardColor.YELLOW, new int[]{2, 4, 6});
        id = prop(d, id, "Yellow B", 3, CardColor.YELLOW, new int[]{2, 4, 6});
        id = prop(d, id, "Yellow C", 3, CardColor.YELLOW, new int[]{2, 4, 6});
        // Green: 3 cards, rent [2, 4, 7]
        id = prop(d, id, "Green A", 4, CardColor.GREEN, new int[]{2, 4, 7});
        id = prop(d, id, "Green B", 4, CardColor.GREEN, new int[]{2, 4, 7});
        id = prop(d, id, "Green C", 4, CardColor.GREEN, new int[]{2, 4, 7});
        // Dark Blue: 2 cards, rent [3, 8]
        id = prop(d, id, "Dark Blue A", 3, CardColor.BLUE, new int[]{3, 8});
        id = prop(d, id, "Dark Blue B", 3, CardColor.BLUE, new int[]{3, 8});
        // Railroad: 4 cards, rent [1, 2, 3, 4]
        id = prop(d, id, "Railroad A", 2, CardColor.RAILROAD, new int[]{1, 2, 3, 4});
        id = prop(d, id, "Railroad B", 2, CardColor.RAILROAD, new int[]{1, 2, 3, 4});
        id = prop(d, id, "Railroad C", 2, CardColor.RAILROAD, new int[]{1, 2, 3, 4});
        id = prop(d, id, "Railroad D", 2, CardColor.RAILROAD, new int[]{1, 2, 3, 4});
        // Utility: 2 cards, rent [1, 2]
        id = prop(d, id, "Utility A", 2, CardColor.UTILITY, new int[]{1, 2});
        id = prop(d, id, "Utility B", 2, CardColor.UTILITY, new int[]{1, 2});
        return id;
    }

    private static int prop(List<Card> d, int id, String name, int value, CardColor color, int[] rent) {
        PropertyCard pc = new PropertyCard(id++, name, value, color, rent);
        pc.setUndoable(true);
        d.add(pc);
        return id;
    }

    // ---- Wild Property Cards (11 张) ----
    private static int appendWildProperties(List<Card> d, int id) {
        // 双色万能房产：2 张 Purple-Orange
        d.add(new PropertyCard(id++, "Wild (Pur-Org)", 0,
                CardColor.PURPLE, CardColor.ORANGE, new int[]{1, 2, 3}, true));
        d.add(new PropertyCard(id++, "Wild (Pur-Org)", 0,
                CardColor.PURPLE, CardColor.ORANGE, new int[]{1, 2, 3}, true));
        // 双色万能：Light Blue - Brown
        d.add(new PropertyCard(id++, "Wild (LB-Bro)", 0,
                CardColor.LIGHT_BLUE, CardColor.BROWN, new int[]{1, 2, 3}, true));
        // 双色万能：Light Blue - Railroad
        d.add(new PropertyCard(id++, "Wild (LB-RR)", 0,
                CardColor.LIGHT_BLUE, CardColor.RAILROAD, new int[]{1, 2, 3}, true));
        // 双色万能：Dark Blue - Green
        d.add(new PropertyCard(id++, "Wild (DB-G)", 0,
                CardColor.BLUE, CardColor.GREEN, new int[]{1, 2, 3}, true));
        // 双色万能：Railroad - Green
        d.add(new PropertyCard(id++, "Wild (RR-G)", 0,
                CardColor.RAILROAD, CardColor.GREEN, new int[]{1, 2, 3}, true));
        // 双色万能：Red - Yellow (×2)
        d.add(new PropertyCard(id++, "Wild (Red-Yel)", 0,
                CardColor.RED, CardColor.YELLOW, new int[]{1, 2, 3}, true));
        d.add(new PropertyCard(id++, "Wild (Red-Yel)", 0,
                CardColor.RED, CardColor.YELLOW, new int[]{1, 2, 3}, true));
        // 双色万能：Utility - Railroad
        d.add(new PropertyCard(id++, "Wild (Util-RR)", 0,
                CardColor.UTILITY, CardColor.RAILROAD, new int[]{1, 2, 3}, true));
        // 彩虹万能（所有颜色可用）：2 张（不为基础，不可被偷）
        d.add(new PropertyCard(id++, "Rainbow Wild", 0, new int[]{1, 1, 1}, true, false, false));
        d.add(new PropertyCard(id++, "Rainbow Wild", 0, new int[]{1, 1, 1}, true, false, false));
        return id;
    }

    // ---- Rent Cards (13 张) ----
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

    // ---- Action Cards (34 张: 2+3+3+3+3+3+10+3+2+2) ----
    private static int appendActionCards(List<Card> d, int id) {
        // Deal Breaker: 2 张, 5M（抢夺一套完整物业）
        id = actMany(d, id, 2, "Deal Breaker", 5,
                ActionCard.ActionType.DEAL_BREAKER, true, false);
        // Sly Deal: 3 张, 3M（偷取一张非完整套的物业）
        id = actMany(d, id, 3, "Sly Deal", 3,
                ActionCard.ActionType.SLY_DEAL, true, false);
        // Forced Deal: 3 张, 3M（强制交换一张物业）
        id = actMany(d, id, 3, "Forced Deal", 3,
                ActionCard.ActionType.FORCE_DEAL, true, false);
        // Just Say No: 3 张, 4M（取消一次行动效果，不计入出牌限额）
        for (int i = 0; i < 3; i++) {
            d.add(new JustSayNoCard(id++));
        }
        // Debt Collector: 3 张, 3M（向指定玩家收取 5M）
        id = actMany(d, id, 3, "Debt Collector", 3,
                ActionCard.ActionType.DEBT_COLLECTOR, true, false);
        // It's My Birthday: 3 张, 2M（向所有其他玩家收取 2M）
        id = actMany(d, id, 3, "It's My Birthday", 2,
                ActionCard.ActionType.ITS_MY_BIRTHDAY, true, false);
        // Pass Go: 10 张, 1M（额外抽 2 张牌）
        id = actMany(d, id, 10, "Pass Go", 1,
                ActionCard.ActionType.PASS_GO, true, false);
        // House: 3 张, 3M（在完整套上放置房屋，租金+3）
        id = actMany(d, id, 3, "House", 3,
                ActionCard.ActionType.HOUSE, true, false);
        // Hotel: 2 张, 4M（在有房屋的完整套上再放旅馆，租金+4）
        id = actMany(d, id, 2, "Hotel", 4,
                ActionCard.ActionType.HOTEL, true, false);
        // Double the Rent: 2 张, 1M（下一张租金卡金额×2）
        id = actMany(d, id, 2, "Double the Rent", 1,
                ActionCard.ActionType.DOUBLE_RENT, true, false);
        return id;
    }

    private static int actMany(List<Card> d, int start, int n, String name, int value,
                                ActionCard.ActionType type, boolean countsTowardLimit,
                                boolean undoable) {
        int id = start;
        for (int i = 0; i < n; i++) {
            ActionCard ac = new ActionCard(id++, name, value, type);
            ac.setCountsTowardLimit(countsTowardLimit);
            ac.setUndoable(undoable);
            d.add(ac);
        }
        return id;
    }

    // ---- Rule Cards (4 张) ----
    private static int appendRuleCards(List<Card> d, int id) {
        for (int i = 0; i < 4; i++) {
            d.add(new RuleCard(id++, "Quick Rules", "See Monopoly Deal instruction booklet."));
        }
        return id;
    }
}