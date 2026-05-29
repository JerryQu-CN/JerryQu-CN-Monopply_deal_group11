package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Global game state: current player index, per-turn card play count, ActionState stack.
 * Aligned with the design of GameState in Monopoly-Deal-main -- controls phases and blocking via the ActionState stack.
 */
public class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    private int currentPlayerIndex;
    private int cardsPlayedThisTurn;
    private boolean gameOver;

    /** Double rent counter */
    private boolean doubleNextRent;
    private int doubleRentCount;

    // ---- ActionState stack ----

    private ActionStatePlayerTurn turnState;
    private final LinkedList<ActionState> states = new LinkedList<>();

    // ---- Accessors ----

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

    public int getDoubleRentCount() {
        return doubleRentCount;
    }

    public void setDoubleRentCount(int count) {
        this.doubleRentCount = count;
    }

    // ---- ActionState stack ----

    /**
     * Returns the current active action state — the top of the stack,
     * or the turn state if the stack is empty.
     */
    public ActionState getActionState() {
        return states.isEmpty() ? turnState : states.getFirst();
    }

    public ActionStatePlayerTurn getTurnState() {
        return turnState;
    }

    public void setTurnState(ActionStatePlayerTurn ts) {
        this.turnState = ts;
        if (ts != null) {
            ts.setGameState(this);
        }
    }

    public boolean hasTurnState() {
        return turnState != null;
    }

    public int getMovesRemaining() {
        return turnState != null ? turnState.getMoves() : 0;
    }

    /**
     * Adds an action state to the front of the queue.
     * Non-important states already present are removed first.
     */
    public void addActionState(ActionState state) {
        if (state == null || states.contains(state) || state.isFinished()) {
            return;
        }
        states.removeIf(s -> !s.isImportant());
        states.addFirst(state);
        state.setGameState(this);
        state.onAdd();
    }

    /**
     * Removes a specific action state from the queue.
     */
    public void removeActionState(ActionState state) {
        if (states.remove(state)) {
            state.onRemove();
        }
    }

    /**
     * Clears all action states from the queue (except turn state).
     */
    public void clearActionStates() {
        for (ActionState s : new ArrayList<>(states)) {
            states.remove(s);
            s.onRemove();
        }
    }

    /**
     * Replaces oldState with newState at the same position.
     */
    public void swapActionState(ActionState oldState, ActionState newState) {
        int idx = states.indexOf(oldState);
        if (idx < 0) {
            throw new IllegalArgumentException("Old state not in stack");
        }
        states.remove(idx);
        oldState.onRemove();
        states.add(idx, newState);
        newState.setGameState(this);
        newState.onAdd();
    }

    // ---- Backward-compatible Phase enum (derived from ActionState stack) ----

    public enum Phase {
        DRAW_PHASE,
        PLAY_PHASE,
        DISCARD_PHASE,
        WAITING_FOR_SELECTION
    }

    /**
     * Derives the current phase from the action state stack.
     */
    public Phase getPhase() {
        if (turnState == null) return Phase.PLAY_PHASE;
        ActionState as = getActionState();
        if (as == turnState) {
            if (turnState.isDrawing()) return Phase.DRAW_PHASE;
            if (turnState.isDiscarding()) return Phase.DISCARD_PHASE;
            return Phase.PLAY_PHASE;
        }
        return Phase.WAITING_FOR_SELECTION;
    }

    /**
     * Compatibility: delegates to turnState.hasDrawn().
     */
    public boolean isHasDrawnThisTurn() {
        return turnState != null && turnState.hasDrawn();
    }

    /**
     * Compatibility: no-op. Phase is now derived from the action state stack.
     */
    public void setPhase(Phase phase) {
        // Phase is now derived from the ActionState stack. This setter exists
        // for backward compatibility with UI controllers that used to call it.
    }

    /**
     * Compatibility: delegates to turnState.setDrawn().
     */
    public void setHasDrawnThisTurn(boolean v) {
        if (turnState != null) turnState.setDrawn(v);
    }

    // ---- Convenience queries ----

    /**
     * Whether the given player is currently focused (it's their turn and no
     * action state is overriding the turn state).
     */
    public boolean isPlayerFocused(Player player) {
        return hasTurnState()
                && getActionState() == turnState
                && turnState.getActionOwner() == player;
    }

    /**
     * Whether the given player owns the current turn (regardless of action state override).
     */
    public boolean isPlayerTurn(Player player) {
        return hasTurnState() && turnState.getActionOwner() == player;
    }

    /**
     * Whether it is currently this player's turn and they can play cards.
     */
    public boolean canPlayerPlayCards(Player player) {
        return isPlayerFocused(player) && turnState.canPlayCards();
    }

    /**
     * Whether the given player can draw cards right now.
     */
    public boolean canPlayerDraw(Player player) {
        return isPlayerFocused(player) && turnState.isDrawing();
    }
}