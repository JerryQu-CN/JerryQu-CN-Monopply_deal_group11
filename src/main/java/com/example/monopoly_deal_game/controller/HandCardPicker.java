package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.ActionState;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class HandCardPicker {
    private Card selectedHandCard;
    private final LinkedHashSet<Card> discardSelections = new LinkedHashSet<>();
    private final AtomicBoolean dialogBusy;

    public HandCardPicker(AtomicBoolean dialogBusy) {
        this.dialogBusy = dialogBusy;
    }

    public Card getSelectedHandCard() { return selectedHandCard; }
    public void setSelectedHandCard(Card c) { this.selectedHandCard = c; }
    public LinkedHashSet<Card> getDiscardSelections() { return discardSelections; }
    public void clearAll() { selectedHandCard = null; discardSelections.clear(); }
    public void clearDiscardSelections() { discardSelections.clear(); }

    public void pruneDiscardSelections(Player currentPlayer) {
        if (currentPlayer != null) {
            discardSelections.removeIf(c -> !currentPlayer.getHand().getCards().contains(c));
        }
    }

    public boolean handlePick(Card card, GameSession session, Consumer<String> feedback) {
        if (dialogBusy.get()) {
            feedback.accept("Please complete the current dialog selection first.");
            return false;
        }
        if (card == null || session == null) return false;

        Player local = session.localPlayer(AppContext.get().networkLobbyState().getLocalPlayerName());
        Player current = session.getCurrentPlayer();
        boolean myTurn = local != null && current != null && local.equals(current);
        GameState gs = session.getGameState();

        if (!myTurn) {
            feedback.accept("It is " + (current != null ? current.getName() : "another player") + "'s turn. You can only view your own hand.");
            return true;
        }

        // Check if the player is actually allowed to interact right now.
        // When an action state is active (e.g., rent payment, SlyDeal target),
        // only the involved players can interact, and only through the action state response.
        if (!gs.isPlayerFocused(local)) {
            ActionState as = gs.getActionState();
            if (as != null && as.isTarget(local)) {
                feedback.accept("Please respond to the opponent's action through the dialog.");
            } else {
                feedback.accept("Please wait for the current action to resolve.");
            }
            return true;
        }

        GameState.Phase phase = gs.getPhase();

        if (phase == GameState.Phase.DISCARD_PHASE) {
            selectedHandCard = null;
            if (discardSelections.contains(card)) {
                discardSelections.remove(card);
            } else {
                discardSelections.add(card);
            }
            feedback.accept("");
            return true;
        }

        if (phase != GameState.Phase.PLAY_PHASE) {
            selectedHandCard = null;
            if (phase == GameState.Phase.DRAW_PHASE) {
                feedback.accept("Please click \"Draw Cards\" first, then enter the play phase to select cards.");
            } else {
                feedback.accept("Cannot select hand cards in the current phase.");
            }
            return true;
        }

        discardSelections.clear();
        selectedHandCard = (selectedHandCard == card) ? null : card;
        feedback.accept("");
        return true;
    }
}