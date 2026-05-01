package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.model.Player;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * 物业牌（含普通物业、双色系、万能物业 Wild）。
 * * 对应需求说明书：
 * - 5.4: 必须放置在物业区。
 * - 5.5: 包含租金档位信息，支持整套判定。
 * - 5.6: 颜色切换逻辑。
 */
public class PropertyCard extends Card {

    private final CardColor primaryColor;
    private final CardColor secondaryColor;
    private CardColor currentColor;

    private final boolean isWild;
    private final int[] rentLevels;
    private final int fullSetThreshold;

    public PropertyCard(int id, String name, int value, CardColor color, int[] rentLevels) {
        super(id, name, value, "Property Card: " + color);
        this.primaryColor = color;
        this.currentColor = color;
        this.secondaryColor = null;
        this.isWild = false;
        this.rentLevels = rentLevels;
        this.fullSetThreshold = (rentLevels != null) ? rentLevels.length : 0;
    }

    public PropertyCard(int id, String name, int value, CardColor c1, CardColor c2, int[] rentLevels, boolean isWild) {
        super(id, name, value, "Wild Property Card");
        this.primaryColor = c1;
        this.secondaryColor = c2;
        this.currentColor = c1;
        this.isWild = isWild;
        this.rentLevels = rentLevels;
        this.fullSetThreshold = (rentLevels != null) ? rentLevels.length : 0;
    }

    public void switchColor() {
        if (isWild && secondaryColor != null && secondaryColor != CardColor.WILD) {
            this.currentColor = (this.currentColor == primaryColor) ? secondaryColor : primaryColor;
        }
    }

    public int getRent(int count) {
        if (rentLevels == null || count <= 0) return 0;
        int index = Math.min(count, rentLevels.length) - 1;
        return rentLevels[index];
    }

    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("TODO(model+logic): 放入物业区而非直接当效果牌使用");
    }

    public CardColor getCurrentColor() {
        return currentColor;
    }

    public boolean isWild() {
        return isWild;
    }

    public int getFullSetThreshold() {
        return fullSetThreshold;
    }

    public int[] getRentLevels() {
        return rentLevels;
    }

    /** 与 {@link Player} / {@link com.example.monopoly_deal_game.model.Property} 协作：可算作的颜色用于成套与租金判定。 */
    public List<CardColor> getApplicableColors() {
        LinkedHashSet<CardColor> set = new LinkedHashSet<>();
        set.add(primaryColor);
        if (secondaryColor != null && secondaryColor != CardColor.NONE) {
            set.add(secondaryColor);
        }
        set.add(getCurrentColor());
        return List.copyOf(set);
    }

    /** 与同组 Player 提交的 {@code PropertyCard#getColors()} 命名对齐。 */
    public List<CardColor> getColors() {
        return getApplicableColors();
    }

    /** 暂无房屋/旅馆牌时默认为“基础物业牌”。 */
    public boolean isBase() {
        return true;
    }
}
