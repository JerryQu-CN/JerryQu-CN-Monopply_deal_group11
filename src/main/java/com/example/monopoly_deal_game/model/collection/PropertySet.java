package com.example.monopoly_deal_game.model.collection;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 一套地产（同色或组员逻辑中的相容集合）。与 {@link PropertyCard#getFullSetThreshold()} 配合判断是否“整套”。
 */
public final class PropertySet {

    private final String setId = UUID.randomUUID().toString();
    private Player owner;
    private final List<PropertyCard> propertyCards = new ArrayList<>();

    public String getSetId() {
        return setId;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public List<PropertyCard> getPropertyCards() {
        return Collections.unmodifiableList(propertyCards);
    }

    /** 物业区入账面价值（卡牌面值之和）。 */
    public int getTotalValue() {
        int total = 0;
        for (PropertyCard pc : propertyCards) {
            total += pc.getValue();
        }
        return total;
    }

    public void addCard(PropertyCard card) {
        if (card != null) {
            propertyCards.add(card);
        }
    }

    public boolean removeCard(PropertyCard card) {
        return propertyCards.remove(card);
    }

    public List<Card> getCards() {
        List<Card> out = new ArrayList<>();
        out.addAll(propertyCards);
        return Collections.unmodifiableList(out);
    }

    public boolean hasSingleColorProperty() {
        if (propertyCards.isEmpty()) return false;
        CardColor anchor = propertyCards.get(0).getCurrentColor();
        return propertyCards.stream().allMatch(p -> Objects.equals(p.getCurrentColor(), anchor));
    }

    /** 同色且非多套混合时返回锚定颜色；否则视为杂色成套。 */
    public CardColor getEffectiveColor() {
        if (propertyCards.isEmpty()) return CardColor.NONE;
        if (!hasSingleColorProperty()) return CardColor.WILD;
        return propertyCards.get(0).getCurrentColor();
    }

    /** 已满 {@link PropertyCard#getFullSetThreshold()} 时为“垄断”。 */
    public boolean isMonopoly() {
        if (propertyCards.isEmpty()) return false;
        int need = Math.max(propertyCards.get(0).getFullSetThreshold(), 1);
        return propertyCards.size() >= need;
    }

    public boolean isCompatibleWith(PropertyCard property) {
        if (property == null || propertyCards.isEmpty()) return true;
        CardColor anchor = getEffectiveColor();
        if (anchor == CardColor.NONE || anchor == CardColor.WILD) {
            return true;
        }
        for (CardColor color : property.getApplicableColors()) {
            if (color == CardColor.WILD || color == anchor || property.isWild()) {
                return true;
            }
        }
        return property.getApplicableColors().contains(anchor);
    }
}
