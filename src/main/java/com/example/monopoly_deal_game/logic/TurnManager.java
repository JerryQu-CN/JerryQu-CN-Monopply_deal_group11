package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.GameSession;

/**
 * 回合与「本回合已打出张数」管理（需求3.1–3.2：0–3 张，Just Say No 豁免）。
 *
 * TODO(logic): 与 {@link GameState} 中阶段字段同步；机器人回合调用 {@link com.example.monopoly_deal_game.ai.BotPolicy}。
 */
public class TurnManager {

    public void beginTurn(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): beginTurn");
    }

    public void onCardPlayed(GameSession session, boolean countsTowardPlayLimit) {
        throw new UnsupportedOperationException("TODO(logic): countsTowardPlayLimit=false 为 Just Say No 等");
    }
}
