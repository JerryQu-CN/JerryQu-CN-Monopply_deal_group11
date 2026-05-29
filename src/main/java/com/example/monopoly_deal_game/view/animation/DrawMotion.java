package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Draw card: flies from {@link MotionContext#deckPane()} to {@link MotionContext#handPane()} (optionally reveals the back first, then flips).
 */
public class DrawMotion extends AbstractUiMotion {

    public DrawMotion(Duration duration) {
        super(duration);
    }

    public DrawMotion() {
        this(Duration.millis(350));
    }

    @Override
    protected Animation buildAnimation(MotionContext ctx) {
        // TODO(view): PathTransition / TranslateTransition + optional parent container change
        return new PauseTransition(duration());
    }
}
