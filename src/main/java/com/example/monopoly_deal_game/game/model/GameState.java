package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.logic.PlayerCommand;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Global game state container for turn phases, counters and special flags.
 * All state modifications must go through GameLogic or TurnManager.
 */
public class GameState {
    /** Game turn phases including intermediate waiting states */
    public enum Phase {
        DRAW,           // Must draw before playing any cards
        PLAY,           // Can play up to 3 cards
        DISCARD,        // Must discard to 7 cards
        WAITING_FOR_PAYMENT,    // Waiting for target to pay rent/fee
        WAITING_FOR_JUST_SAY_NO, // Waiting for target to respond to action
        GAME_OVER       // Game has ended
    }

    private int currentPlayerIndex;
    private Phase currentPhase;
    private int cardsPlayedThisTurn;
    private boolean hasDrawnThisTurn;
    private boolean isGameOver;
    private String winnerId;

    private boolean doubleNextRent;
    private boolean isWaitingForResponse;
    private final Deque<PendingAction> pendingActionStack;

    public GameState() {
        this.currentPlayerIndex = 0;
        this.currentPhase = Phase.DRAW;
        this.cardsPlayedThisTurn = 0;
        this.hasDrawnThisTurn = false;
        this.isGameOver = false;
        this.doubleNextRent = false;
        this.isWaitingForResponse = false;
        this.pendingActionStack = new ArrayDeque<>();
    }

    /** Resets all turn-specific state for a new player turn */
    public void resetForNewTurn() {
        this.cardsPlayedThisTurn = 0;
        this.hasDrawnThisTurn = false;
        this.currentPhase = Phase.DRAW;
        this.doubleNextRent = false;
        this.isWaitingForResponse = false;
        this.pendingActionStack.clear();
    }

    /** Checks if player can play another card this turn */
    public boolean canPlayMoreCards() {
        return currentPhase == Phase.PLAY
                && cardsPlayedThisTurn < GameConfig.MAX_PLAY_PER_TURN
                && !isWaitingForResponse;
    }

    /** Increments card play count (Just Say No passes false) */
    public void incrementCardsPlayed(boolean countsTowardLimit) {
        if (countsTowardLimit) {
            this.cardsPlayedThisTurn++;
        }
    }

    // Standard Getters and Setters
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }
    public Phase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(Phase currentPhase) { this.currentPhase = currentPhase; }
    public int getCardsPlayedThisTurn() { return cardsPlayedThisTurn; }
    public boolean isHasDrawnThisTurn() { return hasDrawnThisTurn; }
    public void setHasDrawnThisTurn(boolean hasDrawnThisTurn) { this.hasDrawnThisTurn = hasDrawnThisTurn; }
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; this.isGameOver = true; }
    public boolean isDoubleNextRent() { return doubleNextRent; }
    public void setDoubleNextRent(boolean doubleNextRent) { this.doubleNextRent = doubleNextRent; }
    public boolean isWaitingForResponse() { return isWaitingForResponse; }
    public void setWaitingForResponse(boolean waitingForResponse) { isWaitingForResponse = waitingForResponse; }
    public Deque<PendingAction> getPendingActionStack() { return pendingActionStack; }

    /** Represents an action waiting for player response (Just Say No chain) */
    public static class PendingAction {
        private final PlayerCommand action;
        private final String initiatorId;
        private final String targetId;

        public PendingAction(PlayerCommand action, String initiatorId, String targetId) {
            this.action = action;
            this.initiatorId = initiatorId;
            this.targetId = targetId;
        }

        public PlayerCommand getAction() { return action; }
        public String getInitiatorId() { return initiatorId; }
        public String getTargetId() { return targetId; }
    }
}
