package com.example.monopoly_deal_game.logic.payment;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.GameConfig;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.RentCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines rent eligibility, payer selection, and charge resolution rules for rent cards.
 */
public final class RentRules {
    private RentRules() {}

    /** Returns the list of colors the rent can be charged on (requires the landlord to have properties of that color). */
    public static List<CardColor> eligibleChargeColors(RentCard rc, Player landlord) {
        List<CardColor> out = new ArrayList<>();
        if (rc == null || landlord == null) return out;
        if (rc.isWildRent()) {
            for (CardColor c : CardColor.TABLE_COLORS) {
                if (RentCalculator.rentOnColor(landlord, c) > 0) out.add(c);
            }
            return out;
        }
        for (CardColor c : rc.getApplicableColors()) {
            if (c != null && c != CardColor.NONE) out.add(c);
        }
        return out;
    }

    public static boolean canUseRentEffect(RentCard rc, Player landlord, CardColor declared) {
        List<CardColor> ok = eligibleChargeColors(rc, landlord);
        if (ok.isEmpty()) return false;
        if (declared != null && !ok.contains(declared)) return false;
        if (declared != null) return RentCalculator.rentOnColor(landlord, declared) > 0;
        return ok.stream().anyMatch(c -> RentCalculator.rentOnColor(landlord, c) > 0);
    }

    public static boolean canUseRentEffect(RentCard rc, Player landlord, CardColor declared,
                                           Player payerChosen, GameSession session) {
        if (!canUseRentEffect(rc, landlord, declared) || session == null || landlord == null)
            return false;
        List<Player> renters = rentersExcludingLandlord(landlord, session);
        if (renters.isEmpty()) return false;
        if (payerChosen != null) return renters.contains(payerChosen);
        // Dual-color or wild rent that charges everyone — no specific target required
        boolean chargesAll = rc.isWildRent()
                ? GameConfig.MULTI_COLOR_RENT_CHARGES_ALL
                : (rc.getApplicableColors().size() >= 2 && GameConfig.TWO_COLOR_RENT_CHARGES_ALL);
        if (chargesAll) return true;
        return landlord.isAI() && renters.size() == 1;
    }

    public static boolean canUseRentEffectForUi(RentCard rc, Player landlord, CardColor declared,
                                                GameSession session) {
        return canUseRentEffect(rc, landlord, declared)
                && session != null && landlord != null
                && !rentersExcludingLandlord(landlord, session).isEmpty();
    }

    public static List<Player> rentersExcludingLandlord(Player landlord, GameSession session) {
        List<Player> out = new ArrayList<>();
        if (landlord == null || session == null) return out;
        for (Player p : session.getPlayers()) {
            if (p != null && !p.equals(landlord)) out.add(p);
        }
        return out;
    }

    public static Player resolvedRentPayer(Player landlord, GameSession session, CardPlayOptions opt) {
        List<Player> renters = rentersExcludingLandlord(landlord, session);
        if (renters.isEmpty()) return null;
        Player pick = opt != null ? opt.actionTargetPlayer() : null;
        if (pick != null && renters.contains(pick)) return pick;
        if (landlord.isAI()) return renters.get(0);
        return null;
    }
}