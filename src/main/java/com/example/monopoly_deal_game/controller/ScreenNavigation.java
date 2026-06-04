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
 * FXML scene navigation utility — loads FXML files and switches the primary stage's scene.
 */
public final class ScreenNavigation {
    public static final double SCENE_WIDTH = 1400;
    public static final double SCENE_HEIGHT = 850;

    private static final String FXML_BASE = "/com/example/monopoly_deal_game/";

    /** Main game screen FXML file name (corresponding to GameView in the design). */
    public static final String GAMEPLAY_FXML = "GameplayScreen.fxml";

    private ScreenNavigation() {}

    private static final String STYLESHEET = FXML_BASE + "game-style.css";

    private static void loadStylesheet(Scene scene) {
        URL css = ScreenNavigation.class.getResource(STYLESHEET);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

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
            Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
            loadStylesheet(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
