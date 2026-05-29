package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.ActionCardHotel;
import com.example.monopoly_deal_game.model.cards.ActionCardHouse;
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
 * A player's property grouping in the property area: composed of multiple {@link PropertyCard} instances, used for same-color set completion, monopoly, and rent calculation.
 * (The former separate {@code PropertySet} has been merged into this class; the cards themselves remain {@link PropertyCard}. See requirements 5.3–5.6, 15.3–15.4.)
 */
public final class Property implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id = UUID.randomUUID().toString();
    private Player owner;
    private final List<PropertyCard> cards = new ArrayList<>();
    /** Houses/hotels placed on this set (each contains one action card). Official rules: at most 1 house + 1 hotel per set. */
    private final List<Card> buildingCards = new ArrayList<>();

    public String getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public List<PropertyCard> getCards() {
        return Collections.unmodifiableList(cards);
    }

    /** Book value: the sum of all property card values in this set. */
    public int getTotalValue() {
        int total = 0;
        for (PropertyCard pc : cards) {
            total += pc.getValue();
        }
        return total;
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
        if (building == null) {
            return false;
        }
        if (!(building instanceof ActionCardHouse) && !(building instanceof ActionCardHotel)) {
            return false;
        }
        if (!isMonopoly()) {
            return false;
        }
        if (building instanceof ActionCardHouse && hasHouseBuilding()) {
            return false;
        }
        if (building instanceof ActionCardHotel && (!hasHouseBuilding() || hasHotelBuilding())) {
            return false;
        }
        buildingCards.add(building);
        return true;
    }

    private boolean hasHouseBuilding() {
        return buildingCards.stream().anyMatch(c -> c instanceof ActionCardHouse);
    }

    private boolean hasHotelBuilding() {
        return buildingCards.stream().anyMatch(c -> c instanceof ActionCardHotel);
    }

    public int getBuildingRentBonus() {
        int b = 0;
        for (Card c : buildingCards) {
            if (c instanceof ActionCardHouse) {
                b += 3;
            } else if (c instanceof ActionCardHotel) {
                b += 4;
            }
        }
        return b;
    }

    public void clearBuildings() {
        buildingCards.clear();
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
        for (PropertyCard pc : cards) {
            if (pc.isMultiColorWild()) {
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
        return hasNonMultiColorAnchor;
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
