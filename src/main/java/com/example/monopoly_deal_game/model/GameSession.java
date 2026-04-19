package com.example.monopoly_deal_game.model;

import java.util.List;

/**
 * 一局游戏的聚合根：玩家列表 + 牌堆 + {@link GameState}。
 *
 * TODO(model): 抽牌堆/弃牌堆；需求 1.4–1.5、2.4、3.6 的数据都在这里或委托给 {@link com.example.monopoly_deal_game.logic.CardManager}。
 */
public class GameSession {

    // TODO(model): List<Player> players; Deque<Card> drawPile; List<Card> discardPile; GameState state;

    public List<Player> getPlayers() {
        throw new UnsupportedOperationException("TODO(model): implement");
    }
}
