package com.example.monopoly_deal_game.view.animation;

/**
 * 可播放的一段 UI 动效；与具体 JavaFX {@link javafx.animation.Animation} 子类解耦，便于单元测试里替换为 no-op。
 */
@FunctionalInterface
public interface UiMotion {

    /**
     * @param ctx        牌堆/手牌等区域
     * @param onFinished 在 JavaFX 应用线程上、动画正常结束后调用；可为 {@code null}
     */
    void play(MotionContext ctx, Runnable onFinished);
}
