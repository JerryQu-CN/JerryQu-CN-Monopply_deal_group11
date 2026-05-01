package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.collection.Bank;
import com.example.monopoly_deal_game.model.collection.Hand;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

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
    /** 物业区：多组 {@link Property}，每组内含若干 {@link PropertyCard} */
    private final List<Property> properties;

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

    /** 与同项目 {@link ComputerPlayer#isComputer()} 命名兼容。 */
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

    public Property getPropertyById(String id) {
        for (Property p : properties) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public int getTotalPropertyValue() {
        int total = 0;
        for (Property ps : properties) {
            total += ps.getTotalValue();
        }
        return total;
    }

    /** 是否存在可收下该 {@link PropertyCard} 的一组物业。 */
    public boolean hasCompatibleProperty(PropertyCard card) {
        for (Property row : properties) {
            if (row.accepts(card)) {
                return true;
            }
        }
        return false;
    }

    /** 未满垄断且相容时，仍可往该组合并。 */
    public boolean hasCompatiblePropertyWithRoom(PropertyCard card) {
        for (Property row : properties) {
            if (!row.isMonopoly() && row.accepts(card)) {
                return true;
            }
        }
        return false;
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

    public int getTotalMonetaryAssets() {
        int total = 0;
        for (Card card : bank.getCards()) {
            total += card.getValue();
        }
        for (Property row : properties) {
            for (PropertyCard pc : row.getCards()) {
                total += pc.getValue();
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
