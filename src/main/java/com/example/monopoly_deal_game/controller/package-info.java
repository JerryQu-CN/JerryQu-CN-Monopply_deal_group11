/**
 * <h2>Controller Layer (Controller / the C in MVC)</h2>
 * <p>
 * Responsibility: Handle FXML button/input events, call the API exposed by
 * {@link com.example.monopoly_deal_game.game.engine.GameEngine} or {@link com.example.monopoly_deal_game.logic.GameLogic};
 * <strong>do not write specific settlement rules here</strong>.
 * In the design diagram, {@code LobbyController} and {@code GameController} correspond to the various
 * {@code *ScreenController} classes in this package:
 * The lobby flow is handled jointly by {@code StartScreen}, {@code AddPlayerScreen}, etc.,
 * and the game session is handled by {@code GameplayScreen} (to be bound).
 * </p>
 *
 * <h3>Features to implement (mapped to the requirements document)</h3>
 * <ul>
 *   <li><b>Initialization (Requirement 1)</b>: Seats 2-5, number of human/bot players, play order selection;
 *   pass options to {@code GameLogic.initGame(...)}.</li>
 *   <li><b>Turn and hand (Requirement 2-3)</b>: Draw cards at turn start, play 0-3 cards, end turn,
 *   >7 cards discard flow -- all done by calling the logic layer.</li>
 *   <li><b>Pay rent and Just Say No (Requirement 7, 14)</b>: Pop up a selection interface, collect user choices,
 *   then construct an "intent object" for the logic layer.</li>
 *   <li><b>UI interaction (Requirement 17)</b>: Hover/click to view card descriptions;
 *   display each player's hand count, bank and property thumbnails.</li>
 * </ul>
 *
 * <h3>Relationships with other packages</h3>
 * <ul>
 *   <li><b>→ logic</b>: The only place allowed to modify game state (through engine/API).</li>
 *   <li><b>→ network</b>: In online mode, synchronize with {@link com.example.monopoly_deal_game.network.NetworkClient}
 *   in {@code SyncController} (to be created).</li>
 *   <li><b>→ view</b>: Complex controls can be split into custom controls;
 *   subscribe to model refreshes through {@link com.example.monopoly_deal_game.view.GameObserver} (to be connected).</li>
 *   <li><b>→ persistence</b>: The load game screen calls {@link com.example.monopoly_deal_game.persistence.SaveGameService}.</li>
 * </ul>
 *
 * <h3>TODO (where to write)</h3>
 * <ul>
 *   <li>Each {@code *ScreenController}: Lobby/load-save flow integration with the logic layer.</li>
 *   <li>New {@code GameplayController} (suggested): Bind {@code GameplayScreen.fxml}, connect to {@code GameEngine}.</li>
 *   <li>New {@code SyncController} (suggested): Align online state with local UI.</li>
 * </ul>
 */
package com.example.monopoly_deal_game.controller;
