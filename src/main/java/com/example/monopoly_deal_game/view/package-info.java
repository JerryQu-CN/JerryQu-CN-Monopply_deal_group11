/**
 * <h2>View Layer (View / the V in MVC)</h2>
 * <p>
 * Responsibility: Related to "how to draw" -- custom JavaFX controls, observer interfaces,
 * future splits into {@code PlayerView}, {@code BankView}, {@code PropertyView}, etc.
 * In the current course project, much of the layout is still in {@code resources/.../*.fxml};
 * this package first holds interfaces and reusable controls to avoid controller bloat.
 * </p>
 *
 * <h3>Features to implement (Requirement 17)</h3>
 * <ul>
 *   <li>Card Tooltip: Hover/click to show name, function, value, color, rent tier.</li>
 *   <li>Three-zone layout: Opponent thumbnails + central hand + own bank/property details.</li>
 *   <li>Each player's hand <strong>count</strong> display (not peeking at opponents' specific cards).</li>
 * </ul>
 *
 * <h3>Relationships with other packages</h3>
 * <ul>
 *   <li><b>← model / logic</b>: Bind a read-only view model through {@link com.example.monopoly_deal_game.view.GameObserver}
 *   or JavaFX {@code Property}.</li>
 *   <li><b>→ controller</b>: {@code fx:controller} in FXML is still in {@link com.example.monopoly_deal_game.controller}.</li>
 * </ul>
 *
 * <h3>TODO</h3>
 * <ul>
 *   <li>Implement concrete subscribers for {@link com.example.monopoly_deal_game.view.GameObserver} (or switch to JavaFX Bean).</li>
 *   <li>Split {@code GameplayScreen} child control classes into this package.</li>
 * </ul>
 */
package com.example.monopoly_deal_game.view;
