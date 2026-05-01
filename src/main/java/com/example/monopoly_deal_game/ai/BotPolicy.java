package com.example.monopoly_deal_game.ai;

import com.example.monopoly_deal_game.logic.PlayerCommand;
import com.example.monopoly_deal_game.game.model.GameSession;

/**
 * 机器人决策策略（设计图 Strategy）。
 *
 * TODO(ai): 实现 chooseCommand；需 logic 层提供「当前合法指令枚举」。
 */
public interface BotPolicy {

    PlayerCommand chooseCommand(String botPlayerId, GameSession session);
}
