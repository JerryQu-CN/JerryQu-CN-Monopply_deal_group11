package com.example.monopoly_deal_game.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * 可折叠卡牌堆：收起时卡片层叠显示（最多展示若干张 + 剩余计数），点击后在父容器中弹出完整网格。
 */
public class ExpandableCardStack extends VBox {

    private static final int MAX_VISIBLE = 5;
    private static final double OVERLAP = 22;

    private final Pane overlayParent;
    private final List<Node> cards = new ArrayList<>();
    private final HBox collapsedRow = new HBox();
    private StackPane expandedOverlay;

    public ExpandableCardStack(Pane overlayParent) {
        this.overlayParent = overlayParent;
        setPickOnBounds(true);
        collapsedRow.setAlignment(Pos.CENTER_LEFT);
        collapsedRow.setCursor(Cursor.HAND);
        collapsedRow.setPickOnBounds(true);
        getChildren().add(collapsedRow);
    }

    public void setCards(List<Node> cardNodes) {
        cards.clear();
        if (cardNodes != null) {
            cards.addAll(cardNodes);
        }
        rebuildCollapsed();
    }

    public int size() {
        return cards.size();
    }

    private void rebuildCollapsed() {
        collapsedRow.getChildren().clear();
        collapsedRow.setOnMouseClicked(null);

        if (cards.isEmpty()) {
            Label empty = new Label("（空）");
            empty.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
            collapsedRow.getChildren().add(empty);
            return;
        }

        int total = cards.size();
        int visible = Math.min(total, MAX_VISIBLE);

        HBox cardsRow = new HBox();
        cardsRow.setAlignment(Pos.CENTER_LEFT);
        cardsRow.setSpacing(-OVERLAP);

        for (int i = 0; i < visible; i++) {
            cardsRow.getChildren().add(cards.get(i));
        }

        collapsedRow.getChildren().add(cardsRow);

        if (total > MAX_VISIBLE) {
            int remaining = total - MAX_VISIBLE;
            Label badge = new Label(" +" + remaining + " ");
            badge.setStyle(
                    "-fx-background-color: #1565c0; -fx-text-fill: white;"
                            + " -fx-font-size: 10px; -fx-font-weight: bold;"
                            + " -fx-background-radius: 10; -fx-padding: 2 7 2 7;");
            badge.setTranslateX(-6);
            collapsedRow.getChildren().add(badge);
        }

        collapsedRow.setOnMouseClicked(e -> {
            if (expandedOverlay != null) {
                collapse();
            } else {
                expand();
            }
        });
    }

    private void expand() {
        collapse(); // 先移除已有

        expandedOverlay = new StackPane();
        expandedOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.52);");
        expandedOverlay.setPickOnBounds(true);

        VBox panel = new VBox(12);
        panel.setMaxWidth(720);
        panel.setMaxHeight(520);
        panel.setStyle(
                "-fx-background-color: #fafafa; -fx-background-radius: 12;"
                        + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 14, 0, 0, 3);");
        panel.setPadding(new Insets(16));

        Label title = new Label("银行财产 · 共 " + cards.size() + " 张");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1b5e20;");

        FlowPane grid = new FlowPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.TOP_LEFT);
        for (Node card : cards) {
            grid.getChildren().add(card);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent;");
        scroll.setPrefViewportHeight(370);

        Label hint = new Label("点击背景或再次点击卡堆关闭");
        hint.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        panel.getChildren().addAll(title, scroll, hint);
        expandedOverlay.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.CENTER);

        expandedOverlay.setOnMouseClicked(ev -> {
            if (ev.getTarget() == expandedOverlay) {
                collapse();
            }
        });

        overlayParent.getChildren().add(expandedOverlay);
        if (overlayParent instanceof AnchorPane) {
            AnchorPane.setTopAnchor(expandedOverlay, 0.0);
            AnchorPane.setBottomAnchor(expandedOverlay, 0.0);
            AnchorPane.setLeftAnchor(expandedOverlay, 0.0);
            AnchorPane.setRightAnchor(expandedOverlay, 0.0);
        }
        expandedOverlay.prefWidthProperty().bind(overlayParent.widthProperty());
        expandedOverlay.prefHeightProperty().bind(overlayParent.heightProperty());
    }

    public void collapse() {
        if (expandedOverlay != null) {
            overlayParent.getChildren().remove(expandedOverlay);
            expandedOverlay = null;
        }
    }
}