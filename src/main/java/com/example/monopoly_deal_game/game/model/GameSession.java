package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.game.rules.GameConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root for a single game session.
 * Holds all game data: players, draw pile, discard pile and game state.
 */
public class GameSession {
    private final List<Player> players;
    private final List<Card> drawPile;
    private final List<Card> discardPile;
    private final GameState gameState;

    public GameSession() {
        this.players = new ArrayList<>();
        this.drawPile = new ArrayList<>();
        this.discardPile = new ArrayList<>();
        this.gameState = new GameState();
    }

    // Player Management
    /** Adds a player to the game (enforces max player limit) */
    public void addPlayer(Player player) {
        if (players.size() >= GameConfig.MAX_PLAYERS) {
            throw new IllegalStateException("Maximum " + GameConfig.MAX_PLAYERS + " players allowed");
        }
        players.add(player);
        player.setOrderIndex(players.size() - 1);
    }

    /** Gets the current active player */
    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(gameState.getCurrentPlayerIndex());
    }

    /** Finds a player by their unique ID */
    public Player getPlayerById(String playerId) {
        for (Player p : players) {
            if (p.getPlayerId().equals(playerId)) return p;
        }
        return null;
    }

    /** Gets the next player in turn order */
    public Player getNextPlayer() {
        int nextIndex = (gameState.getCurrentPlayerIndex() + 1) % players.size();
        return players.get(nextIndex);
    }

    // Pile Management
    /** Initializes and shuffles the draw pile with the full deck */
    public void initDrawPile(List<Card> fullDeck) {
        drawPile.clear();
        drawPile.addAll(fullDeck);
        Collections.shuffle(drawPile);
    }

    /** Single card discard entry point */
    public void discardCard(Card card) {
        if (card != null) discardPile.add(card);
    }

    /** Multiple card discard entry point */
    public void discardCards(List<Card> cards) {
        if (cards != null && !cards.isEmpty()) discardPile.addAll(cards);
    }

    /** Shuffles all discard pile cards back into the draw pile */
    public void shuffleDiscardIntoDrawPile() {
        if (discardPile.isEmpty()) return;
        drawPile.addAll(discardPile);
        discardPile.clear();
        Collections.shuffle(drawPile);
    }

    // Standard Getters
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<Card> getDrawPile() { return drawPile; }
    public List<Card> getDiscardPile() { return discardPile; }
    public GameState getGameState() { return gameState; }
    public int getDrawPileSize() { return drawPile.size(); }
    public int getDiscardPileSize() { return discardPile.size(); }
}
