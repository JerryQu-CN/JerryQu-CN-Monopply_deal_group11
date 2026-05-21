package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;

/**
 * 回合与「本回合已打出张数」管理（需求 3.1–3.2：0–3 张，Just Say No 豁免）。
 */
public class TurnManager {

    private static final int MAX_PLAYS_PER_TURN = 3;

    public void beginTurn(GameSession session) {
        GameState state = session.getGameState();

        state.setCardsPlayedThisTurn(0);
        state.setHasDrawnThisTurn(false);
        state.setPhase(GameState.Phase.DRAW_PHASE);

        if (session.getCurrentPlayer() != null) {
            System.out.println("TurnManager: New turn started for " + session.getCurrentPlayer().getName());
        }
    }

    /**
     * @param countsTowardPlayLimit {@code false} 表示 Just Say No 等不计入 3 张限额
     */
    public void onCardPlayed(GameSession session, boolean countsTowardPlayLimit) {
        GameState state = session.getGameState();

        if (countsTowardPlayLimit) {
            int currentCount = state.getCardsPlayedThisTurn();
            state.setCardsPlayedThisTurn(currentCount + 1);
            System.out.println("TurnManager: Action counted. Total: " + state.getCardsPlayedThisTurn());
        } else {
            System.out.println("TurnManager: Action exempted (e.g., Just Say No).");
        }
    }

    public boolean canPlayMore(GameSession session) {
        GameState state = session.getGameState();
        return state.isHasDrawnThisTurn() && state.getCardsPlayedThisTurn() < MAX_PLAYS_PER_TURN;
    }
}
