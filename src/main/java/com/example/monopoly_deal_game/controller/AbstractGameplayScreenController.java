package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.view.GameplayUiBundle;
import com.example.monopoly_deal_game.view.GameplayViewCoordinator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code GameplayScreen} / {@code FirstRunScreen} 共用逻辑：FXML 控件注入、菜单遮罩、与 {@link AppContext} 的弱耦合入口。
 */
public abstract class AbstractGameplayScreenController implements StageAware, Initializable {

    private static final double[] UI_SCALE_STEPS = {0.85, 1.0, 1.15, 1.3};
    private final AtomicInteger scaleIndex = new AtomicInteger(1);

    @FXML
    protected AnchorPane gameRoot;

    @FXML
    protected HBox topbar;

    @FXML
    protected Button menuButton;

    @FXML
    protected Button debugButton;

    @FXML
    protected Label topbarTitle;

    @FXML
    protected VBox leftSidebar;

    @FXML
    protected Button uiMinusButton;

    @FXML
    protected Label uiScaleLabel;

    @FXML
    protected Button uiPlusButton;

    @FXML
    protected Pane deckPane;

    @FXML
    protected Pane discardPane;

    @FXML
    protected Pane voidPane;

    @FXML
    protected Label movesLabel;

    @FXML
    protected Button primaryActionButton;

    @FXML
    protected Button undoButton;

    @FXML
    protected Label versionLabel;

    @FXML
    protected Pane opponentsPane;

    @FXML
    protected Pane selfBoardPane;

    @FXML
    protected Pane handPane;

    @FXML
    protected Pane actionLayer;

    @FXML
    protected StackPane menuOverlay;

    @FXML
    protected Button menuReturnButton;

    @FXML
    protected Button menuLeaveButton;

    protected Stage stage;
    protected GameplayViewCoordinator viewCoordinator;

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        versionLabel.setText("dev");
        syncScaleLabel();
        viewCoordinator = new GameplayViewCoordinator(toUiBundle());
    }

    protected GameplayUiBundle toUiBundle() {
        return new GameplayUiBundle(
                gameRoot,
                topbar,
                leftSidebar,
                deckPane,
                discardPane,
                voidPane,
                opponentsPane,
                selfBoardPane,
                handPane,
                actionLayer,
                menuOverlay,
                chatPaneOrNull());
    }

    /** 子类在存在聊天面板时覆盖；默认 {@code null}。 */
    protected Pane chatPaneOrNull() {
        return null;
    }

    @FXML
    protected void onMenu(ActionEvent event) {
        menuOverlay.setVisible(true);
        menuOverlay.setManaged(true);
    }

    @FXML
    protected void onMenuReturn(ActionEvent event) {
        menuOverlay.setVisible(false);
        menuOverlay.setManaged(false);
    }

    @FXML
    protected void onMenuLeave(ActionEvent event) {
        if (stage != null) {
            ScreenNavigation.show(stage, "StartScreen.fxml");
        }
    }

    @FXML
    protected void onDebug(ActionEvent event) {
        // TODO(controller): 调试面板或日志窗口
    }

    @FXML
    protected void onUiMinus(ActionEvent event) {
        int i = scaleIndex.get();
        if (i > 0) {
            scaleIndex.set(i - 1);
            syncScaleLabel();
        }
    }

    @FXML
    protected void onUiPlus(ActionEvent event) {
        int i = scaleIndex.get();
        if (i < UI_SCALE_STEPS.length - 1) {
            scaleIndex.set(i + 1);
            syncScaleLabel();
        }
    }

    @FXML
    protected void onPrimaryAction(ActionEvent event) {
        // TODO(controller+logic): 将用户意图交给 AppContext.get().gameEngine().getGameLogic()
    }

    @FXML
    protected void onUndo(ActionEvent event) {
        // TODO(controller+logic): 若逻辑层支持撤销出牌，在此调用
    }

    private void syncScaleLabel() {
        double s = UI_SCALE_STEPS[scaleIndex.get()];
        uiScaleLabel.setText(String.format("%.2fx", s));
        // TODO(view): 将 s 应用到 gameRoot.scaleX/Y 或 CSS 变量
    }
}
