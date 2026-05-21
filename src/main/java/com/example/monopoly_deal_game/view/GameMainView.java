package com.example.monopoly_deal_game.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 对局主界面布局骨架（信息区、历史、手牌、物业区、银行区）。
 * 在 {@link Stage} 上调用 {@link #start(Stage)} 展示；向手牌区添加卡牌使用 {@link #addCard(CardView)}。
 */
public class GameMainView {

    private HBox handArea;

    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #4ECDC4;");

        VBox leftPlayerInfo = new VBox(20);
        leftPlayerInfo.setPrefWidth(160);
        leftPlayerInfo.setStyle("-fx-padding:15; -fx-background-color: #F7EEDD;");
        leftPlayerInfo.getChildren().addAll(new Label("【Information Area】"));
        root.setLeft(leftPlayerInfo);

        VBox rightHistory = new VBox(10);
        rightHistory.setPrefWidth(180);
        rightHistory.setStyle("-fx-padding:15; -fx-background-color: #F0E6D2;");
        rightHistory.getChildren().add(new Label("【历史出牌记录】"));
        ScrollPane historyScroll = new ScrollPane(rightHistory);
        historyScroll.setFitToWidth(true);
        root.setRight(historyScroll);

        VBox centerArea = new VBox(12);
        centerArea.setPadding(new Insets(10));

        centerArea.getChildren().add(createArea("对手物业区"));

        handArea = new HBox(15);
        handArea.setPrefHeight(300);
        handArea.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-padding:20;");
        handArea.setAlignment(javafx.geometry.Pos.CENTER);

        // 示例卡牌（验证资源路径；接入逻辑后可删）
        var v1 = new CardView("blueCard.png", "蓝色地产", "价值2M，3张成套");
        var v2 = new CardView("pinkCard.png", "粉色地产", "价值1M，2张成套");
        var v3 = new CardView("passGo.png", "Pass Go", "抽2张牌");
        var v4 = new CardView("justSayNo.png", "Just Say No", "抵消一次效果");
        handArea.getChildren().addAll(v1, v2, v3, v4);

        ScrollPane handScroll = new ScrollPane(handArea);
        handScroll.setFitToHeight(true);
        handScroll.setStyle("-fx-background:transparent;");
        centerArea.getChildren().add(handScroll);

        centerArea.getChildren().add(createArea("我的物业区"));

        StackPane bankArea = createArea("银行区");
        bankArea.setPrefHeight(120);
        centerArea.getChildren().add(bankArea);

        root.setCenter(centerArea);

        Scene scene = new Scene(root, 1300, 850);
        stage.setTitle("大富翁Deal - UI框架");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    public void addCard(CardView card) {
        handArea.getChildren().add(card);
    }

    private StackPane createArea(String title) {
        StackPane pane = new StackPane(new Label(title));
        pane.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-padding:10;");
        pane.setPrefHeight(130);
        return pane;
    }
}
