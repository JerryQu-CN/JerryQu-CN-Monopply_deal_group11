package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class RentCalculator {
    private static final Set<CardColor> TABLE_COLORS = EnumSet.of(CardColor.BROWN, CardColor.LIGHT_BLUE, CardColor.PURPLE, CardColor.ORANGE, CardColor.RED, CardColor.YELLOW, CardColor.GREEN, CardColor.BLUE, CardColor.RAILROAD);
    private RentCalculator() {}
    public static Set<CardColor> tableColors() { return EnumSet.copyOf(TABLE_COLORS); }
    public static int rentOnColor(Player landlord, CardColor color) { return rentForSingleColor(landlord, color); }
    public static int bestRentForLandlord(Player landlord, List<CardColor> chosenColors, boolean doubleRent) {
        int best = 0;
        for (CardColor c : chosenColors) if (c != null && c != CardColor.NONE && c != CardColor.WILD) best = Math.max(best, rentForSingleColor(landlord, c));
        if (best == 0 && chosenColors != null && chosenColors.stream().anyMatch(x -> x == CardColor.WILD)) {
            for (CardColor c : TABLE_COLORS) best = Math.max(best, rentForSingleColor(landlord, c));
        }
        return doubleRent ? best * 2 : best;
    }
    public static int bestRentWild(Player landlord, boolean doubleRent) {
        int best = 0; for (CardColor c : TABLE_COLORS) best = Math.max(best, rentForSingleColor(landlord, c)); return doubleRent ? best * 2 : best;
    }
    private static int baseRentForDeclaredColor(List<PropertyCard> cs, CardColor color, int n) {
        for (PropertyCard pc : cs) if (pc.getCurrentColor() == color && !pc.isWild()) return pc.getRent(n);
        return cs.isEmpty() ? 0 : cs.get(0).getRent(n);
    }
    private static int rentForSingleColor(Player landlord, CardColor color) {
        int best = 0; for (Property row : landlord.getProperties()) {
            if (row.getEffectiveColor() != color) continue; List<PropertyCard> cs = row.getCards(); if (cs.isEmpty()) continue;
            best = Math.max(best, baseRentForDeclaredColor(cs, color, cs.size()) + row.getBuildingRentBonus());
        } return best;
    }
}
