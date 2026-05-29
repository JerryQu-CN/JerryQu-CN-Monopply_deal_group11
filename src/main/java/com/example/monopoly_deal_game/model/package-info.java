/**
 * <h2>Model Layer (Model / the M in MVC)</h2>
 * <p>
 * Responsibility: Describe players, property collections, and other <strong>pure data and domain objects</strong>,
 * without JavaFX, without Socket.
 * The card family is under {@link com.example.monopoly_deal_game.model.cards}; a game session is under {@link com.example.monopoly_deal_game.game.model}.
 * </p>
 *
 * <h3>Features to implement (mapped to requirements)</h3>
 * <ul>
 *   <li><b>Initial deal (Requirement 1.4-1.5)</b>: Each player starts with 5 cards; remaining cards form the draw pile;
 *   shuffle the discard pile to replenish (data structures in {@link com.example.monopoly_deal_game.game.model.GameSession}
 *   or references held by {@code CardManager}).</li>
 *   <li><b>Property and bank display data (Requirement 4-5, 17)</b>: {@link com.example.monopoly_deal_game.model.Player}
 *   contains hand, bank area, property area; supports "dual-color" and "wild property" current color fields.</li>
 *   <li><b>Win/Loss state (Requirement 16)</b>: {@link com.example.monopoly_deal_game.game.model.GameState}
 *   records current player, turn, whether in progress; full set statistics are in the session object.</li>
 * </ul>
 *
 * <h3>Relationships with other packages</h3>
 * <ul>
 *   <li><b>← logic</b>: {@link com.example.monopoly_deal_game.logic.GameLogic}, {@link com.example.monopoly_deal_game.logic.CardManager}
 *   read and write this session data.</li>
 *   <li><b>← controller</b>: Read-only view binding (expose read-only API or DTO through the engine, avoiding direct UI model mutation).</li>
 *   <li><b>← persistence</b>: Serialize/deserialize {@link com.example.monopoly_deal_game.game.model.GameSession}.</li>
 *   <li><b>← network</b>: The state snapshot broadcast by the host should be restorable to {@link com.example.monopoly_deal_game.game.model.GameSession}.</li>
 * </ul>
 *
 * <h3>TODO (where to write)</h3>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.model.cards.Card} hierarchy: Property/Rent/Action card fields and
 *   {@code use} default implementation strategy (specific effects executed in the logic layer).</li>
 *   <li>{@link com.example.monopoly_deal_game.model.Player}: Hand limit, bank denomination list, properties grouped by color.</li>
 *   <li>{@link com.example.monopoly_deal_game.game.model.GameSession}: Player list 2-5, draw/discard pile references, current turn index.</li>
 * </ul>
 */
package com.example.monopoly_deal_game.model;
