package com.example.monopoly_deal_game.view.animation;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Card draw animation: a card-back flies from the deck pile to the hand area
 * with a slight arc and fade-in landing.
 */
public class DrawMotion extends AbstractUiMotion {

    private static final double CARD_W = 90;
    private static final double CARD_H = 130;

    private final Pane targetPane;

    /** Single card draw — flies deck → hand. */
    public DrawMotion() {
        this(null);
    }

    /**
     * @param targetPane optional specific target pane (defaults to handPane from context)
     */
    public DrawMotion(Pane targetPane) {
        super(Duration.millis(380));
        this.targetPane = targetPane;
    }

    @Override
    protected Animation buildAnimation(MotionContext ctx) {
        Pane overlay = ctx.actionOverlayPane();
        Pane deck = ctx.deckPane();
        Pane target = targetPane != null ? targetPane : ctx.handPane();

        // Create the flying card node
        StackPane card = buildCardBack();
        overlay.getChildren().add(card);

        // Convert deck and target positions to overlay coordinate space
        Bounds deckBounds = deck.localToScene(deck.getBoundsInLocal());
        Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
        Point2D deckPos = overlay.sceneToLocal(deckBounds.getMinX(), deckBounds.getMinY());
        Point2D targetPos = overlay.sceneToLocal(
                targetBounds.getMinX() + targetBounds.getWidth() / 2 - CARD_W / 2,
                targetBounds.getMinY() + targetBounds.getHeight() / 2 - CARD_H / 2);

        // Start at deck position
        card.setLayoutX(deckPos.getX() + deckBounds.getWidth() / 2 - CARD_W / 2);
        card.setLayoutY(deckPos.getY() + deckBounds.getHeight() / 2 - CARD_H / 2);

        // Random initial rotation and position for natural feel
        double startRotate = (Math.random() - 0.5) * 10;
        card.setRotate(startRotate);

        // Slight random offset on landing so overlapping cards spread naturally
        double landX = targetPos.getX() + (Math.random() - 0.5) * 80;
        double landY = targetPos.getY() + (Math.random() - 0.5) * 30;

        // Movement
        TranslateTransition move = new TranslateTransition(duration(), card);
        move.setToX(landX - card.getLayoutX());
        move.setToY(landY - card.getLayoutY());
        move.setInterpolator(Interpolator.EASE_IN);

        // Rotation — straighten out during flight
        RotateTransition rotate = new RotateTransition(duration(), card);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.EASE_OUT);

        // Scale — slight shrink then grow on arrival
        ScaleTransition scale = new ScaleTransition(duration(), card);
        scale.setFromX(0.6);
        scale.setFromY(0.6);
        scale.setToX(0.85);
        scale.setToY(0.85);
        scale.setInterpolator(Interpolator.EASE_OUT);

        // Fade in
        FadeTransition fade = new FadeTransition(duration(), card);
        fade.setFromValue(0.4);
        fade.setToValue(1.0);

        ParallelTransition flight = new ParallelTransition(move, rotate, scale, fade);

        // Landing bounce
        ScaleTransition bounce = new ScaleTransition(Duration.millis(120), card);
        bounce.setFromX(0.85);
        bounce.setFromY(0.85);
        bounce.setToX(1.0);
        bounce.setToY(1.0);
        bounce.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition seq = new SequentialTransition(flight, bounce);

        seq.setOnFinished(e -> overlay.getChildren().remove(card));
        return seq;
    }

    private static StackPane buildCardBack() {
        StackPane card = new StackPane();
        card.setPrefSize(CARD_W, CARD_H);
        card.setMaxSize(CARD_W, CARD_H);

        Rectangle bg = new Rectangle(CARD_W, CARD_H);
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        bg.setFill(Color.rgb(20, 15, 25));
        bg.setStroke(Color.rgb(201, 162, 43, 0.7));
        bg.setStrokeWidth(2);

        Rectangle inner = new Rectangle(CARD_W - 10, CARD_H - 10);
        inner.setArcWidth(5);
        inner.setArcHeight(5);
        inner.setFill(Color.rgb(30, 25, 40));
        inner.setStroke(Color.rgb(201, 162, 43, 0.35));
        inner.setStrokeWidth(1);

        card.getChildren().addAll(bg, inner);
        card.setOpacity(0.9);
        return card;
    }
}
