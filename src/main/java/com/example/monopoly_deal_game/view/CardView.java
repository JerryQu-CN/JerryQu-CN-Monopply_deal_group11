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
 * Card UI node: displays card image, hover zoom, click to select; images from
 * {@code /com/example/monopoly_deal_game/images/cards/}.
 */
public class CardView extends StackPane {

    private static final String CARD_IMAGE_ROOT = "/com/example/monopoly_deal_game/images/cards/";
    private static final double CARD_W = 110;
    private static final double CARD_H = 160;
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private boolean selected = false;
    private boolean strongSelection = false;
    /** When false, no blue glow border is drawn (hand cards are only logically selected, no visual emphasis). */
    private boolean selectionOutlineEnabled = true;
    private final ImageView cardImage;
    /** When non-null (in-game hand cards): click only triggers callback, selection styling is refreshed externally. */
    private Runnable handInteraction;
    /** False when in table read-only display mode, to prevent hover zoom from disrupting layout. */
    private boolean hoverZoomEnabled = true;
    private double baseScaleX = 1.0;
    private double baseScaleY = 1.0;
    private boolean readOnly = false;

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
                System.err.println("Image not found: " + imageName);
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

    /** In-game hand cards: called on click; if not set, retains the standalone demo toggle-selection behavior. */
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

    /** Externally sets whether this is the currently selected card to be played. */
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

    /** Table read-only mode: disables hover zoom, click toggle, hand cursor; completely unresponsive to mouse. */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (readOnly) {
            setCursor(Cursor.DEFAULT);
            setOnMouseEntered(null);
            setOnMouseExited(null);
            setOnMouseClicked(null);
            setFocusTraversable(false);
            setMouseTransparent(true);
        }
    }

    private static final DropShadow SELECT_GLOW = Styles.SELECT_GLOW;
    private static final DropShadow STRONG_SELECT_GLOW = Styles.STRONG_SELECT_GLOW;

    private void applySelectionStyle() {
        if (!selectionOutlineEnabled) {
            setEffect(null);
            setStyle(Styles.UNSELECT_CSS);
            return;
        }
        if (!selected && !strongSelection) {
            setEffect(null);
            setStyle(Styles.UNSELECT_CSS);
            return;
        }
        if (strongSelection) {
            setStyle(Styles.STRONG_SELECT_CSS);
            setEffect(STRONG_SELECT_GLOW);
            return;
        }
        setStyle(Styles.SELECT_CSS);
        setEffect(selected ? SELECT_GLOW : null);
    }
}
