package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A player's property grouping on the table — holds same-color property cards
 * for set completion, monopoly status, and rent calculation.
 */
public final class Property implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id = UUID.randomUUID().toString();
    private Player owner;
    private final List<PropertyCard> cards = new ArrayList<>();
    /** Houses/hotels placed on this set (each contains one action card). Official rules: at most 1 house + 1 hotel per set. */
    private final List<Card> buildingCards = new ArrayList<>();

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public List<PropertyCard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public void addCard(PropertyCard card) {
        if (card == null) return;
        cards.add(card);
    }

    public boolean removeCard(PropertyCard card) {
        return cards.remove(card);
    }

    public List<Card> getBuildingCards() {
        return Collections.unmodifiableList(buildingCards);
    }

    public boolean addBuildingCard(Card building) {
        if (building == null || !building.isBuilding()) return false;
        if (!isMonopoly()) return false;
        if (building.isHouse() && hasHouseBuilding()) return false;
        if (building.isHotel() && (!hasHouseBuilding() || hasHotelBuilding())) return false;
        buildingCards.add(building);
        return true;
    }

    private boolean hasHouseBuilding() {
        return buildingCards.stream().anyMatch(Card::isHouse);
    }

    private boolean hasHotelBuilding() {
        return buildingCards.stream().anyMatch(Card::isHotel);
    }

    public int getBuildingRentBonus() {
        return buildingCards.stream().mapToInt(Card::getBuildingRentBonus).sum();
    }

    /** When the set is broken up or the entire group is taken, remove the house/hotel cards and return them to the caller to discard. */
    public List<Card> takeAllBuildings() {
        List<Card> out = new ArrayList<>(buildingCards);
        buildingCards.clear();
        return out;
    }

    public boolean hasSingleColorProperty() {
        if (cards.isEmpty()) return false;
        CardColor anchor = null;
        boolean anchorFromWild = false;
        boolean hasNonMultiColorAnchor = false;
        boolean hasMultiColorWildAnchor = false;
        CardColor multiWildColor = null;
        for (PropertyCard pc : cards) {
            if (pc.isMultiColorWild()) {
                CardColor c = pc.getCurrentColor();
                if (c != null && c != CardColor.NONE && c != CardColor.WILD) {
                    hasMultiColorWildAnchor = true;
                    if (multiWildColor == null) multiWildColor = c;
                }
                continue;
            }
            CardColor current = pc.getCurrentColor();
            if (current == null || current == CardColor.NONE || current == CardColor.WILD) {
                return false;
            }
            hasNonMultiColorAnchor = true;
            if (anchor == null) {
                anchor = current;
                anchorFromWild = pc.isWild();
            } else if (anchor != current) {
                if (pc.isWild() && pc.getApplicableColors().contains(anchor)) {
                    continue;
                }
                if (anchorFromWild && !pc.isWild()) {
                    PropertyCard anchorCard = findFirstNonRainbowCard();
                    if (anchorCard != null && anchorCard.getApplicableColors().contains(current)) {
                        anchor = current;
                        anchorFromWild = false;
                        continue;
                    }
                }
                return false;
            }
        }
        return hasNonMultiColorAnchor || hasMultiColorWildAnchor;
    }

    private PropertyCard findFirstNonRainbowCard() {
        for (PropertyCard pc : cards) {
            if (!pc.isMultiColorWild()) return pc;
        }
        return null;
    }

    public CardColor getEffectiveColor() {
        if (cards.isEmpty()) return CardColor.NONE;
        if (!hasSingleColorProperty()) return CardColor.NONE;
        // Prefer the color of a non-wild plain card as the anchor color
        for (PropertyCard pc : cards) {
            if (!pc.isMultiColorWild() && !pc.isWild()) {
                return pc.getCurrentColor();
            }
        }
        // When all cards are bi-color wilds, use the first one's currentColor
        for (PropertyCard pc : cards) {
            if (!pc.isMultiColorWild()) {
                return pc.getCurrentColor();
            }
        }
        // When only rainbow wilds exist, use the first one with a declared color
        for (PropertyCard pc : cards) {
            if (pc.isMultiColorWild()) {
                CardColor c = pc.getCurrentColor();
                if (c != null && c != CardColor.NONE && c != CardColor.WILD) {
                    return c;
                }
            }
        }
        return CardColor.NONE;
    }

    public boolean isMonopoly() {
        if (cards.isEmpty()) {
            return false;
        }
        if (!hasSingleColorProperty()) {
            return false;
        }
        CardColor eff = getEffectiveColor();
        if (eff == null || eff == CardColor.NONE) {
            return false;
        }
        int need = requiredSetSize();
        int n = cards.size();
        return n >= need && need >= 1;
    }

    private int requiredSetSize() {
        for (PropertyCard pc : cards) {
            if (!pc.isWild() || !pc.isMultiColorWild()) {
                return Math.max(1, pc.getFullSetThreshold());
            }
        }
        return Integer.MAX_VALUE;
    }

    public boolean accepts(PropertyCard incoming) {
        if (incoming == null) return false;
        if (cards.isEmpty()) return true;
        CardColor anchor = getEffectiveColor();
        if (anchor == CardColor.NONE) {
            return incoming.isMultiColorWild() || hasCompatibleColorPair(incoming);
        }
        if (cards.size() >= requiredSetSize()) {
            return false;
        }
        if (incoming.isMultiColorWild()) {
            return true;
        }
        for (CardColor color : incoming.getApplicableColors()) {
            if (color == anchor) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCompatibleColorPair(PropertyCard incoming) {
        for (PropertyCard existing : cards) {
            if (existing.isMultiColorWild() || incoming.isMultiColorWild()) {
                return true;
            }
            for (CardColor color : incoming.getApplicableColors()) {
                if (color != CardColor.NONE && color != CardColor.WILD && existing.getApplicableColors().contains(color)) {
                    return true;
                }
            }
        }
        return false;
    }
}
