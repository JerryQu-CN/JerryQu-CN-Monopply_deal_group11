package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.collection.Bank;
import com.example.monopoly_deal_game.model.collection.Hand;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String playerId;
    private String name;
    private boolean isAI;
    private boolean isActive;
    private boolean isConnected;
    private int orderIndex;
    private boolean isCurrentTurn;
    private boolean hasDrawnThisTurn;
    private boolean hasPlayedThisTurn;

    private Hand hand;
    private Bank bank;
    /** Property area: multiple {@link Property} groups, each containing several {@link PropertyCard} instances */
    private final List<Property> properties;

    /**
     * Sequence of card snapshots representing cards "played from hand" in this session, used only for UI table display (decoupled from the physical cards in the bank/property area).
     */
    private final List<PlayedCardSnapshot> playedCardsDisplay = new ArrayList<>();

    private static final int MAX_PLAYED_CARDS_DISPLAY = 48;

    public Player(String name, boolean isAI) {
        this.playerId = UUID.randomUUID().toString();
        this.name = name;
        this.isAI = isAI;
        this.isActive = true;
        this.isConnected = true;
        this.properties = new ArrayList<>();
        this.hand = new Hand();
        this.bank = new Bank();
        this.hand.setOwner(this);
        this.bank.setOwner(this);
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAI() {
        return isAI;
    }

    public boolean isComputer() {
        return isAI;
    }

    public void setAI(boolean AI) {
        isAI = AI;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }

    public void setCurrentTurn(boolean currentTurn) {
        isCurrentTurn = currentTurn;
    }

    public boolean isHasDrawnThisTurn() {
        return hasDrawnThisTurn;
    }

    public void setHasDrawnThisTurn(boolean hasDrawnThisTurn) {
        this.hasDrawnThisTurn = hasDrawnThisTurn;
    }

    public boolean isHasPlayedThisTurn() {
        return hasPlayedThisTurn;
    }

    public void setHasPlayedThisTurn(boolean hasPlayedThisTurn) {
        this.hasPlayedThisTurn = hasPlayedThisTurn;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<PlayedCardSnapshot> getPlayedCardsDisplay() {
        return Collections.unmodifiableList(playedCardsDisplay);
    }

    public void recordPlayedCardForDisplay(PlayedCardSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        playedCardsDisplay.add(snapshot);
        while (playedCardsDisplay.size() > MAX_PLAYED_CARDS_DISPLAY) {
            playedCardsDisplay.remove(0);
        }
    }

    public void clearPlayedCardsDisplay() {
        playedCardsDisplay.clear();
    }

    public void addProperty(Property property) {
        if (property == null) return;
        properties.add(property);
        property.setOwner(this);
    }

    public void removeProperty(Property property) {
        if (properties.remove(property)) {
            property.setOwner(null);
        }
    }

    public boolean hasSingleColorProperty(CardColor color) {
        return getSingleColorProperty(color) != null;
    }

    public Property getSingleColorProperty(CardColor color) {
        for (Property row : properties) {
            if (row.getEffectiveColor() == color && row.hasSingleColorProperty()) {
                return row;
            }
        }
        return null;
    }

    public boolean hasRentableProperties(CardColor color) {
        for (Property row : properties) {
            for (PropertyCard card : row.getCards()) {
                if (card.getColors().contains(color) && card.isBase()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasRentableProperties(List<CardColor> colors) {
        for (CardColor color : colors) {
            if (hasRentableProperties(color)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Number of property groups that have achieved "monopoly" (complete set), used for victory determination (e.g. three complete sets to win rule).
     */
    public int getFullSetCount() {
        int n = 0;
        for (Property p : properties) {
            if (p.isMonopoly()) {
                n++;
            }
        }
        return n;
    }

    public void resetTurnState() {
        hasDrawnThisTurn = false;
        hasPlayedThisTurn = false;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return playerId.equals(player.playerId);
    }

    public int hashCode() {
        return playerId.hashCode();
    }

    public String toString() {
        return name;
    }
}
