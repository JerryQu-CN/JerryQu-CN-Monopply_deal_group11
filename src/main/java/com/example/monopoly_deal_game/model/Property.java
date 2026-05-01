package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 玩家在物业区的一组地产：由多张 {@link PropertyCard} 组成，用于同色成套、垄断与租金计算。
 * （原单独的 {@code PropertySet} 已并入本类；卡牌本身仍是 {@link PropertyCard}，见需求 5.3–5.6、15.3–15.4。）
 */
public final class Property {

    private final String id = UUID.randomUUID().toString();
    private Player owner;
    private final List<PropertyCard> cards = new ArrayList<>();

    public String getId() {
        return id;
    }

    /** @deprecated 使用 {@link #getId()}；兼容旧命名的调用。 */
    @Deprecated(forRemoval = false)
    public String getSetId() {
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

    /** 与卡牌类型 {@link PropertyCard} 对齐的语义别名。 */
    public List<PropertyCard> getPropertyCards() {
        return getCards();
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
        if (card != null) {
            cards.add(card);
        }
    }

    public boolean removeCard(PropertyCard card) {
        return cards.remove(card);
    }

    public boolean hasSingleColorProperty() {
        if (cards.isEmpty()) return false;
        CardColor anchor = cards.get(0).getCurrentColor();
        return cards.stream().allMatch(p -> Objects.equals(p.getCurrentColor(), anchor));
    }

    /** 同色且不混杂时为锚定颜色；否则视为杂色/万能语义。 */
    public CardColor getEffectiveColor() {
        if (cards.isEmpty()) return CardColor.NONE;
        if (!hasSingleColorProperty()) return CardColor.WILD;
        return cards.get(0).getCurrentColor();
    }

    /** 已满 {@link PropertyCard#getFullSetThreshold()} 张时为垄断。 */
    public boolean isMonopoly() {
        if (cards.isEmpty()) return false;
        int need = Math.max(cards.get(0).getFullSetThreshold(), 1);
        return cards.size() >= need;
    }

    public boolean accepts(PropertyCard incoming) {
        if (incoming == null || cards.isEmpty()) return true;
        CardColor anchor = getEffectiveColor();
        if (anchor == CardColor.NONE || anchor == CardColor.WILD) {
            return true;
        }
        for (CardColor color : incoming.getApplicableColors()) {
            if (color == CardColor.WILD || color == anchor || incoming.isWild()) {
                return true;
            }
        }
        return incoming.getApplicableColors().contains(anchor);
    }
}
