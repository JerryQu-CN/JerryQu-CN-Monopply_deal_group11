package com.example.monopoly_deal_game.controller;

import javafx.stage.Stage;

/**需要主舞台引用以便 {@link ScreenNavigation} 切换场景的控制器。 */
public interface StageAware {
    void setStage(Stage stage);
}
