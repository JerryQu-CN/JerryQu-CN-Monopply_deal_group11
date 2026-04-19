package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.ai.BotPolicy;

/**
 * 机器人玩家（策略模式，设计图 ComputerPlayer + Strategy）。
 *
 * TODO(model+ai): 持有 {@link BotPolicy}，在轮到 AI 时由 {@link com.example.monopoly_deal_game.logic.TurnManager} 触发决策。
 */
public class ComputerPlayer extends Player {

    private BotPolicy policy;

    @Override
    public boolean isComputer() {
        return true;
    }

    public BotPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(BotPolicy policy) {
        this.policy = policy;
    }
}
