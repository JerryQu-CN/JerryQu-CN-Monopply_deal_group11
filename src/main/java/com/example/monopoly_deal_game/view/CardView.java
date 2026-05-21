package com.example.monopoly_deal_game.view;

import java.net.URL;

import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 卡牌 UI 节点：显示卡图、悬停放大、点击选中；图片来自
 * {@code /com/example/monopoly_deal_game/images/cards/}。
 */
public class CardView extends StackPane {

    private static final String CARD_IMAGE_ROOT = "/com/example/monopoly_deal_game/images/cards/";
    private static final double CARD_W = 110;
    private static final double CARD_H = 160;
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private boolean selected = false;
    private boolean strongSelection = false;
    /** 为 false 时不绘制蓝框发光（对局手牌仅逻辑选中、不强调视觉）。 */
    private boolean selectionOutlineEnabled = true;
    private final ImageView cardImage;
    /** 非 null 时（对局手牌）：点击只回调，由外部刷新选中样式。 */
    private Runnable handInteraction;
    /** 牌桌只读展示时为 false，避免悬停缩放干扰排版。 */
    private boolean hoverZoomEnabled = true;
    private double baseScaleX = 1.0;
    private double baseScaleY = 1.0;

    public CardView(String imageName, String cardName, String cardDesc) {
        setPrefSize(CARD_W, CARD_H);
        setMaxSize(CARD_W, CARD_H);
        setCursor(Cursor.HAND);

        cardImage = new ImageView();
        cardImage.setFitWidth(CARD_W);
        cardImage.setFitHeight(CARD_H);
        cardImage.setPreserveRatio(true);

        if (imageName != null && !imageName.isBlank()) {
            URL url = CardView.class.getResource(CARD_IMAGE_ROOT + imageName);
            if (url != null) {
                cardImage.setImage(
                        IMAGE_CACHE.computeIfAbsent(
                                url.toExternalForm(), Image::new));
            } else {
                System.err.println("图片未找到：" + imageName);
            }
        }

        getChildren().add(cardImage);

        setOnMouseEntered(e -> {
            if (!hoverZoomEnabled) {
                return;
            }
            baseScaleX = getScaleX();
            baseScaleY = getScaleY();
            setScaleX(baseScaleX * 1.2);
            setScaleY(baseScaleY * 1.2);
            setTranslateY(-15);
        });

        setOnMouseExited(e -> {
            if (!hoverZoomEnabled) {
                return;
            }
            setScaleX(baseScaleX);
            setScaleY(baseScaleY);
            setTranslateY(0);
        });

        setOnMouseClicked(e -> {
            if (handInteraction != null) {
                handInteraction.run();
                e.consume();
                return;
            }
            selected = !selected;
            applySelectionStyle();
        });

        if (cardName != null || cardDesc != null) {
            setCardInfo(
                    cardName != null ? cardName : "",
                    cardDesc != null ? cardDesc : "");
        }
    }

    public void setCardInfo(String name, String desc) {
        Tooltip tip = new Tooltip(name + "\n" + desc);
        tip.setShowDelay(Duration.millis(100));
        Tooltip.install(this, tip);
    }

    /** 对局手牌：点击时调用，不传则保留独立演示用的切换选中行为。 */
    public void setHandInteraction(Runnable onClick) {
        this.handInteraction = onClick;
    }

    public void setSelectionOutlineEnabled(boolean enabled) {
        this.selectionOutlineEnabled = enabled;
        if (!enabled) {
            setEffect(null);
            setStyle("-fx-border-color:transparent;-fx-border-width:0");
        } else {
            applySelectionStyle();
        }
    }

    /** 由外部设置是否为当前选中、待打出的牌。 */
    public void setHandSelected(boolean sel) {
        this.selected = sel;
        applySelectionStyle();
    }

    public void setStrongHandSelection(boolean strong) {
        this.strongSelection = strong;
        applySelectionStyle();
    }

    public void setHoverZoomEnabled(boolean hoverZoomEnabled) {
        this.hoverZoomEnabled = hoverZoomEnabled;
    }

    private static final DropShadow SELECT_GLOW =
            new DropShadow(14, Color.color(0.15, 0.35, 0.85, 0.55));
    private static final DropShadow STRONG_SELECT_GLOW =
            new DropShadow(22, Color.color(0.95, 0.45, 0.05, 0.82));

    private void applySelectionStyle() {
        if (!selectionOutlineEnabled) {
            setEffect(null);
            setStyle("-fx-border-color:transparent;-fx-border-width:0");
            return;
        }
        if (!selected && !strongSelection) {
            setEffect(null);
            setStyle("-fx-border-color:transparent;-fx-border-width:0");
            return;
        }
        if (strongSelection) {
            setStyle(
                    "-fx-border-color:#ef6c00;-fx-border-width:5;-fx-border-style:solid solid solid solid;"
                            + "-fx-border-radius:11;-fx-background-radius:11;"
                            + "-fx-background-color: rgba(255,243,224,0.45);");
            setEffect(STRONG_SELECT_GLOW);
            return;
        }
        setStyle(
                "-fx-border-color:#1565c0;-fx-border-width:3;-fx-border-radius:10;-fx-background-radius:10");
        setEffect(selected ? SELECT_GLOW : null);
    }
}
