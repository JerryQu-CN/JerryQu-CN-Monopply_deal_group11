package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for a game session: player list, draw/discard piles, and {@link GameState}.
 */
public class GameSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Player> players = new ArrayList<>();
    private final GameState gameState = new GameState();
    private final List<Card> drawPile = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();

    public List<Player> getPlayers() {
        return players;
    }

    public GameState getGameState() {
        return gameState;
    }

    /** Draw pile (the top of the stack is the tail of the list; can be customized by convention). */
    public List<Card> getDrawPile() {
        return drawPile;
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }

    public Player getCurrentPlayer() {
        int idx = gameState.getCurrentPlayerIndex();
        if (players.isEmpty() || idx < 0 || idx >= players.size()) {
            return null;
        }
        return players.get(idx);
    }

    /** Discard pile (action cards, rent cards, etc. after card play resolution). */
    public void discardCard(Card c) {
        if (c != null) {
            discardPile.add(c);
        }
    }

    public Player findPlayerByName(String name) {
        if (name == null) return null;
        for (Player p : players) {
            if (name.equals(p.getName())) return p;
        }
        return null;
    }

    public Player localPlayer(String localName) {
        if (localName == null || localName.isBlank()) return getCurrentPlayer();
        Player found = findPlayerByName(localName);
        return found != null ? found : getCurrentPlayer();
    }
}
