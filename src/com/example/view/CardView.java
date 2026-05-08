package com.example.view;

import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class CardView extends StackPane {

    private static final double CARD_W = 110;
    private static final double CARD_H = 160;

    private boolean selected = false;
    private final ImageView cardImage;


    public CardView(String imageName,String cardName,String cardDesc) {
        setPrefSize(CARD_W, CARD_H);
        setMaxSize(CARD_W, CARD_H);
        setCursor(Cursor.HAND);

        cardImage = new ImageView();
        cardImage.setFitWidth(CARD_W);
        cardImage.setFitHeight(CARD_H);
        cardImage.setPreserveRatio(true);


        if (imageName != null && !imageName.isBlank()) {
            try {
                String path = "file:src/resources/images/cards/" + imageName;
                cardImage.setImage(new Image(path));
            } catch (Exception e) {
                System.out.println("图片加载失败：" + imageName);
            }
        }

        getChildren().add(cardImage);

        //hover zoom effect
        setOnMouseEntered(e -> {
            setScaleX(1.2);
            setScaleY(1.2);
            setTranslateY(-15);
        });

        setOnMouseExited(e -> {
            setScaleX(1.0);
            setScaleY(1.0);
            setTranslateY(0);
        });

        setOnMouseClicked(e -> {
            selected = !selected;
            setStyle(selected
                    ? "-fx-border-color:red;-fx-border-width:3;-fx-border-radius:8"
                    : "");
        });
    }


    public void setCardInfo(String name, String desc) {
        Tooltip tip = new Tooltip(name + "\n" + desc);
        tip.setShowDelay(Duration.millis(100));
        Tooltip.install(this, tip);
    }
}