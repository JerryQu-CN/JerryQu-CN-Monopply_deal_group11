package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.List;

/**
 * 行动牌使用效果的前置判断（银行面值不在此校验）。
 */
public final class PlayEligibility {

    private PlayEligibility() {}

    public static boolean canUseActionEffect(ActionCard ac, GameSession session, CardPlayOptions opt) {
        if (ac == null || session == null) return false;
        if (opt == null) opt = CardPlayOptions.auto();
        Player cur = session.getCurrentPlayer();
        if (cur == null) return false;
        return switch (ac.getActionType()) {
            case JUST_SAY_NO -> true;
            case PASS_GO, DOUBLE_RENT, ITS_MY_BIRTHDAY -> true;
            case DEBT_COLLECTOR ->
                    resolvedActionTarget(cur, session, opt, ac.getActionType()) != null;
            case SLY_DEAL -> {
                Player t = resolvedActionTarget(cur, session, opt, ac.getActionType());
                yield t != null && PropertyQuery.firstStealableProperty(t) != null;
            }
            case FORCE_DEAL -> {
                PropertyCard mine = PropertyQuery.firstTableProperty(cur);
                Player opp = resolvedActionTarget(cur, session, opt, ac.getActionType());
                PropertyCard theirs = opp != null ? PropertyQuery.firstTableProperty(opp) : null;
                yield mine != null && opp != null && theirs != null;
            }
            case DEAL_BREAKER -> {
                Player t = resolvedActionTarget(cur, session, opt, ac.getActionType());
                yield t != null && PropertyQuery.firstMonopolyOfPlayer(t) != null;
            }
            case HOUSE -> PropertyQuery.findHouseTarget(cur, true) != null;
            case HOTEL -> PropertyQuery.findHouseTarget(cur, false) != null;
        };
    }

    public static boolean canUseActionEffectForUi(ActionCard ac, GameSession session) {
        CardPlayOptions auto = CardPlayOptions.auto();
        if (canUseActionEffect(ac, session, auto)) return true;
        if (!needsChosenOpponent(ac)) return false;
        Player cur = session.getCurrentPlayer();
        if (cur == null) return false;
        return hasAnyEligibleTarget(cur, session, ac.getActionType());
    }

    public static boolean needsChosenOpponent(ActionCard ac) {
        if (ac == null) return false;
        return switch (ac.getActionType()) {
            case DEBT_COLLECTOR, SLY_DEAL, FORCE_DEAL, DEAL_BREAKER -> true;
            default -> false;
        };
    }

    private static boolean hasAnyEligibleTarget(Player actor, GameSession session,
                                                 ActionCard.ActionType type) {
        return switch (type) {
            case DEBT_COLLECTOR -> !requiredOpponents(actor, session).isEmpty();
            case SLY_DEAL -> requiredOpponents(actor, session).stream()
                    .anyMatch(o -> PropertyQuery.firstStealableProperty(o) != null);
            case FORCE_DEAL -> {
                if (PropertyQuery.firstTableProperty(actor) == null) yield false;
                yield requiredOpponents(actor, session).stream()
                        .anyMatch(o -> PropertyQuery.firstTableProperty(o) != null);
            }
            case DEAL_BREAKER -> requiredOpponents(actor, session).stream()
                    .anyMatch(o -> PropertyQuery.firstMonopolyOfPlayer(o) != null);
            default -> false;
        };
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

    /** 出牌结算用：真人须事先在 UI 选人；AI 取第一个合法目标。 */
    public static Player resolvedActionTarget(Player actor, GameSession session,
                                               CardPlayOptions opt, ActionCard.ActionType type) {
        Player p = requiredOpponent(actor, session, opt);
        if (p != null) return p;
        if (actor == null || session == null || !actor.isAI()) return null;
        List<Player> elig = eligibleOpponentsForAction(actor, session, type);
        return elig.isEmpty() ? null : elig.get(0);
    }

    public static List<Player> eligibleOpponentsForAction(Player actor, GameSession session,
                                                           ActionCard.ActionType type) {
        List<Player> all = requiredOpponents(actor, session);
        if (actor == null) return List.of();
        return switch (type) {
            case DEBT_COLLECTOR -> new ArrayList<>(all);
            case SLY_DEAL -> {
                List<Player> ok = new ArrayList<>();
                for (Player o : all) if (PropertyQuery.firstStealableProperty(o) != null) ok.add(o);
                yield ok;
            }
            case FORCE_DEAL -> {
                if (PropertyQuery.firstTableProperty(actor) == null) yield List.of();
                List<Player> ok = new ArrayList<>();
                for (Player o : all) if (PropertyQuery.firstTableProperty(o) != null) ok.add(o);
                yield ok;
            }
            case DEAL_BREAKER -> {
                List<Player> ok = new ArrayList<>();
                for (Player o : all) if (PropertyQuery.firstMonopolyOfPlayer(o) != null) ok.add(o);
                yield ok;
            }
            default -> List.of();
        };
    }

}