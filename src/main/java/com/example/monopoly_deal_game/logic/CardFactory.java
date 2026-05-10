package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for generating the official Monopoly Deal 110-card deck.
 * Centralized card creation for consistent deck composition.
 */
public class CardFactory {
    private int nextCardId = 1;

    /** Creates and returns the complete official 110-card deck */
    public List<Card> createFullDeck() {
        List<Card> deck = new ArrayList<>();
        addBankCards(deck);
        addPropertyCards(deck);
        addActionCards(deck);
        addRentCards(deck);
        addRuleCards(deck);
        return deck;
    }

    // Bank (Money) Cards
    private void addBankCards(List<Card> deck) {
        for (int i = 0; i < 6; i++) deck.add(new BankCard(nextCardId++, "1M", 1));
        for (int i = 0; i < 5; i++) deck.add(new BankCard(nextCardId++, "2M", 2));
        for (int i = 0; i < 3; i++) deck.add(new BankCard(nextCardId++, "3M", 3));
        for (int i = 0; i < 3; i++) deck.add(new BankCard(nextCardId++, "4M", 4));
        for (int i = 0; i < 2; i++) deck.add(new BankCard(nextCardId++, "5M", 5));
        deck.add(new BankCard(nextCardId++, "10M", 10));
    }

    // Property Cards
    private void addPropertyCards(List<Card> deck) {
        // Single-color properties
        addSingleColorProperty(deck, "Mediterranean Ave", CardColor.BROWN, 1, new int[]{1, 2});
        addSingleColorProperty(deck, "Baltic Ave", CardColor.BROWN, 1, new int[]{1, 2});

        addSingleColorProperty(deck, "Oriental Ave", CardColor.LIGHT_BLUE, 1, new int[]{1, 2, 3});
        addSingleColorProperty(deck, "Vermont Ave", CardColor.LIGHT_BLUE, 1, new int[]{1, 2, 3});
        addSingleColorProperty(deck, "Connecticut Ave", CardColor.LIGHT_BLUE, 1, new int[]{1, 2, 3});

        addSingleColorProperty(deck, "St. Charles Place", CardColor.PURPLE, 2, new int[]{1, 2, 4});
        addSingleColorProperty(deck, "States Ave", CardColor.PURPLE, 2, new int[]{1, 2, 4});
        addSingleColorProperty(deck, "Virginia Ave", CardColor.PURPLE, 2, new int[]{1, 2, 4});

        addSingleColorProperty(deck, "St. James Place", CardColor.ORANGE, 2, new int[]{1, 3, 5});
        addSingleColorProperty(deck, "Tennessee Ave", CardColor.ORANGE, 2, new int[]{1, 3, 5});
        addSingleColorProperty(deck, "New York Ave", CardColor.ORANGE, 2, new int[]{1, 3, 5});

        addSingleColorProperty(deck, "Kentucky Ave", CardColor.RED, 3, new int[]{2, 3, 6});
        addSingleColorProperty(deck, "Indiana Ave", CardColor.RED, 3, new int[]{2, 3, 6});
        addSingleColorProperty(deck, "Illinois Ave", CardColor.RED, 3, new int[]{2, 3, 6});

        addSingleColorProperty(deck, "Atlantic Ave", CardColor.YELLOW, 3, new int[]{2, 4, 7});
        addSingleColorProperty(deck, "Ventnor Ave", CardColor.YELLOW, 3, new int[]{2, 4, 7});
        addSingleColorProperty(deck, "Marvin Gardens", CardColor.YELLOW, 3, new int[]{2, 4, 7});

        addSingleColorProperty(deck, "Pacific Ave", CardColor.GREEN, 4, new int[]{2, 4, 7});
        addSingleColorProperty(deck, "North Carolina Ave", CardColor.GREEN, 4, new int[]{2, 4, 7});
        addSingleColorProperty(deck, "Pennsylvania Ave", CardColor.GREEN, 4, new int[]{2, 4, 7});

        addSingleColorProperty(deck, "Park Place", CardColor.BLUE, 4, new int[]{3, 8});
        addSingleColorProperty(deck, "Boardwalk", CardColor.BLUE, 4, new int[]{3, 8});

        addSingleColorProperty(deck, "Reading Railroad", CardColor.RAILROAD, 2, new int[]{1, 2, 3, 4});
        addSingleColorProperty(deck, "Pennsylvania Railroad", CardColor.RAILROAD, 2, new int[]{1, 2, 3, 4});
        addSingleColorProperty(deck, "B&O Railroad", CardColor.RAILROAD, 2, new int[]{1, 2, 3, 4});
        addSingleColorProperty(deck, "Short Line", CardColor.RAILROAD, 2, new int[]{1, 2, 3, 4});

        addSingleColorProperty(deck, "Electric Company", CardColor.UTILITY, 2, new int[]{1, 2});
        addSingleColorProperty(deck, "Water Works", CardColor.UTILITY, 2, new int[]{1, 2});

        // Dual-color properties
        addDualColorProperty(deck, CardColor.BROWN, CardColor.LIGHT_BLUE, 1, new int[]{1, 2});
        addDualColorProperty(deck, CardColor.PURPLE, CardColor.ORANGE, 2, new int[]{1, 2, 4});
        addDualColorProperty(deck, CardColor.RED, CardColor.YELLOW, 3, new int[]{2, 3, 6});
        addDualColorProperty(deck, CardColor.GREEN, CardColor.BLUE, 4, new int[]{2, 4, 7});
        addDualColorProperty(deck, CardColor.RAILROAD, CardColor.UTILITY, 2, new int[]{1, 2, 3, 4});
        addDualColorProperty(deck, CardColor.RAILROAD, CardColor.UTILITY, 2, new int[]{1, 2, 3, 4});
        addDualColorProperty(deck, CardColor.LIGHT_BLUE, CardColor.RAILROAD, 1, new int[]{1, 2, 3});
        addDualColorProperty(deck, CardColor.PURPLE, CardColor.RAILROAD, 2, new int[]{1, 2, 4});

        // Wild properties
        for (int i = 0; i < 2; i++) {
            deck.add(new PropertyCard(
                    nextCardId++,
                    "Wild Property",
                    0,
                    CardColor.WILD,
                    CardColor.WILD,
                    new int[]{},
                    true
            ));
        }
    }

