package com.example.monopoly_deal_game.model.cards;

import java.util.ArrayList;
import java.util.List;

/**
 * Monopoly Deal official 110-card deck.
 * Aligned with the data in oldmana.md.server.card.collection.deck.VanillaDeck from Monopoly-Deal-main.
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

    // ---- Money Cards (20 cards) ----
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

    // ---- Plain Property Cards (28 cards) ----
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
        // Black: 4 cards, rent [1, 2, 3, 4]
        id = prop(d, id, "Black A", 2, CardColor.BLACK, new int[]{1, 2, 3, 4});
        id = prop(d, id, "Black B", 2, CardColor.BLACK, new int[]{1, 2, 3, 4});
        id = prop(d, id, "Black C", 2, CardColor.BLACK, new int[]{1, 2, 3, 4});
        id = prop(d, id, "Black D", 2, CardColor.BLACK, new int[]{1, 2, 3, 4});
        // Light Green: 2 cards, rent [1, 2]
        id = prop(d, id, "Light Green A", 2, CardColor.LIGHT_GREEN, new int[]{1, 2});
        id = prop(d, id, "Light Green B", 2, CardColor.LIGHT_GREEN, new int[]{1, 2});
        return id;
    }

    private static int prop(List<Card> d, int id, String name, int value, CardColor color, int[] rent) {
        PropertyCard pc = new PropertyCard(id++, name, value, color, rent);
        pc.setUndoable(true);
        d.add(pc);
        return id;
    }

    // ---- Wild Property Cards (11 cards) ----
    private static int appendWildProperties(List<Card> d, int id) {
        // Bi-color property: 2 cards Purple-Orange
        d.add(new PropertyCard(id++, "Multi (Pur-Org)", 2,
                CardColor.PURPLE, CardColor.ORANGE, new int[]{1, 2, 3}));
        d.add(new PropertyCard(id++, "Multi (Pur-Org)", 2,
                CardColor.PURPLE, CardColor.ORANGE, new int[]{1, 2, 3}));
        // Bi-color: Light Blue - Brown
        d.add(new PropertyCard(id++, "Multi (LB-Bro)", 1,
                CardColor.LIGHT_BLUE, CardColor.BROWN, new int[]{1, 2, 3}));
        // Bi-color: Light Blue - Black
        d.add(new PropertyCard(id++, "Multi (LB-Blk)", 4,
                CardColor.LIGHT_BLUE, CardColor.BLACK, new int[]{1, 2, 3}));
        // Bi-color: Dark Blue - Green
        d.add(new PropertyCard(id++, "Multi (DB-G)", 4,
                CardColor.BLUE, CardColor.GREEN, new int[]{1, 2, 3}));
        // Bi-color: Black - Green
        d.add(new PropertyCard(id++, "Multi (Blk-G)", 4,
                CardColor.BLACK, CardColor.GREEN, new int[]{1, 2, 3}));
        // Bi-color: Red - Yellow (x2)
        d.add(new PropertyCard(id++, "Multi (Red-Yel)", 3,
                CardColor.RED, CardColor.YELLOW, new int[]{1, 2, 3}));
        d.add(new PropertyCard(id++, "Multi (Red-Yel)", 3,
                CardColor.RED, CardColor.YELLOW, new int[]{1, 2, 3}));
        // Bi-color: Light Green - Black
        d.add(new PropertyCard(id++, "Multi (LG-Blk)", 2,
                CardColor.LIGHT_GREEN, CardColor.BLACK, new int[]{1, 2, 3}));
        // Rainbow (all colors selectable): 2 cards (not base, not stealable)
        d.add(new PropertyCard(id++, "Rainbow", 0, new int[]{1, 1, 1}, false, false));
        d.add(new PropertyCard(id++, "Rainbow", 0, new int[]{1, 1, 1}, false, false));
        return id;
    }

    // ---- Rent Cards (13 cards) ----
    private static int appendRentCards(List<Card> d, int id) {
        d.add(new RentCard(id++, "Rent Pur-Org", 1, List.of(CardColor.PURPLE, CardColor.ORANGE), false));
        d.add(new RentCard(id++, "Rent Pur-Org", 1, List.of(CardColor.PURPLE, CardColor.ORANGE), false));
        d.add(new RentCard(id++, "Rent Blk-LG", 1, List.of(CardColor.BLACK, CardColor.LIGHT_GREEN), false));
        d.add(new RentCard(id++, "Rent Blk-LG", 1, List.of(CardColor.BLACK, CardColor.LIGHT_GREEN), false));
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

    // ---- Action Cards (34 cards: 2+3+3+3+3+3+10+3+2+2) ----
    private static int appendActionCards(List<Card> d, int id) {
        // Deal Breaker: 2 cards, 5M (steal a complete property set)
        id = actMany(d, id, 2, "Deal Breaker", 5, ActionCardDealBreaker::new);
        // Sly Deal: 3 cards, 3M (steal a property from a non-complete set)
        id = actMany(d, id, 3, "Sly Deal", 3, ActionCardSlyDeal::new);
        // Forced Deal: 3 cards, 3M (force swap of a property)
        id = actMany(d, id, 3, "Forced Deal", 3, ActionCardForcedDeal::new);
        // Just Say No: 3 cards, 4M (cancel an action effect; does not count toward play limit)
        for (int i = 0; i < 3; i++) {
            d.add(new ActionCardJustSayNo(id++, "Just Say No", 4));
        }
        // Debt Collector: 3 cards, 3M (collect 5M from a target player)
        id = actMany(d, id, 3, "Debt Collector", 3, ActionCardDebtCollector::new);
        // It's My Birthday: 3 cards, 2M (collect 2M from all other players)
        id = actMany(d, id, 3, "It's My Birthday", 2, ActionCardItsMyBirthday::new);
        // Pass Go: 10 cards, 1M (draw 2 extra cards)
        id = actMany(d, id, 10, "Pass Go", 1, ActionCardPassGo::new);
        // House: 3 cards, 3M (place a house on a full set; rent +3)
        id = actMany(d, id, 3, "House", 3, ActionCardHouse::new);
        // Hotel: 2 cards, 4M (place a hotel on a full set that has a house; rent +4)
        id = actMany(d, id, 2, "Hotel", 4, ActionCardHotel::new);
        // Double the Rent: 2 cards, 1M (next rent card amount x2)
        id = actMany(d, id, 2, "Double the Rent", 1, ActionCardDoubleTheRent::new);
        return id;
    }

    @FunctionalInterface
    private interface ActionCardFactory {
        ActionCard create(int id, String name, int value);
    }

    private static int actMany(List<Card> d, int start, int n, String name, int value,
                                ActionCardFactory factory) {
        int id = start;
        for (int i = 0; i < n; i++) {
            ActionCard ac = factory.create(id++, name, value);
            ac.setUndoable(true);
            d.add(ac);
        }
        return id;
    }

    // ---- Rule Cards (4 cards) ----
    private static int appendRuleCards(List<Card> d, int id) {
        for (int i = 0; i < 4; i++) {
            d.add(new RuleCard(id++, "Quick Rules", "See Monopoly Deal instruction booklet."));
        }
        return id;
    }
}