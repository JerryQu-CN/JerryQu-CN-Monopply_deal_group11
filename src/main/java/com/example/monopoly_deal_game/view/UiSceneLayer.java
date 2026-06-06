package com.example.monopoly_deal_game.view;

/**
 * Logical layers within the gameplay interface (not the entire window of a JavaFX {@link javafx.scene.Scene}).
 */
public enum UiSceneLayer {

    /** Main table: opponents, own bank/property strip, hand, sidebar deck indicator. */
    TABLE,

    /** Interaction overlay: card selection, player selection, confirm/cancel. */
    ACTION_MODAL,

    /** System menu: leave game, settings, etc. */
    MENU_OVERLAY
}
