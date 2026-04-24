package com.example.monopoly_deal_game.view.animation;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * 移动：同一桌面上已有 {@link javafx.scene.Node} 的平移、换父（Sly Deal、强制收租等）。
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
        // TODO(view): 计算源/目标在 Scene 坐标系中的 bounds，再 TranslateTransition
        return new PauseTransition(duration());
    }
}
