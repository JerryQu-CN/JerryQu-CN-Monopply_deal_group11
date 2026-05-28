package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.ActionState;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;

/**
 * Just Say No 中介：提供 UI 桥接和查询工具。
 * 实际 JSN 打出流程由 GameLogic.playCard() 驱动，调用 ActionState.refuse()。
 */
public final class JustSayNoMediator {

    private static volatile JustSayNoUi ui;

    private JustSayNoMediator() {}

    @FunctionalInterface
    public interface JustSayNoUi {
        /** @return 玩家同意消耗一张 JSN 反对当前行动 */
        boolean confirmUseJustSayNo(Player respondent, Player activator, GameSession session, String situationText);
    }

    public static void installUi(JustSayNoUi bridge) {
        ui = bridge;
    }

    public static void clearUi() {
        ui = null;
    }

    public static JustSayNoUi getUiOrNull() {
        return ui;
    }

    /**
     * @deprecated JSN is now handled through the ActionState system.
     * This stub exists for backward compatibility; always returns false.
     */
    @Deprecated
    public static boolean tryBlockAgainstPlayer(
            Player respondent, Player activator, GameSession session, String situationText) {
        // JSN flow is now handled by the ActionState stack:
        // CardEffectExecutor pushes an ActionState with targets,
        // targeted players get JSN dialog, JSN is played via GameLogic.playCard().
        return false;
    }

    /**
     * Check if the given player should be offered a JSN dialog.
     * Returns true when there's a pending action state that can be refused by this player.
     */
    public static boolean shouldOfferJustSayNo(Player player, GameSession session) {
        if (player == null || session == null) return false;
        ActionState as = session.getGameState().getActionState();
        if (as == null || as == session.getGameState().getTurnState()) return false;
        return as.canRefuseAny(player) && findJustSayNoInHand(player) != null;
    }

    public static Card findJustSayNoRespondentHeld(Player p) {
        Card c = findJustSayNoInHand(p);
        if (c != null) return c;
        return findJustSayNoInBank(p);
    }

    private static Card findJustSayNoInHand(Player p) {
        for (Card c : p.getHand().getCards()) {
            if (isJustSayNo(c)) return c;
        }
        return null;
    }

    private static Card findJustSayNoInBank(Player p) {
        for (Card c : p.getBank().getCards()) {
            if (isJustSayNo(c)) return c;
        }
        return null;
    }

    private static boolean isJustSayNo(Card c) {
        return c instanceof ActionCard ac
                && ac.getActionType() == ActionCard.ActionType.JUST_SAY_NO;
    }
}