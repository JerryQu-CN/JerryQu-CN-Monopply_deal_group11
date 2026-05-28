package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.cards.ActionCard;
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
 * 玩家在物业区的一组地产：由多张 {@link PropertyCard} 组成，用于同色成套、垄断与租金计算。
 * （原单独的 {@code PropertySet} 已并入本类；卡牌本身仍是 {@link PropertyCard}，见需求 5.3–5.6、15.3–15.4。）
 */
public final class Property implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id = UUID.randomUUID().toString();
    private Player owner;
    private final List<PropertyCard> cards = new ArrayList<>();
    /** 置于该成套上的房屋/旅馆（各含一张行动牌）。官方每套至多 1 房 + 1 店。 */
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

    /** 账面：该成套内所有物业牌的金额之和。 */
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
        if (building instanceof ActionCard ac) {
            if (ac.getActionType() != ActionCard.ActionType.HOUSE
                    && ac.getActionType() != ActionCard.ActionType.HOTEL) {
                return false;
            }
        } else {
            return false;
        }
        if (getEffectiveColor() == CardColor.RAILROAD) {
            return false;
        }
        if (!isMonopoly()) {
            return false;
        }
        if (acn(building) == ActionCard.ActionType.HOUSE && hasHouseBuilding()) {
            return false;
        }
        if (acn(building) == ActionCard.ActionType.HOTEL && (!hasHouseBuilding() || hasHotelBuilding())) {
            return false;
        }
        buildingCards.add(building);
        return true;
    }

    private static ActionCard.ActionType acn(Card c) {
        return ((ActionCard) c).getActionType();
    }

    private boolean hasHouseBuilding() {
        return buildingCards.stream().anyMatch(c -> c instanceof ActionCard ac && ac.getActionType() == ActionCard.ActionType.HOUSE);
    }

    private boolean hasHotelBuilding() {
        return buildingCards.stream().anyMatch(c -> c instanceof ActionCard ac && ac.getActionType() == ActionCard.ActionType.HOTEL);
    }

    public int getBuildingRentBonus() {
        int b = 0;
        for (Card c : buildingCards) {
            if (c instanceof ActionCard ac) {
                if (ac.getActionType() == ActionCard.ActionType.HOUSE) {
                    b += 3;
                } else if (ac.getActionType() == ActionCard.ActionType.HOTEL) {
                    b += 4;
                }
            }
        }
        return b;
    }

    public void clearBuildings() {
        buildingCards.clear();
    }

    /** 成套被拆散或将整组移走时，将房屋/旅馆牌取下并交由调用方丢弃。 */
    public List<Card> takeAllBuildings() {
        List<Card> out = new ArrayList<>(buildingCards);
        buildingCards.clear();
        return out;
    }

    public boolean hasSingleColorProperty() {
        if (cards.isEmpty()) return false;
        CardColor anchor = null;
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
            } else if (anchor != current) {
                return false;
            }
        }
        return hasNonMultiColorAnchor;
    }

    /** 同色且不混杂时为锚定颜色；否则视为杂色/万能语义。 */
    public CardColor getEffectiveColor() {
        if (cards.isEmpty()) return CardColor.NONE;
        if (!hasSingleColorProperty()) return CardColor.WILD;
        for (PropertyCard pc : cards) {
            if (!pc.isMultiColorWild()) {
                return pc.getCurrentColor();
            }
        }
        return CardColor.WILD;
    }

    /**
     * 成套垄断：同色（含对齐后的万能）且达到该颜色要求的张数。
     * 混血行或尚未声明为单色系的万能行不构成垄断。
     */
    public boolean isMonopoly() {
        if (cards.isEmpty()) {
            return false;
        }
        if (!hasSingleColorProperty()) {
            return false;
        }
        CardColor eff = getEffectiveColor();
        if (eff == null || eff == CardColor.NONE || eff == CardColor.WILD) {
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
        if (anchor == CardColor.NONE || anchor == CardColor.WILD) {
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
