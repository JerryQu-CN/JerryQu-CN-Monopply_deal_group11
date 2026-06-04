package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.ActionState;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCardJustSayNo;
import com.example.monopoly_deal_game.model.cards.Card;

/**
 * Just Say No mediator: provides UI bridging and query utilities.
 * The actual JSN play flow is driven by GameLogic.playCard(), which calls ActionState.refuse().
 */
public final class JustSayNoMediator {

    private static volatile JustSayNoUi ui;

    private JustSayNoMediator() {}

    @FunctionalInterface
    public interface JustSayNoUi {
        /** @return true if the player agrees to consume a JSN to oppose the current action */
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
        return c instanceof ActionCardJustSayNo;
    }
}