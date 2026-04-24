package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * 摸牌：从 {@link MotionContext#deckPane()} 飞向 {@link MotionContext#handPane()}（或先揭背面再翻转）。
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
        // TODO(view): PathTransition / TranslateTransition + 可选父容器变更
        return new PauseTransition(duration());
    }
}
