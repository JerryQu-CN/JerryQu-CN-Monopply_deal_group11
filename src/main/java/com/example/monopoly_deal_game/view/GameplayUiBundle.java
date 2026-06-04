package com.example.monopoly_deal_game.view;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Bundles FXML-defined layout pane references for the main game screen,
 * providing typed access to deck, hand, table, sidebar, and overlay zones.
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
