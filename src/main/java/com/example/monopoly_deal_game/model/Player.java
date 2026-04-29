package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.card.Card;
import com.example.monopoly_deal_game.model.card.PropertyCard;
import com.example.monopoly_deal_game.model.card.property.PropertyColor;
import com.example.monopoly_deal_game.model.collection.Bank;
import com.example.monopoly_deal_game.model.collection.Hand;
import com.example.monopoly_deal_game.model.collection.PropertySet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {
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
    private List<PropertySet> propertySets;
    
    public Player(String name, boolean isAI) {
        this.playerId = UUID.randomUUID().toString();
        this.name = name;
        this.isAI = isAI;
        this.isActive = true;
        this.isConnected = true;
        this.propertySets = new ArrayList<>();
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
    
    public List<PropertySet> getPropertySets() {
        return propertySets;
    }
    
    public void addPropertySet(PropertySet propertySet) {
        if (propertySet == null) return;
        propertySets.add(propertySet);
        propertySet.setOwner(this);
    }
    
    public void removePropertySet(PropertySet propertySet) {
        if (propertySets.remove(propertySet)) {
            propertySet.setOwner(null);
        }
    }
    
    public PropertySet getPropertySetById(String setId) {
        for (PropertySet ps : propertySets) {
            if (ps.getSetId().equals(setId)) {
                return ps;
            }
        }
        return null;
    }
    
    public int getTotalPropertyValue() {
        int total = 0;
        for (PropertySet ps : propertySets) {
            total += ps.getTotalValue();
        }
        return total;
    }
    
    public boolean hasCompatiblePropertySet(PropertyCard property) {
        for (PropertySet set : propertySets) {
            if (set.isCompatibleWith(property)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasCompatiblePropertySetWithRoom(PropertyCard property) {
        for (PropertySet set : propertySets) {
            if (!set.isMonopoly() && set.isCompatibleWith(property)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasSingleColorPropertySet(PropertyColor color) {
        return getSingleColorPropertySet(color) != null;
    }
    
    public PropertySet getSingleColorPropertySet(PropertyColor color) {
        for (PropertySet set : propertySets) {
            if (set.getEffectiveColor() == color && set.hasSingleColorProperty()) {
                return set;
            }
        }
        return null;
    }
    
    public boolean hasRentableProperties(PropertyColor color) {
        for (PropertySet set : propertySets) {
            for (PropertyCard card : set.getPropertyCards()) {
                if (card.getColors().contains(color) && card.isBase()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean hasRentableProperties(List<PropertyColor> colors) {
        for (PropertyColor color : colors) {
            if (hasRentableProperties(color)) {
                return true;
            }
        }
        return false;
    }
    
    public int getTotalMonetaryAssets() {
        int total = 0;
        for (Card card : bank.getCards()) {
            total += card.getValue();
        }
        for (PropertySet set : propertySets) {
            for (Card card : set.getCards()) {
                total += card.getValue();
            }
        }
        return total;
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
