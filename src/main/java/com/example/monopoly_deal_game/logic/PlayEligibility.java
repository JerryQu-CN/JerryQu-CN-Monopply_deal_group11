package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.List;

/**
 * 判断行动牌「使用效果」是否满足前提（作银行面值时不在此校验）。
 */
public final class PlayEligibility {

    private PlayEligibility() {}

    public static boolean canUseActionEffect(
            ActionCard ac, GameSession session, CardPlayOptions opt) {
        if (ac == null || session == null) {
            return false;
        }
        if (opt == null) {
            opt = CardPlayOptions.auto();
        }
        Player cur = session.getCurrentPlayer();
        if (cur == null) {
            return false;
        }
        return switch (ac.getActionType()) {
            case JUST_SAY_NO -> true;
            case PASS_GO, DOUBLE_RENT, ITS_MY_BIRTHDAY -> true;
            case DEBT_COLLECTOR ->
                    resolvedActionTarget(cur, session, opt, ac.getActionType()) != null;
            case SLY_DEAL -> {
                Player t = resolvedActionTarget(cur, session, opt, ac.getActionType());
                yield t != null && firstStealableProperty(t) != null;
            }
            case FORCE_DEAL -> {
                PropertyCard mine = firstTableProperty(cur);
                Player opp = resolvedActionTarget(cur, session, opt, ac.getActionType());
                PropertyCard theirs = opp != null ? firstTableProperty(opp) : null;
                yield mine != null && opp != null && theirs != null;
            }
            case DEAL_BREAKER -> {
                Player t = resolvedActionTarget(cur, session, opt, ac.getActionType());
                yield t != null && firstMonopolyOfPlayer(t) != null;
            }
            case HOUSE -> findHouseTarget(cur, true) != null;
            case HOTEL -> findHouseTarget(cur, false) != null;
        };
    }

    /**
     * 出牌条按钮：若尚无明确对手但多名玩家可满足条件，仍可点开后由弹窗选人。
     */
    public static boolean canUseActionEffectForUi(ActionCard ac, GameSession session) {
        CardPlayOptions auto = CardPlayOptions.auto();
        if (canUseActionEffect(ac, session, auto)) {
            return true;
        }
        if (!needsChosenOpponent(ac)) {
            return false;
        }
        Player cur = session.getCurrentPlayer();
        if (cur == null) {
            return false;
        }
        return hasAnyEligibleTarget(cur, session, ac.getActionType());
    }

    public static boolean needsChosenOpponent(ActionCard ac) {
        if (ac == null) {
            return false;
        }
        return switch (ac.getActionType()) {
            case DEBT_COLLECTOR, SLY_DEAL, FORCE_DEAL, DEAL_BREAKER -> true;
            default -> false;
        };
    }

    private static boolean hasAnyEligibleTarget(
            Player actor, GameSession session, ActionCard.ActionType type) {
        return switch (type) {
            case DEBT_COLLECTOR -> !requiredOpponents(actor, session).isEmpty();
            case SLY_DEAL ->
                    requiredOpponents(actor, session).stream()
                            .anyMatch(o -> firstStealableProperty(o) != null);
            case FORCE_DEAL -> {
                if (firstTableProperty(actor) == null) {
                    yield false;
                }
                yield requiredOpponents(actor, session).stream()
                        .anyMatch(o -> firstTableProperty(o) != null);
            }
            case DEAL_BREAKER ->
                    requiredOpponents(actor, session).stream()
                            .anyMatch(o -> firstMonopolyOfPlayer(o) != null);
            default -> false;
        };
    }

    /** 显式所选时返回对手；真人玩家必须由 UI 明确选择，AI 才允许自动取唯一目标。 */
    public static Player requiredOpponent(
            Player actor, GameSession session, CardPlayOptions opt) {
        if (actor == null || session == null) {
            return null;
        }
        Player pick = opt != null ? opt.actionTargetPlayer() : null;
        List<Player> others = requiredOpponents(actor, session);
        if (pick != null && others.contains(pick)) {
            return pick;
        }
        if (actor.isAI() && others.size() == 1) {
            return others.get(0);
        }
        return null;
    }

    private static List<Player> requiredOpponents(Player actor, GameSession session) {
        List<Player> out = new ArrayList<>();
        for (Player p : session.getPlayers()) {
            if (p != null && !p.equals(actor)) {
                out.add(p);
            }
        }
        return out;
    }

    /** 出牌结算用：真人须事先在 UI 选人；多名对手时 AI 取 {@link #eligibleOpponentsForAction} 的第一个。 */
    public static Player resolvedActionTarget(
            Player actor, GameSession session, CardPlayOptions opt, ActionCard.ActionType type) {
        Player p = requiredOpponent(actor, session, opt);
        if (p != null) {
            return p;
        }
        if (actor == null || session == null || !actor.isAI()) {
            return null;
        }
        List<Player> elig = eligibleOpponentsForAction(actor, session, type);
        return elig.isEmpty() ? null : elig.get(0);
    }

    public static List<Player> eligibleOpponentsForAction(
            Player actor, GameSession session, ActionCard.ActionType type) {
        List<Player> all = requiredOpponents(actor, session);
        if (actor == null) {
            return List.of();
        }
        return switch (type) {
            case DEBT_COLLECTOR -> new ArrayList<>(all);
            case SLY_DEAL -> {
                List<Player> ok = new ArrayList<>();
                for (Player o : all) {
                    if (firstStealableProperty(o) != null) {
                        ok.add(o);
                    }
                }
                yield ok;
            }
            case FORCE_DEAL -> {
                if (firstTableProperty(actor) == null) {
                    yield List.of();
                }
                List<Player> ok = new ArrayList<>();
                for (Player o : all) {
                    if (firstTableProperty(o) != null) {
                        ok.add(o);
                    }
                }
                yield ok;
            }
            case DEAL_BREAKER -> {
                List<Player> ok = new ArrayList<>();
                for (Player o : all) {
                    if (firstMonopolyOfPlayer(o) != null) {
                        ok.add(o);
                    }
                }
                yield ok;
            }
            default -> List.of();
        };
    }

    private static Property firstMonopolyOfPlayer(Player p) {
        if (p == null) {
            return null;
        }
        for (Property row : p.getProperties()) {
            if (row.isMonopoly()) {
                return row;
            }
        }
        return null;
    }

    private static PropertyCard firstTableProperty(Player p) {
        for (Property row : p.getProperties()) {
            if (!row.getCards().isEmpty()) {
                return row.getCards().get(0);
            }
        }
        return null;
    }

    private static PropertyCard firstStealableProperty(Player p) {
        for (Property row : p.getProperties()) {
            if (row.isMonopoly()) {
                continue;
            }
            if (!row.getCards().isEmpty()) {
                return row.getCards().get(0);
            }
        }
        return null;
    }

    private static Property findHouseTarget(Player actor, boolean forHouse) {
        for (Property ps : actor.getProperties()) {
            if (!ps.isMonopoly()) {
                continue;
            }
            boolean hasH =
                    ps.getBuildingCards().stream()
                            .anyMatch(
                                    c ->
                                            c instanceof ActionCard x
                                                    && x.getActionType() == ActionCard.ActionType.HOUSE);
            boolean hasT =
                    ps.getBuildingCards().stream()
                            .anyMatch(
                                    c ->
                                            c instanceof ActionCard x
                                                    && x.getActionType()
                                                            == ActionCard.ActionType.HOTEL);
            if (forHouse && !hasH) {
                return ps;
            }
            if (!forHouse && hasH && !hasT) {
                return ps;
            }
        }
        return null;
    }
}
