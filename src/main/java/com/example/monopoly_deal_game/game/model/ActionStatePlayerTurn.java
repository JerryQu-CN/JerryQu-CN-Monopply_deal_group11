package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;

/**
 * Turn state: three phases of DRAW -> PLAY -> DISCARD.
 * Aligned with the design of ActionStatePlayerTurn in Monopoly-Deal-main.
 */
public class ActionStatePlayerTurn extends ActionState {
    @Serial
    private static final long serialVersionUID = 1L;

    public enum TurnPhase { DRAW, PLAY, DISCARD }

    private TurnPhase phase = TurnPhase.DRAW;
    private int moves;
    private boolean hasDrawn;

    public ActionStatePlayerTurn(Player player, int movesPerTurn) {
        super(player);
        this.moves = movesPerTurn;
        this.hasDrawn = false;
        setStatus(player.getName() + "'s Turn: Draw");
    }

    public TurnPhase getTurnPhase() {
        return phase;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
        updatePhase();
    }

    public void decrementMoves() {
        decrementMoves(1);
    }

    public void decrementMoves(int amount) {
        moves = Math.max(moves - amount, 0);
        updatePhase();
    }

    public void incrementMoves() {
        incrementMoves(1);
    }

    public void incrementMoves(int amount) {
        moves += amount;
        updatePhase();
    }

    public boolean hasDrawn() {
        return hasDrawn;
    }

    public void setDrawn() {
        if (phase == TurnPhase.DRAW) {
            hasDrawn = true;
            phase = TurnPhase.PLAY;
            updatePhase();
        }
    }

    /**
     * Also records that the draw was done (convenience for empty-hand 5-draw).
     */
    public void setDrawn(boolean drawn) {
        hasDrawn = drawn;
        if (drawn && phase == TurnPhase.DRAW) {
            phase = TurnPhase.PLAY;
        }
        updatePhase();
    }

    public boolean isDrawing() {
        return phase == TurnPhase.DRAW;
    }

    public boolean canPlayCards() {
        return phase == TurnPhase.PLAY && moves > 0;
    }

    public boolean isDiscarding() {
        return phase == TurnPhase.DISCARD;
    }

    /**
     * Re-evaluate the phase based on current state.
     * When moves == 0 and hand needs discarding → DISCARD, else → PLAY (if drawn).
     */
    public void updatePhase() {
        if (phase == TurnPhase.DRAW) {
            return;
        }
        TurnPhase prev = phase;
        Player owner = getActionOwner();
        boolean tooManyCards = owner != null && owner.getHand().size() > 7;

        if (moves == 0 && tooManyCards) {
            phase = TurnPhase.DISCARD;
            setStatus("Waiting for " + owner.getName() + " to discard");
        } else {
            phase = TurnPhase.PLAY;
            if (moves > 0) {
                setStatus(owner != null ? owner.getName() + "'s Turn" : "Turn");
            } else {
                setStatus("Waiting for " + (owner != null ? owner.getName() : "player") + " to finish turn");
            }
        }
    }

    @Override
    public boolean isFinished() {
        return false; // Turn state never auto-finishes
    }
}