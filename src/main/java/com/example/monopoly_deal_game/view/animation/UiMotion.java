package com.example.monopoly_deal_game.view.animation;

/**
 * A playable UI motion effect; decoupled from specific JavaFX {@link javafx.animation.Animation} subclasses for easy substitution with no-ops in unit tests.
 */
@FunctionalInterface
public interface UiMotion {

    /**
     * @param ctx        Deck/hand and other areas
     * @param onFinished Called on the JavaFX application thread after the animation completes normally; may be {@code null}
     */
    void play(MotionContext ctx, Runnable onFinished);
}
