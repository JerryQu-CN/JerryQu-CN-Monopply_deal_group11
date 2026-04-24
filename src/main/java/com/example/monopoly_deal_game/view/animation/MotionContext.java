package com.example.monopoly_deal_game.view.animation;

import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * 动效所需的舞台引用与默认参数；由 {@link com.example.monopoly_deal_game.view.GameplayUiBundle} 或
 * 对局 Controller 在构造时填入，避免每个 Motion 构造函数一长串 Pane。
 */
public record MotionContext(
        Pane deckPane,
        Pane handPane,
        Pane discardPane,
        Pane actionOverlayPane,
        Duration defaultDuration) {

    public MotionContext {
        Objects.requireNonNull(deckPane);
        Objects.requireNonNull(handPane);
        Objects.requireNonNull(discardPane);
        Objects.requireNonNull(actionOverlayPane);
        Objects.requireNonNull(defaultDuration);
    }

    public static MotionContext forTable(
            Pane deckPane, Pane handPane, Pane discardPane, Pane actionOverlayPane) {
        return new MotionContext(deckPane, handPane, discardPane, actionOverlayPane, Duration.millis(320));
    }
}
