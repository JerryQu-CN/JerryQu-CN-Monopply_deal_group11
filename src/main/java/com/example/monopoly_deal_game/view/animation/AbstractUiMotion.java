package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.util.Duration;

/**
 * 模板：子类实现 {@link #buildAnimation(MotionContext)}，基类负责 {@link #play} 与结束回调。
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
        anim.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        anim.play();
    }
}
