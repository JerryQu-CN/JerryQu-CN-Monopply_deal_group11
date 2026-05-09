package com.example.monopoly_deal_game.game.model;

/**
 * 全局进行标记：当前玩家索引、阶段枚举、是否暂停等待支付/Just Say No 链等。
 */
public class GameState {

    // 1. 定义阶段枚举 (用于区分摸牌、出牌、弃牌)
    public enum Phase { DRAW_PHASE, PLAY_PHASE, DISCARD_PHASE }

    private int currentPlayerIndex = 0;   // 轮到谁了
    private int cardsPlayedThisTurn = 0;  // 需求3：本回合出牌计数
    private boolean hasDrawnThisTurn = false; // 需求2：是否已补牌
    private Phase phase = Phase.DRAW_PHASE;
    private boolean isGameOver = false;

    // TurnManager 需要的所有 Getter 和 Setter

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int index) { this.currentPlayerIndex = index; }

    public int getCardsPlayedThisTurn() { return cardsPlayedThisTurn; }
    public void setCardsPlayedThisTurn(int count) { this.cardsPlayedThisTurn = count; }

    public boolean isHasDrawnThisTurn() { return hasDrawnThisTurn; }
    public void setHasDrawnThisTurn(boolean hasDrawn) { this.hasDrawnThisTurn = hasDrawn; }

    public Phase getPhase() { return phase; }
    public void setPhase(Phase phase) { this.phase = phase; }

    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }
}