    private void addSingleColorProperty(List<Card> deck, String name, CardColor color, int value, int[] rentLevels) {
        deck.add(new PropertyCard(nextCardId++, name, value, color, rentLevels));
    }

    private void addDualColorProperty(List<Card> deck, CardColor c1, CardColor c2, int value, int[] rentLevels) {
        deck.add(new PropertyCard(nextCardId++, c1 + "/" + c2 + " Property", value, c1, c2, rentLevels, false));
    }

    // Action Cards
    private void addActionCards(List<Card> deck) {
        for (int i = 0; i < 10; i++) deck.add(new ActionCard(nextCardId++, "Pass Go", 1, ActionCard.ActionType.PASS_GO));
        for (int i = 0; i < 2; i++) deck.add(new ActionCard(nextCardId++, "Deal Breaker", 5, ActionCard.ActionType.DEAL_BREAKER));
        for (int i = 0; i < 3; i++) deck.add(new ActionCard(nextCardId++, "Sly Deal", 3, ActionCard.ActionType.SLY_DEAL));
        for (int i = 0; i < 2; i++) deck.add(new ActionCard(nextCardId++, "Force Deal", 3, ActionCard.ActionType.FORCE_DEAL));
        for (int i = 0; i < 3; i++) deck.add(new ActionCard(nextCardId++, "Debt Collector", 3, ActionCard.ActionType.DEBT_COLLECTOR));
        for (int i = 0; i < 3; i++) deck.add(new ActionCard(nextCardId++, "It's My Birthday", 2, ActionCard.ActionType.ITS_MY_BIRTHDAY));
        for (int i = 0; i < 3; i++) deck.add(new ActionCard(nextCardId++, "Just Say No", 4, ActionCard.ActionType.JUST_SAY_NO));
        for (int i = 0; i < 3; i++) deck.add(new ActionCard(nextCardId++, "Double Rent", 1, ActionCard.ActionType.DOUBLE_RENT));
    }

    // Rent Cards
    private void addRentCards(List<Card> deck) {
        // Dual-color rent cards
        for (int i = 0; i < 3; i++) {
            addDualColorRent(deck, CardColor.BROWN, CardColor.LIGHT_BLUE);
            addDualColorRent(deck, CardColor.PURPLE, CardColor.ORANGE);
            addDualColorRent(deck, CardColor.RED, CardColor.YELLOW);
            addDualColorRent(deck, CardColor.GREEN, CardColor.BLUE);
        }

        // Wild rent cards
        for (int i = 0; i < 2; i++) {
            deck.add(new RentCard(
                    nextCardId++,
                    1,
                    List.of(CardColor.values()),
                    true
            ));
        }
    }

    private void addDualColorRent(List<Card> deck, CardColor c1, CardColor c2) {
        deck.add(new RentCard(nextCardId++, 1, List.of(c1, c2), false));
    }

    // Rule Cards (filtered out during game init)
    private void addRuleCards(List<Card> deck) {
        //function to rule card~
    }
}
