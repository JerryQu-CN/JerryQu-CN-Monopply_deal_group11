package com.example.monopoly_deal_game.model.cards;

import java.util.List;

/**
 * 租金卡：用于向其他玩家收取特定颜色物业的租金。
 *
 * <p>双色租金卡列出 {@link #applicableColors}；万能全色租金 {@link #isWildRent} 为 true，颜色列表可为空。
 */
public class RentCard extends Card {

    private final List<CardColor> applicableColors;
    private final boolean isWildRent;

    /** 兼容旧调用：名称固定为 "Rent"。 */
    public RentCard(int id, int value, List<CardColor> applicableColors, boolean isWildRent) {
        this(id, "Rent", value, applicableColors, isWildRent);
    }

    public RentCard(int id, String name, int value, List<CardColor> applicableColors, boolean isWildRent) {
        super(id, name, value, "Charge rent based on your properties in the chosen color(s).");
        this.applicableColors = applicableColors == null ? List.of() : List.copyOf(applicableColors);
        this.isWildRent = isWildRent;
    }

    @Override
    public CardType getCardType() {
        return CardType.RENT;
    }

    public List<CardColor> getApplicableColors() {
        return applicableColors;
    }

    public boolean isWildRent() {
        return isWildRent;
    }
}
