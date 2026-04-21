package com.example.monopoly_deal_game.app;

import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.controller.ScreenNavigation;
import com.example.monopoly_deal_game.controller.StageAware;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX 应用入口（原 HelloApplication 已按课设命名规范更名）。
 *
 * TODO(app): 如需启动前加载配置、单例 {@link com.example.monopoly_deal_game.logic.GameEngine} 预热，可在此扩展。
 */
public class MonopolyDealApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        AppContext.install(AppContext.createDefault());
        FXMLLoader fxmlLoader = new FXMLLoader(ScreenNavigation.fxmlUrl("StartScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), ScreenNavigation.SCENE_WIDTH, ScreenNavigation.SCENE_HEIGHT);
        Object controller = fxmlLoader.getController();
        if (controller instanceof StageAware stageAware) {
            stageAware.setStage(stage);
        }
        stage.setTitle("Monopoly Deal");
        stage.setScene(scene);
        stage.show();
    }
}
