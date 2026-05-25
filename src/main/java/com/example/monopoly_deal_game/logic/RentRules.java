package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.RentCard;

import java.util.ArrayList;
import java.util.List;

public final class RentRules {
    private RentRules() {}
    public static List<CardColor> eligibleChargeColors(RentCard rc, Player landlord) {
        List<CardColor> out = new ArrayList<>();
        if (rc == null || landlord == null) return out;
        if (rc.isWildRent()) { for (CardColor c : RentCalculator.tableColors()) if (RentCalculator.rentOnColor(landlord, c) > 0) out.add(c); return out; }
        for (CardColor c : rc.getApplicableColors()) if (c != null && c != CardColor.NONE && c != CardColor.WILD) out.add(c);
        return out;
    }
    public static boolean canUseRentEffect(RentCard rc, Player landlord, CardColor declared) {
        List<CardColor> ok = eligibleChargeColors(rc, landlord); if (ok.isEmpty()) return false; if (declared != null && !ok.contains(declared)) return false; if (declared != null) return RentCalculator.rentOnColor(landlord, declared) > 0; return ok.stream().anyMatch(c -> RentCalculator.rentOnColor(landlord, c) > 0);
    }
    public static boolean canUseRentEffect(RentCard rc, Player landlord, CardColor declared, Player payerChosen, GameSession session) {
        if (!canUseRentEffect(rc, landlord, declared) || session == null || landlord == null) return false;
        List<Player> renters = rentersExcludingLandlord(landlord, session); if (renters.isEmpty()) return false;
        if (payerChosen != null) return renters.contains(payerChosen); return landlord.isAI() && renters.size() == 1;
    }
    public static boolean canUseRentEffectForUi(RentCard rc, Player landlord, CardColor declared, GameSession session) {
        return canUseRentEffect(rc, landlord, declared) && session != null && landlord != null && !rentersExcludingLandlord(landlord, session).isEmpty();
    }
    public static List<Player> rentersExcludingLandlord(Player landlord, GameSession session) {
        List<Player> out = new ArrayList<>(); if (landlord == null || session == null) return out; for (Player p : session.getPlayers()) if (p != null && !p.equals(landlord)) out.add(p); return out;
    }
    public static Player resolvedRentPayer(Player landlord, GameSession session, CardPlayOptions opt) {
        List<Player> renters = rentersExcludingLandlord(landlord, session); if (renters.isEmpty()) return null; Player pick = opt != null ? opt.actionTargetPlayer() : null; if (pick != null && renters.contains(pick)) return pick; if (landlord != null && landlord.isAI()) return renters.get(0); return null;
    }
}
