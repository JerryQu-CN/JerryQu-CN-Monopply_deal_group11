package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Place: a "drop+align" animation for cards landing on target {@link javafx.scene.layout.Pane} areas such as the bank strip, property area, discard pile, etc.
 */
public class PlaceMotion extends AbstractUiMotion {

    public PlaceMotion(Duration duration) {
        super(duration);
    }

    public PlaceMotion() {
        this(Duration.millis(280));
    }

    @Override
    protected Animation buildAnimation(MotionContext ctx) {
        // TODO(view): Target Pane should be passed in by the caller via a closure or MotionContext extension field
        return new PauseTransition(duration());
    }
}
