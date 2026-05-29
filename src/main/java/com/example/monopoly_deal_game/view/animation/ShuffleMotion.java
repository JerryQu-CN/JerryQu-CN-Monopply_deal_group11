package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Shuffle animation: visual feedback only (deck jitter, minor stacking order adjustments); the real card order is governed by logic.
 */
public class ShuffleMotion extends AbstractUiMotion {

    public ShuffleMotion(Duration duration) {
        super(duration);
    }

    public ShuffleMotion() {
        this(Duration.millis(400));
    }

    @Override
    protected Animation buildAnimation(MotionContext ctx) {
        // TODO(view): Apply a short Timeline or RotateTransition to child nodes within deckPane
        return new PauseTransition(duration());
    }
}
