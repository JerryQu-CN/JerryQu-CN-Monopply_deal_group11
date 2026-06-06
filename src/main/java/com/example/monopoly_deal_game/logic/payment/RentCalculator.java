package com.example.monopoly_deal_game.logic.payment;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.List;

/**
 * Calculates rent amounts based on property set color, count, and building bonuses.
 */
public final class RentCalculator {

    private RentCalculator() {}

    /** Calculate the highest rent the landlord can charge for a given color (iterates all complete sets of that color, takes max, includes building bonuses). */
    public static int rentOnColor(Player landlord, CardColor color) {
        int best = 0;
        for (Property row : landlord.getProperties()) {
            if (row.getEffectiveColor() != color) continue;
            List<PropertyCard> cs = row.getCards();
            if (cs.isEmpty()) continue;
            int baseRent = baseRentForSet(cs, color);
            int bonus = row.getBuildingRentBonus();
            best = Math.max(best, baseRent + bonus);
        }
        return best;
    }

    /** Select the highest rent the landlord can charge among all candidate colors. */
    public static int bestRentForLandlord(Player landlord, List<CardColor> chosenColors,
                                          boolean doubleRent) {
        int best = 0;
        for (CardColor c : chosenColors) {
            if (c != null && c != CardColor.NONE && c != CardColor.WILD) {
                best = Math.max(best, rentOnColor(landlord, c));
            }
        }
        if (best == 0) {
            for (CardColor c : CardColor.TABLE_COLORS) {
                best = Math.max(best, rentOnColor(landlord, c));
            }
        }
        return doubleRent ? best * 2 : best;
    }

    /** Best wild rent (iterates all 10 colors). */
    public static int bestRentWild(Player landlord, boolean doubleRent) {
        int best = 0;
        for (CardColor c : CardColor.TABLE_COLORS) {
            best = Math.max(best, rentOnColor(landlord, c));
        }
        return doubleRent ? best * 2 : best;
    }

    private static int baseRentForSet(List<PropertyCard> cs, CardColor color) {
        int n = cs.size();
        // Find a non-wild base property card to get the rent tier
        for (PropertyCard pc : cs) {
            if (!pc.isWild() || pc.isBase()) {
                return pc.getRent(n);
            }
        }
        // fallback: use the first card's rent tier
        return cs.get(0).getRent(n);
    }
}