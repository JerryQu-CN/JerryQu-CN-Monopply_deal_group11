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
        // TODO(controller): SaveGameService.load → GameEngine 恢复会话后再 navigate
        ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
    }

    @FXML
    void onBack(ActionEvent event) {
        ScreenNavigation.show(stage, "StartScreen.fxml");
    }
}
