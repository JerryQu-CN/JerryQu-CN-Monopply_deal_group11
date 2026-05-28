package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.*;

import java.util.ArrayList;
import java.util.List;

public final class PlayEligibility {

    private PlayEligibility() {}

    public static boolean canUseActionEffect(ActionCard ac, GameSession session, CardPlayOptions opt) {
        if (ac == null || session == null) return false;
        if (opt == null) opt = CardPlayOptions.auto();
        Player cur = session.getCurrentPlayer();
        if (cur == null) return false;

        if (ac instanceof ActionCardJustSayNo
                || ac instanceof ActionCardPassGo
                || ac instanceof ActionCardDoubleTheRent
                || ac instanceof ActionCardItsMyBirthday) {
            return true;
        }
        if (ac instanceof ActionCardDebtCollector) {
            return resolvedActionTarget(cur, session, opt) != null;
        }
        if (ac instanceof ActionCardSlyDeal) {
            Player t = resolvedActionTarget(cur, session, opt);
            return t != null && PropertyQuery.firstStealableProperty(t) != null;
        }
        if (ac instanceof ActionCardForcedDeal) {
            PropertyCard mine = PropertyQuery.firstTableProperty(cur);
            Player opp = resolvedActionTarget(cur, session, opt);
            PropertyCard theirs = opp != null ? PropertyQuery.firstTableProperty(opp) : null;
            return mine != null && opp != null && theirs != null;
        }
        if (ac instanceof ActionCardDealBreaker) {
            Player t = resolvedActionTarget(cur, session, opt);
            return t != null && PropertyQuery.firstMonopolyOfPlayer(t) != null;
        }
        if (ac instanceof ActionCardHouse) {
            return PropertyQuery.findHouseTarget(cur, true) != null;
        }
        if (ac instanceof ActionCardHotel) {
            return PropertyQuery.findHouseTarget(cur, false) != null;
        }
        return false;
    }

    public static boolean canUseActionEffectForUi(ActionCard ac, GameSession session) {
        CardPlayOptions auto = CardPlayOptions.auto();
        if (canUseActionEffect(ac, session, auto)) return true;
        if (!needsChosenOpponent(ac)) return false;
        Player cur = session.getCurrentPlayer();
        if (cur == null) return false;
        return hasAnyEligibleTarget(cur, session, ac);
    }

    public static boolean needsChosenOpponent(ActionCard ac) {
        if (ac == null) return false;
        return ac instanceof ActionCardDebtCollector
                || ac instanceof ActionCardSlyDeal
                || ac instanceof ActionCardForcedDeal
                || ac instanceof ActionCardDealBreaker;
    }

    private static boolean hasAnyEligibleTarget(Player actor, GameSession session, ActionCard ac) {
        if (ac instanceof ActionCardDebtCollector) {
            return !requiredOpponents(actor, session).isEmpty();
        }
        if (ac instanceof ActionCardSlyDeal) {
            return requiredOpponents(actor, session).stream()
                    .anyMatch(o -> PropertyQuery.firstStealableProperty(o) != null);
        }
        if (ac instanceof ActionCardForcedDeal) {
            if (PropertyQuery.firstTableProperty(actor) == null) return false;
            return requiredOpponents(actor, session).stream()
                    .anyMatch(o -> PropertyQuery.firstTableProperty(o) != null);
        }
        if (ac instanceof ActionCardDealBreaker) {
            return requiredOpponents(actor, session).stream()
                    .anyMatch(o -> PropertyQuery.firstMonopolyOfPlayer(o) != null);
        }
        return false;
    }

    public static Player requiredOpponent(Player actor, GameSession session, CardPlayOptions opt) {
        if (actor == null || session == null) return null;
        Player pick = opt != null ? opt.actionTargetPlayer() : null;
        List<Player> others = requiredOpponents(actor, session);
        if (pick != null && others.contains(pick)) return pick;
        if (actor.isAI() && others.size() == 1) return others.get(0);
        return null;
    }

    private static List<Player> requiredOpponents(Player actor, GameSession session) {
        List<Player> out = new ArrayList<>();
        for (Player p : session.getPlayers()) {
            if (p != null && !p.equals(actor)) out.add(p);
        }
        return out;
    }

    public static Player resolvedActionTarget(Player actor, GameSession session, CardPlayOptions opt) {
        Player p = requiredOpponent(actor, session, opt);
        if (p != null) return p;
        if (actor == null || session == null || !actor.isAI()) return null;
        List<Player> elig = eligibleOpponentsForAction(actor, session, null);
        return elig.isEmpty() ? null : elig.get(0);
    }

    public static List<Player> eligibleOpponentsForAction(Player actor, GameSession session, ActionCard ac) {
        List<Player> all = requiredOpponents(actor, session);
        if (actor == null) return List.of();
        if (ac instanceof ActionCardDebtCollector) {
            return new ArrayList<>(all);
        }
        if (ac instanceof ActionCardSlyDeal) {
            List<Player> ok = new ArrayList<>();
            for (Player o : all) if (PropertyQuery.firstStealableProperty(o) != null) ok.add(o);
            return ok;
        }
        if (ac instanceof ActionCardForcedDeal) {
            if (PropertyQuery.firstTableProperty(actor) == null) return List.of();
            List<Player> ok = new ArrayList<>();
            for (Player o : all) if (PropertyQuery.firstTableProperty(o) != null) ok.add(o);
            return ok;
        }
        if (ac instanceof ActionCardDealBreaker) {
            List<Player> ok = new ArrayList<>();
            for (Player o : all) if (PropertyQuery.firstMonopolyOfPlayer(o) != null) ok.add(o);
            return ok;
        }
        return List.of();
    }
}