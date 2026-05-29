package com.example.monopoly_deal_game.view.animation;

import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Stage references and default parameters needed for motion effects; filled in at construction time by {@link com.example.monopoly_deal_game.view.GameplayUiBundle} or
 * the game Controller, avoiding a long chain of Pane arguments in each Motion constructor.
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
