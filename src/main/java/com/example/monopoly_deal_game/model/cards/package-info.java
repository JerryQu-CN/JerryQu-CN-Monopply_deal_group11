/**
 * <h2>Card Family (Card Domain Model)</h2>
 * <p>
 * {@link com.example.monopoly_deal_game.model.cards.Card} and its subclasses, {@link CardColor}, {@link CardType}
 * are independent of the "game session" aggregate root.
 * Session and runtime state (aggregate root) are in {@link com.example.monopoly_deal_game.game.model.GameSession}
 * and {@link com.example.monopoly_deal_game.game.model.GameState}.
 * </p>
 */
package com.example.monopoly_deal_game.model.cards;
