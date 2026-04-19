package com.example.monopoly_deal_game.ai;

import com.example.monopoly_deal_game.logic.PlayerCommand;
import com.example.monopoly_deal_game.model.GameSession;

/**
 * 占位：随机合法行动（待 logic 暴露枚举后实现）。
 *
 * TODO(ai): 枚举合法 {@link PlayerCommand} 后随机选择。
 */
public final class RandomLegalBotPolicy implements BotPolicy {

    @Override
    public PlayerCommand chooseCommand(String botPlayerId, GameSession session) {
        throw new UnsupportedOperationException("TODO(ai): implement RandomLegalBotPolicy");
    }
}
