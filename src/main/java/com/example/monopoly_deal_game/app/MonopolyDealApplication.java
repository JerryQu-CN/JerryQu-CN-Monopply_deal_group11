package com.example.monopoly_deal_game.app;

import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.controller.ScreenNavigation;
import com.example.monopoly_deal_game.controller.StageAware;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * JavaFX {@link Application} entry point — initializes the game engine and loads the start screen.
 */
public class MonopolyDealApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        AppContext.install(AppContext.createDefault());
        AppContext.get().gameEngine().launchGame();
        FXMLLoader fxmlLoader = new FXMLLoader(ScreenNavigation.fxmlUrl("StartScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), ScreenNavigation.SCENE_WIDTH, ScreenNavigation.SCENE_HEIGHT);
        Object controller = fxmlLoader.getController();
        if (controller instanceof StageAware stageAware) {
            stageAware.setStage(stage);
        }
        URL css = ScreenNavigation.class.getResource("/com/example/monopoly_deal_game/game-style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("Monopoly Deal");
        stage.setScene(scene);
        stage.show();
    }
}
