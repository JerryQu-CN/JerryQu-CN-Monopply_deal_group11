package com.example.monopoly_deal_game.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * 读档界面：路径校验后进入对局。
 *
 * TODO(controller+persistence): 使用 {@link com.example.monopoly_deal_game.persistence.SaveGameService#load(java.nio.file.Path)}
 * 得到 {@link com.example.monopoly_deal_game.model.GameSession}，再交给 {@link com.example.monopoly_deal_game.logic.GameEngine} 恢复。
 */
public class LoadGameScreenController implements StageAware, Initializable {

    @FXML
    private TextField savePathField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button startGameButton;

    private Stage stage;

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var valid = Bindings.createBooleanBinding(
                () -> validatePath(savePathField.getText()),
                savePathField.textProperty());
        startGameButton.disableProperty().bind(valid.not());

        savePathField.textProperty().addListener((obs, oldV, newV) -> updateStatus(newV));
        updateStatus(savePathField.getText());
    }

    private void updateStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            statusLabel.setText("");
            return;
        }
        if (validatePath(raw)) {
            statusLabel.setText("Valid save file found.");
            statusLabel.setStyle("-fx-text-fill: #ccffcc;");
        } else {
            statusLabel.setText("No valid save game found");
            statusLabel.setStyle("-fx-text-fill: #ffcccc;");
        }
    }

    private static boolean validatePath(String raw) {
        if (raw == null) {
            return false;
        }
        String s = raw.strip();
        if (s.isEmpty()) {
            return false;
        }
        try {
            Path p = Path.of(s);
            return Files.exists(p) && Files.isRegularFile(p) && Files.isReadable(p);
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    void onStartGame(ActionEvent event) {
<<<<<<< HEAD
        String raw = savePathField.getText();
        if (raw == null || raw.isBlank()) {
            return;
        }
        Path path = Path.of(raw.strip());
        try {
            var session = AppContext.get().saveGameService().load(path);
            // TODO(controller+logic): GameEngine 提供 resume(session) 后再依赖返回值决定是否跳转
            if (session == null) {
                return;
            }
        } catch (UnsupportedOperationException ignored) {
            // persistence 未实现时仍进入对局界面便于调试 UI
        }
=======
        // TODO(controller): SaveGameService.load → GameEngine 恢复会话后再 navigate
>>>>>>> ec928dc (Initial commit: rename folder and add all files)
        ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
    }

    @FXML
    void onBack(ActionEvent event) {
        ScreenNavigation.show(stage, "StartScreen.fxml");
    }
}
