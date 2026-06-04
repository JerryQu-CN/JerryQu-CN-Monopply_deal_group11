package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;

import java.util.List;

/**
 * Thin facade that delegates eligibility queries to polymorphic methods on {@link ActionCard}.
 */
public final class PlayEligibility {

    private PlayEligibility() {}

    public static boolean canUseActionEffect(ActionCard ac, GameSession session, CardPlayOptions opt) {
        if (ac == null || session == null) return false;
        if (opt == null) opt = CardPlayOptions.auto();
        Player cur = session.getCurrentPlayer();
        return cur != null && ac.canUseEffect(cur, session, opt);
    }

    public static boolean canUseActionEffectForUi(ActionCard ac, GameSession session) {
        if (ac == null || session == null) return false;
        Player cur = session.getCurrentPlayer();
        if (cur == null) return false;
        if (ac.canUseEffect(cur, session, CardPlayOptions.auto())) return true;
        return ac.needsChosenOpponent() && !ac.eligibleOpponents(cur, session).isEmpty();
    }

    public static boolean needsChosenOpponent(ActionCard ac) {
        return ac != null && ac.needsChosenOpponent();
    }

    public static List<Player> eligibleOpponentsForAction(Player actor, GameSession session, ActionCard ac) {
        if (actor == null || ac == null) return List.of();
        return ac.eligibleOpponents(actor, session);
    }

    public static Player resolvedActionTarget(Player actor, GameSession session, CardPlayOptions opt) {
        if (actor == null || session == null) return null;
        Player pick = opt != null ? opt.actionTargetPlayer() : null;
        List<Player> others = session.getPlayers().stream()
                .filter(p -> p != null && !p.equals(actor)).toList();
        if (pick != null && others.contains(pick)) return pick;
        if (actor.isAI() && others.size() == 1) return others.get(0);
        return null;
    }
}
