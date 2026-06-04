package com.example.monopoly_deal_game.view.animation;

import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Pane references and default animation duration for UiMotion effects,
 * avoiding repeated Pane arguments in each motion constructor.
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
