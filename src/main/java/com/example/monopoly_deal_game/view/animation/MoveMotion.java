package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Move: translation and reparenting of existing {@link javafx.scene.Node} nodes on the same table (Sly Deal, forced rent collection, etc.).
 */
public class MoveMotion extends AbstractUiMotion {

    public MoveMotion(Duration duration) {
        super(duration);
    }

    public MoveMotion() {
        this(Duration.millis(300));
    }

    @Override
    protected Animation buildAnimation(MotionContext ctx) {
        // TODO(view): Compute source/target bounds in Scene coordinate space, then TranslateTransition
        return new PauseTransition(duration());
    }
}
