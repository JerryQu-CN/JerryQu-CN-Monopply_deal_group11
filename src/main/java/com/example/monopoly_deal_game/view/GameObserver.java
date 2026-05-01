package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.game.model.GameSession;

/**
 * 观察者：逻辑层/会话状态变化时刷新 UI（设计图 GameObserver）。
 *
 * TODO(view+controller): 在 {@link com.example.monopoly_deal_game.game.engine.GameEngine} 状态变更后调用 {@link #onSessionUpdated(GameSession)}。
 */
public interface GameObserver {

    void onSessionUpdated(GameSession session);
}
