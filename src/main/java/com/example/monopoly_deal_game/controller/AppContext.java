package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.logic.GameEngine;
import com.example.monopoly_deal_game.persistence.SaveGameService;

import java.util.Objects;

/**
 * 应用级依赖容器（课设阶段无 Spring 时的轻量替代）：在 {@link com.example.monopoly_deal_game.app.MonopolyDealApplication}
 * 启动时注入一次，各 {@code *ScreenController} 通过 {@link #get()} 取得共享实例。
 */
public final class AppContext {

    private static volatile AppContext instance;

    private final GameEngine gameEngine;
    private final SaveGameService saveGameService;

    public AppContext(GameEngine gameEngine, SaveGameService saveGameService) {
        this.gameEngine = Objects.requireNonNull(gameEngine);
        this.saveGameService = Objects.requireNonNull(saveGameService);
    }

    /** 默认实现：单进程一个引擎、一个存档服务。 */
    public static AppContext createDefault() {
        return new AppContext(new GameEngine(), new SaveGameService());
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

    public SaveGameService saveGameService() {
        return saveGameService;
    }
}
