package com.example.monopoly_deal_game.game.engine;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.GameLogic;

/**
 * 进程级门面：启动本地/局域网联机对局（架构图 Core 层 GameEngine）。
 *
 * TODO(logic): 单例或依赖注入容器管理；持有 {@link GameLogic}、{@link com.example.monopoly_deal_game.game.rules.GameConfig}、当前 {@link GameSession}。
 */
public class GameEngine {

    private final GameLogic gameLogic = new GameLogic();

    public void launchGame() {
        throw new UnsupportedOperationException("TODO(logic): 总入口，加载资源/显示首屏后可不做事");
    }

    /**
     * 纯本地：人类 + 机器人。
     *
     * TODO(logic): 创建 {@link GameSession}，{@link GameLogic#initGame(GameSession)}。
     */
    public GameSession startLocalGame(int humanCount, int botCount) {
        throw new UnsupportedOperationException("TODO(logic): startLocalGame");
    }

    /**
     * 局域网联机（主机权威）。
     *
     * TODO(logic+network): 与 {@link com.example.monopoly_deal_game.network.GameServer} 绑定同一 {@link GameLogic} 实例。
     */
    public void startLanGame() {
        throw new UnsupportedOperationException("TODO(logic+network): startLanGame");
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}
