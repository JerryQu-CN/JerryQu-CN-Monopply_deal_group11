/**
 * <h2>Game Logic Layer</h2>
 * <p>
 * Responsibility: Implement all <strong>rules and flows</strong> in the requirements document -- dealing, drawing,
 * playing cards, ending turns, discarding, action card effects, rent and Just Say No chains, victory determination.
 * Corresponds to the architecture diagram core: {@link com.example.monopoly_deal_game.game.engine.GameEngine} depends on
 * {@link com.example.monopoly_deal_game.logic.GameLogic};
 * {@link com.example.monopoly_deal_game.network.GameServer} on the host side should also call the same logic
 * to ensure authoritative state.
 * </p>
 *
 * <h3>Class-to-requirement mapping (for task division)</h3>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.game.rules.GameConfig} — Constants: player count limits, 5-card starting hand,
 *   turn draw 2/5 cards, hand limit 7, max 3 cards played per turn, etc.</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.GameLogic} — {@code initGame} (Requirement 1),
 *   {@code drawCard} (Requirement 2), {@code playCard} (Requirement 3-6), {@code endTurn} (Requirement 3),
 *   {@code checkGameOver} (Requirement 16).</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.TurnManager} — Current player switching, played card count,
 *   whether Just Say No consumes the quota (Requirement 3.2).</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.CardManager} — Draw pile/discard pile,
 *   shuffle discard when draw pile empty (Requirement 1.5, 2.4).</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.CardFactory} — Generate the full deck (matching the physical card list).</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.CardEffectExecutor} — Rent/DealBreaker/.../JustSayNo chains
 *   (Requirement 8-15).</li>
 *   <li>{@link com.example.monopoly_deal_game.game.engine.GameEngine} — {@code launchGame},
 *   {@code startLocalGame}, {@code startLanGame} entry points, assembles the above objects.</li>
 * </ul>
 *
 * <h3>Relationships with other packages</h3>
 * <ul>
 *   <li><b>→ model</b>: Only modify data within {@link com.example.monopoly_deal_game.game.model.GameSession},
 *   do not directly operate on JavaFX.</li>
 *   <li><b>← controller</b>: Controller only calls use-case methods on {@link com.example.monopoly_deal_game.game.engine.GameEngine}.</li>
 *   <li><b>← network</b>: Client sends {@link com.example.monopoly_deal_game.logic.PlayerCommand},
 *   host executes the same logic.</li>
 *   <li><b>← ai</b>: Bot generates legal {@link com.example.monopoly_deal_game.logic.PlayerCommand} through strategy.</li>
 * </ul>
 *
 * <h3>TODO (overall)</h3>
 * <ul>
 *   <li>See {@code LOGIC_TASKS.md} in this package directory (task checklist, prerequisites, and mapping to each requirement chapter).</li>
 *   <li>First close the "local two-player versus" loop, then integrate online synchronization.</li>
 *   <li>Write unit tests for key rules (especially draw counts, discarding, Just Say No chains).</li>
 * </ul>
 */
package com.example.monopoly_deal_game.logic;
