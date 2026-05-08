package com.example.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GameMainView {

    private HBox handArea;

    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #4ECDC4;");

        // ========== Left:Information Area ==========
        VBox leftPlayerInfo = new VBox(20);
        leftPlayerInfo.setPrefWidth(160);
        leftPlayerInfo.setStyle("-fx-padding:15; -fx-background-color: #F7EEDD;");
        leftPlayerInfo.getChildren().addAll(
                new Label("【Information Area】")
        );
        root.setLeft(leftPlayerInfo);

        // ========== Right:historical playing record ==========
        VBox rightHistory = new VBox(10);
        rightHistory.setPrefWidth(180);
        rightHistory.setStyle("-fx-padding:15; -fx-background-color: #F0E6D2;");
        rightHistory.getChildren().add(new Label("【历史出牌记录】"));
        ScrollPane historyScroll = new ScrollPane(rightHistory);
        historyScroll.setFitToWidth(true);
        root.setRight(historyScroll);

        // ========== middle ==========
        VBox centerArea = new VBox(12);
        centerArea.setPadding(new Insets(10));

        // 1. 对手物业区
        centerArea.getChildren().add(createArea("对手物业区"));

        // 2. 手牌区（空容器，UI做好，等队友加卡牌）
        handArea = new HBox(15);
        handArea.setPrefHeight(300);
        handArea.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-padding:20;");
        handArea.setAlignment(javafx.geometry.Pos.CENTER);

        // ========== 测试用示例卡牌（看效果用，之后可删除） ==========
        // handArea.getChildren().addAll(
        //         new CardView("blueCard.png", "蓝色地产", "价值2M，3张成套"),
        //         new CardView("pinkCard.png", "粉色地产", "价值1M，2张成套"),
        //         new CardView("passGo.png", "Pass Go", "抽2张牌"),
        //         new CardView("justSayNo.png", "Just Say No", "抵消一次效果")
        // );

        ScrollPane handScroll = new ScrollPane(handArea);
        handScroll.setFitToHeight(true);
        handScroll.setStyle("-fx-background:transparent;");
        centerArea.getChildren().add(handScroll);

        // 3. 我的物业区
        centerArea.getChildren().add(createArea("我的物业区"));

        // 4. 银行区
        StackPane bankArea = createArea("银行区");
        bankArea.setPrefHeight(120);
        centerArea.getChildren().add(bankArea);

        root.setCenter(centerArea);

        // 全屏
        Scene scene = new Scene(root, 1300, 850);
        stage.setTitle("大富翁Deal - UI框架");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    // 给队友调用：添加一张卡牌到手牌区
    public void addCard(CardView card) {
        handArea.getChildren().add(card);
    }

    // 创建区域框
    private StackPane createArea(String title) {
        StackPane pane = new StackPane(new Label(title));
        pane.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-padding:10;");
        pane.setPrefHeight(130);
        return pane;
    }

}