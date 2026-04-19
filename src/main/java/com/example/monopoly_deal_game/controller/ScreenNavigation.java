package com.example.monopoly_deal_game.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;

/**
 * FXML 场景切换工具类。
 *
 * TODO(controller): 若引入 Spring/JavaFX 依赖注入，可改为导航服务接口。
 */
public final class ScreenNavigation {
    public static final double SCENE_WIDTH = 1400;
    public static final double SCENE_HEIGHT = 850;

    private static final String FXML_BASE = "/com/example/monopoly_deal_game/";

    /** 对局主界面 FXML 文件名（对应设计图中的 GameView）。 */
    public static final String GAMEPLAY_FXML = "GameplayScreen.fxml";

    private ScreenNavigation() {}

    public static URL fxmlUrl(String fxmlResourceName) {
        String path = FXML_BASE + fxmlResourceName;
        return Objects.requireNonNull(
                ScreenNavigation.class.getResource(path),
                "Missing FXML: " + path);
    }

    public static void show(Stage stage, String fxmlResourceName) {
        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl(fxmlResourceName));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof StageAware stageAware) {
                stageAware.setStage(stage);
            }
            stage.setScene(new Scene(root, SCENE_WIDTH, SCENE_HEIGHT));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
