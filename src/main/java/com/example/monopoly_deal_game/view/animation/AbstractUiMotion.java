package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.util.Duration;

/**
 * Template: subclasses implement {@link #buildAnimation(MotionContext)}, while the base class handles {@link #play} and the completion callback.
 */
public abstract class AbstractUiMotion implements UiMotion {

    private final Duration duration;

    protected AbstractUiMotion(Duration duration) {
        this.duration = duration;
    }

    protected Duration duration() {
        return duration;
    }

    protected abstract Animation buildAnimation(MotionContext ctx);

    @Override
    public final void play(MotionContext ctx, Runnable onFinished) {
        Animation anim = buildAnimation(ctx);
        javafx.event.EventHandler<javafx.event.ActionEvent> existing = anim.getOnFinished();
        anim.setOnFinished(e -> {
            if (existing != null) {
                existing.handle(e);
            }
            if (onFinished != null) {
                onFinished.run();
            }
        });
        anim.play();
    }
}
