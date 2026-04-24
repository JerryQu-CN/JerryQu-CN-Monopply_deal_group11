package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * 洗牌动画：仅视觉反馈（牌堆抖动、叠放顺序微调）；真实牌序以 logic 为准。
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
        // TODO(view): 对 deckPane 内子节点做短 Timeline 或 RotateTransition
        return new PauseTransition(duration());
    }
}
