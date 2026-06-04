package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkLobbyState;

import javafx.application.Platform;

import java.util.Objects;

/**
 * Singleton dependency container providing shared access to {@link GameEngine},
 * {@link GameServer}, and {@link NetworkLobbyState} across all controllers.
 */
public final class AppContext {

    private static volatile AppContext instance;

    private final GameEngine gameEngine;
    private final GameServer gameServer;
    private final NetworkLobbyState networkLobbyState;

    public AppContext(GameEngine gameEngine, GameServer gameServer) {
        this.gameEngine = Objects.requireNonNull(gameEngine);
        this.gameServer = Objects.requireNonNull(gameServer);
        this.networkLobbyState = new NetworkLobbyState();
        this.gameEngine.setStateListener(session -> this.networkLobbyState.setSession(session));
    }

    /** Default implementation: one engine per process. */
    public static AppContext createDefault() {
        GameEngine engine = new GameEngine();
        AppContext ctx = new AppContext(engine, new GameServer(engine));
        engine.setStateListener(session -> {
            if (Platform.isFxApplicationThread()) {
                if (instance != null) {
                    instance.networkLobbyState.setSession(session);
                }
            } else {
                Platform.runLater(() -> {
                    if (instance != null) {
                        instance.networkLobbyState.setSession(session);
                    }
                });
            }
        });
        return ctx;
    }

    public static void install(AppContext ctx) {
        instance = Objects.requireNonNull(ctx);
    }

    public static AppContext get() {
        if (instance == null) {
            throw new IllegalStateException("AppContext not installed; call AppContext.install from Application.start");
        }
        return instance;
    }

    public GameEngine gameEngine() {
        return gameEngine;
    }

    public GameServer gameServer() {
        return gameServer;
    }

    public NetworkLobbyState networkLobbyState() { return networkLobbyState; }
}
