package com.example.monopoly_deal_game.game.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局进行标记：当前玩家索引、阶段枚举、回合内出牌计数等。
 */
public class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public enum Phase {
        DRAW_PHASE,
        PLAY_PHASE,
        DISCARD_PHASE
    }

    private int currentPlayerIndex;
    private int cardsPlayedThisTurn;
    private boolean hasDrawnThisTurn;
    private Phase phase = Phase.DRAW_PHASE;
    private boolean gameOver;

    /** 打出「加倍租金」后，下一次租金结算翻倍并清空。 */
    private boolean doubleNextRent;

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index;
    }

    public int getCardsPlayedThisTurn() {
        return cardsPlayedThisTurn;
    }

    public void setCardsPlayedThisTurn(int count) {
        this.cardsPlayedThisTurn = count;
    }

    public boolean isHasDrawnThisTurn() {
        return hasDrawnThisTurn;
    }

    public void setHasDrawnThisTurn(boolean hasDrawn) {
        this.hasDrawnThisTurn = hasDrawn;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isDoubleNextRent() {
        return doubleNextRent;
    }

    public void setDoubleNextRent(boolean doubleNextRent) {
        this.doubleNextRent = doubleNextRent;
    }
}
