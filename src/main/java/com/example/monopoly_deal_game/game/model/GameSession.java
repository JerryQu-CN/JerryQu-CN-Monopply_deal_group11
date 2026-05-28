package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一局游戏的聚合根：玩家列表、抽/弃牌堆与 {@link GameState}。
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

    /** 摸牌堆（栈顶为列表尾部，可约定）。 */
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

    /** 弃牌堆（出牌结算后的行动牌、租金卡等）。 */
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
