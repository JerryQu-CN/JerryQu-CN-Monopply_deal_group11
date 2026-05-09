package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * 一局游戏的聚合根：玩家列表 + 状态。
 */
public class GameSession {

    private List<Player> players = new ArrayList<>();
    private GameState gameState = new GameState();

    /**
     * 获取玩家列表
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * 获取当前对局的实时状态对象。
     */
    public GameState getGameState() {
        // TODO: 返回内部 state 实例
        return null;
    }

    /**
     * 获取当前正在进行回合的玩家实例。
     */
    public Player getCurrentPlayer() {
        // TODO: 根据 gameState 的 index 从 players 列表获取
        return null;
    }
}