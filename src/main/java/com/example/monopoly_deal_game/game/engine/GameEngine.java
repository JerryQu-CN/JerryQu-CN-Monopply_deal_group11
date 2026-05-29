package com.example.monopoly_deal_game.game.engine;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.GameLogic;

import com.example.monopoly_deal_game.model.Player;

import java.util.Objects;

/**
 * Process-level facade: creates/restores {@link GameSession}, holds a game state shared with the UI, delegates to {@link GameLogic} for rule execution.
 */
public class GameEngine {

    public interface StateListener {
        void onSessionChanged(GameSession session);
    }

    private final GameLogic gameLogic = new GameLogic();
    private GameSession currentSession;
    private StateListener stateListener;

    /**
     * Optional launch hook (e.g., initial screen resource loading); currently no mandatory logic.
     */
    public void launchGame() {
        // Reserved: global warm-up, config loading
    }

    /**
     * LAN game: creates all-human seats; the network layer will sync host/player names from the lobby to these seats.
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

    /** Inject a restored session from a saved game or other source. */
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
