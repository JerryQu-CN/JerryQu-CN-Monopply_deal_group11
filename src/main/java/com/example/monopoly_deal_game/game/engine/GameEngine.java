package com.example.monopoly_deal_game.game.engine;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.GameLogic;

import com.example.monopoly_deal_game.model.Player;

import java.util.Objects;

/**
 * 进程级门面：创建/恢复 {@link GameSession}，持有与 UI 共享的一局状态，委托 {@link GameLogic} 执行规则。
 */
public class GameEngine {

    public interface StateListener {
        void onSessionChanged(GameSession session);
    }

    private final GameLogic gameLogic = new GameLogic();
    private GameSession currentSession;
    private StateListener stateListener;

    /**
     * 可选启动钩子（首屏资源加载等）；当前无强制逻辑。
     */
    public void launchGame() {
        // 预留：全局预热、配置读取
    }

    /**
     * 联机局：创建全真人席位；网络层会把大厅中的房主/玩家名称同步到这些座位上。
     */
    public GameSession startLanGame(int humanPlayerCount) {
        if (humanPlayerCount < 2 || humanPlayerCount > 5) {
            throw new IllegalArgumentException("LAN need 2–5 human seats, got " + humanPlayerCount);
        }
        GameSession session = new GameSession();
        for (int i = 0; i < humanPlayerCount; i++) {
            String name = i == 0 ? "Host" : "Player " + (i + 1);
            session.getPlayers().add(new Player(name, false));
        }
        gameLogic.initGame(session);
        this.currentSession = session;
        notifyStateChanged();
        return session;
    }

    /** 读档或其它途径注入已恢复的会话。 */
    public void resumeSession(GameSession session) {
        this.currentSession = Objects.requireNonNull(session);
        notifyStateChanged();
    }

    public void clearSession() {
        this.currentSession = null;
        notifyStateChanged();
    }

    public GameSession getCurrentSession() {
        return currentSession;
    }

    public boolean isGameOver() {
        return currentSession != null && currentSession.getGameState().isGameOver();
    }

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    private void notifyStateChanged() {
        if (stateListener != null) {
            stateListener.onSessionChanged(currentSession);
        }
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}
