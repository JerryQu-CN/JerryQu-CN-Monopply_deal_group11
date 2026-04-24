package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * 放置：牌落到银行条、物业区、弃牌堆等目标 {@link javafx.scene.layout.Pane} 的「落下+对齐」动画。
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
        // TODO(view): 目标 Pane 由调用方通过闭包或 MotionContext 扩展字段传入
        return new PauseTransition(duration());
    }
}
