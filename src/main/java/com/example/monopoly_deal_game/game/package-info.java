/**
 * <h2>Game Runtime Structure (aligned with course project layering: Facade / Constants / Session Aggregate)</h2>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.game.engine.GameEngine} — Facade and game entry point.</li>
 *   <li>{@link com.example.monopoly_deal_game.game.rules.GameConfig} — Player count limits and rule constants.</li>
 *   <li>{@link com.example.monopoly_deal_game.game.model.GameSession}, {@link com.example.monopoly_deal_game.game.model.GameState} — Game session aggregate root and runtime state.</li>
 * </ul>
 */
package com.example.monopoly_deal_game.game;
