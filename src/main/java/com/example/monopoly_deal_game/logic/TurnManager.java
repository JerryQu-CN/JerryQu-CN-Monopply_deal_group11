package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.ActionStatePlayerTurn;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.model.Player;

/**
 * 回合与出牌张数管理（对齐 Monopoly-Deal-main 中的 ActionStatePlayerTurn 状态机）。
 */
public class TurnManager {

    public void beginTurn(GameSession session) {
        GameState state = session.getGameState();
        Player current = session.getCurrentPlayer();
        if (current == null) return;

        ActionStatePlayerTurn turnState = new ActionStatePlayerTurn(current, GameConfig.MAX_PLAY_PER_TURN);
        state.setTurnState(turnState);
        state.clearActionStates();
        state.setCardsPlayedThisTurn(0);
        state.setDoubleNextRent(false);
        state.setDoubleRentCount(0);
    }

    public void onCardPlayed(GameSession session, boolean countsTowardPlayLimit) {
        GameState state = session.getGameState();
        if (countsTowardPlayLimit) {
            state.setCardsPlayedThisTurn(state.getCardsPlayedThisTurn() + 1);
        }
        if (state.hasTurnState() && countsTowardPlayLimit) {
            state.getTurnState().decrementMoves();
        }
    }

    public boolean canPlayMore(GameSession session) {
        GameState state = session.getGameState();
        if (!state.hasTurnState()) return false;
        ActionStatePlayerTurn ts = state.getTurnState();
        return ts.hasDrawn() && ts.canPlayCards();
    }
}