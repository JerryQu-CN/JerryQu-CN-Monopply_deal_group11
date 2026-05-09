package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;

/**
 * 回合与「本回合已打出张数」管理（需求3.1–3.2）。
 * 核心职责：计数出牌张数，处理 Just Say No 的豁免规则。
 */
public class TurnManager {

    private static final int MAX_PLAYS_PER_TURN = 3;

    /**
     * 开始新回合：重置当前玩家的计数状态
     */
    public void beginTurn(GameSession session) {
        GameState state = session.getGameState();

        // 1. 重置出牌计数
        state.setCardsPlayedThisTurn(0);

        // 2. 重置摸牌标记（每回合必须先摸牌）
        state.setHasDrawnThisTurn(false);

        // 3. 设置阶段为摸牌阶段
        state.setPhase(GameState.Phase.DRAW_PHASE);

        System.out.println("TurnManager: New turn started for " + session.getCurrentPlayer().getName());
    }

    /**
     * 核心逻辑：记录出牌次数
     * @param countsTowardPlayLimit 是否计入 3 张限制（Just Say No 为 false）
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

    /**
     * 校验当前是否还能出牌
     */
    public boolean canPlayMore(GameSession session) {
        GameState state = session.getGameState();
        return state.isHasDrawnThisTurn() && state.getCardsPlayedThisTurn() < MAX_PLAYS_PER_TURN;
    }
}