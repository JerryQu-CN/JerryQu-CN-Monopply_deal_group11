/**
 * <h2>Application Entry Layer (App)</h2>
 * <p>
 * Responsibility: Launch JavaFX, create the main {@link javafx.stage.Stage}, load the first screen FXML.
 * </p>
 *
 * <h3>Relationships with other packages</h3>
 * <ul>
 *   <li>Only depends on {@code ScreenNavigation} in {@link com.example.monopoly_deal_game.controller}
 *   for first screen loading; does not write game rules.</li>
 *   <li>Global exception handling, icon, window size strategy can be expanded in this package.</li>
 * </ul>
 *
 * <h3>TODO (where to write)</h3>
 * <ul>
 *   <li>{@code MonopolyDealApplication}: Launch parameters, default resolution, exception fallback.</li>
 *   <li>{@code Launcher}: {@code main} entry; just needs to be consistent with the JavaFX run configuration for course project submission.</li>
 * </ul>
 */
package com.example.monopoly_deal_game.app;
