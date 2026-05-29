package com.example.monopoly_deal_game.view;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Functional area references of the main game screen defined in FXML (does not include specific card drawing logic).
 * <p>
 * UI classes typically need the following:
 * </p>
 * <ul>
 *   <li><b>Root and top bar</b>: {@link #gameRoot()}, {@link #topbar()} -- full-screen overlay layer, menu entry.</li>
 *   <li><b>Left sidebar HUD</b>: {@link #leftSidebar()} -- deck display, step count, main action buttons, etc.</li>
 *   <li><b>Table zones</b>: {@link #deckPane()}, {@link #discardPane()}, {@link #voidPane()},
 *   {@link #opponentsPane()}, {@link #selfBoardPane()}, {@link #handPane()} -- custom Node or Canvas will be mounted here later.</li>
 *   <li><b>Interaction layer</b>: {@link #actionLayer()} -- modal interactions such as card selection, target selection.</li>
 *   <li><b>Menu overlay</b>: {@link #menuOverlay()} -- pause/leave game.</li>
 *   <li><b>Optional</b>: {@link #chatPane()} -- only exists in the full game screen; may be {@code null} in first-run/simplified layouts.</li>
 * </ul>
 * <p>
 * Constructed from the loaded FXML by {@link com.example.monopoly_deal_game.controller.AbstractGameplayScreenController#toUiBundle()},
 * making it easy to pass "layout references" to plain Java view helper classes, preventing the controller from further bloat.
 * </p>
 */
public record GameplayUiBundle(
        AnchorPane gameRoot,
        HBox topbar,
        VBox leftSidebar,
        Pane deckPane,
        Pane discardPane,
        Pane voidPane,
        Pane opponentsPane,
        Pane selfBoardPane,
        Pane handPane,
        Pane actionLayer,
        StackPane menuOverlay,
        Pane chatPane) {

    public GameplayUiBundle {
        Objects.requireNonNull(gameRoot);
        Objects.requireNonNull(topbar);
        Objects.requireNonNull(leftSidebar);
        Objects.requireNonNull(deckPane);
        Objects.requireNonNull(discardPane);
        Objects.requireNonNull(voidPane);
        Objects.requireNonNull(opponentsPane);
        Objects.requireNonNull(selfBoardPane);
        Objects.requireNonNull(handPane);
        Objects.requireNonNull(actionLayer);
        Objects.requireNonNull(menuOverlay);
    }
}
