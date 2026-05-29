/**
 * <h2>GUI "Scene" partitioning (one FXML file per screen)</h2>
 * <p>
 * Lobby, add player, load save, etc. are already independent FXML + {@link com.example.monopoly_deal_game.controller};
 * this package only describes <b>in-game</b> layers and responsibilities,
 * making it easy to attach animations and popups to the correct {@link javafx.scene.layout.Pane}.
 * </p>
 *
 * <h3>Recommended layering (bottom to top)</h3>
 * <ol>
 *   <li>{@link com.example.monopoly_deal_game.view.scene.UiSceneLayer#TABLE} — tabletop, opponent areas, hand bar (main interaction)</li>
 *   <li>{@link com.example.monopoly_deal_game.view.scene.UiSceneLayer#ACTION_MODAL} — target selection, multi-card selection ({@code actionLayer})</li>
 *   <li>{@link com.example.monopoly_deal_game.view.scene.UiSceneLayer#MENU_OVERLAY} — pause menu (semi-transparent overlay)</li>
 * </ol>
 *
 * <p>
 * {@link com.example.monopoly_deal_game.view.scene.ScenePaneResolver} resolves the enum to
 * an existing {@link javafx.scene.layout.Pane} in
 * {@link com.example.monopoly_deal_game.view.GameplayUiBundle},
 * avoiding hardcoding {@code fx:id} strings in animation classes.
 * </p>
 */
package com.example.monopoly_deal_game.view.scene;
