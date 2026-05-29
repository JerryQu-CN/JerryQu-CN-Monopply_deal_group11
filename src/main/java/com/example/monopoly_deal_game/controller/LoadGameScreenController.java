package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
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
 * Load game screen: enter the game after path validation.
 *
 * TODO(controller+persistence): Load {@link com.example.monopoly_deal_game.game.model.GameSession}
 * using {@link com.example.monopoly_deal_game.persistence.SaveGameService#load(java.nio.file.Path)}
 * and pass it to {@link com.example.monopoly_deal_game.game.engine.GameEngine} for recovery.
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
        String raw = savePathField.getText();
        if (raw == null || raw.isBlank()) {
            return;
        }
        Path path = Path.of(raw.strip());
        try {
            GameSession session = AppContext.get().saveGameService().load(path);
            if (session != null) {
                AppContext.get().gameEngine().resumeSession(session);
                ScreenNavigation.show(stage, ScreenNavigation.GAMEPLAY_FXML);
            }
        } catch (UnsupportedOperationException e) {
            statusLabel.setText("Load not implemented yet — choose another mode.");
            statusLabel.setStyle("-fx-text-fill: #ffcccc;");
        }
    }

    @FXML
    void onBack(ActionEvent event) {
        ScreenNavigation.show(stage, "StartScreen.fxml");
    }
}
