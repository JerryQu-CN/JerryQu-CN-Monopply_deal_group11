package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.persistence.SaveGameService;

import javafx.application.Platform;

import java.util.Objects;

/**
 * 应用级依赖容器（课设阶段无 Spring 时的轻量替代）：在 {@link com.example.monopoly_deal_game.app.MonopolyDealApplication}
 * 启动时注入一次，各 {@code *ScreenController} 通过 {@link #get()} 取得共享实例。
 */
public final class AppContext {

    private static volatile AppContext instance;

    private final GameEngine gameEngine;
    private final GameServer gameServer;
    private final NetworkLobbyState networkLobbyState;
    private final SaveGameService saveGameService;

    public AppContext(GameEngine gameEngine, GameServer gameServer, SaveGameService saveGameService) {
        this.gameEngine = Objects.requireNonNull(gameEngine);
        this.gameServer = Objects.requireNonNull(gameServer);
        this.networkLobbyState = new NetworkLobbyState();
        this.saveGameService = Objects.requireNonNull(saveGameService);
        this.gameEngine.setStateListener(session -> this.networkLobbyState.setSession(session));
    }

    /** 默认实现：单进程一个引擎、一个存档服务。 */
    public static AppContext createDefault() {
        GameEngine engine = new GameEngine();
        AppContext ctx = new AppContext(engine, new GameServer(engine), new SaveGameService());
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

    public SaveGameService saveGameService() {
        return saveGameService;
    }
}
