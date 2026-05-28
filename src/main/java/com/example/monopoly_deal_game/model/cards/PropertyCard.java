package com.example.monopoly_deal_game.model.cards;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 物业牌：普通单色、双色万能、彩虹万能。
 * 对齐 Monopoly-Deal-main 中 oldmana.md.server.card.CardProperty 的逻辑。
 */
public class PropertyCard extends Card {

    private final List<CardColor> colors;   // 卡牌拥有的颜色（1=单色，2=双色，10=彩虹万能）
    private CardColor currentColor;         // 当前声明/对齐的颜色

    private final boolean isWild;           // 是否万能属性
    private final boolean isBase;           // 是否基础物业（非基础=万能，不能作为成套锚定色）
    private final boolean stealable;        // 是否可被 Sly Deal/Forced Deal 偷取

    private final int[] rentLevels;         // 租金档位数组（从 1 张开始）

    /** 单色物业 */
    public PropertyCard(int id, String name, int value, CardColor color, int[] rentLevels) {
        super(id, name, value, "Property Card: " + color.getDisplayName());
        this.colors = new ArrayList<>(List.of(color));
        this.currentColor = color;
        this.isWild = false;
        this.isBase = true;
        this.stealable = true;
        this.rentLevels = rentLevels;
        this.innerColorRGB = 0xFFFFFF;
        // 设置外框颜色为对应颜色
        this.outerColorRGB = colorToRGB(color);
    }

    /** 双色万能物业 */
    public PropertyCard(int id, String name, int value, CardColor c1, CardColor c2, int[] rentLevels, boolean isWild) {
        super(id, name, value, "Wild Property Card (" + c1.getDisplayName() + "/" + c2.getDisplayName() + ")");
        this.colors = new ArrayList<>(List.of(c1, c2));
        this.currentColor = c1;
        this.isWild = isWild;
        this.isBase = true;
        this.stealable = true;
        this.rentLevels = rentLevels;
        this.innerColorRGB = 0xFFFFFF;
        this.outerColorRGB = colorToRGB(c1);
    }

    /** 彩虹万能物业（所有颜色） */
    public PropertyCard(int id, String name, int value, int[] rentLevels, boolean isWild, boolean isBase, boolean stealable) {
        super(id, name, value, "Rainbow Wild Property Card");
        this.colors = new ArrayList<>(CardColor.standardColors());
        this.currentColor = CardColor.WILD;
        this.isWild = isWild;
        this.isBase = isBase;
        this.stealable = stealable;
        this.rentLevels = rentLevels;
        this.innerColorRGB = 0xFFFFFF;
        this.outerColorRGB = 0xFFD700; // 金色
    }

    private static int colorToRGB(CardColor color) {
        return switch (color) {
            case BROWN      -> 0x86461B;
            case LIGHT_BLUE -> 0xBBDEF1;
            case PURPLE     -> 0xBD2F83;
            case ORANGE     -> 0xE38B03;
            case RED        -> 0xD71025;
            case YELLOW     -> 0xF9EF04;
            case GREEN      -> 0x50B42F;
            case BLUE       -> 0x405CA5;
            case BLACK       -> 0x11110E;
            case LIGHT_GREEN -> 0xCEE5B7;
            default         -> 0x808080;
        };
    }

    @Override
    public CardType getCardType() { return CardType.PROPERTY; }

    // ---- 颜色管理 ----

    public List<CardColor> getColors() { return new ArrayList<>(colors); }

    /** 可算作的合法颜色集合（用于成套与租金判定） */
    public List<CardColor> getApplicableColors() {
        LinkedHashSet<CardColor> set = new LinkedHashSet<>();
        if (isMultiColorWild()) {
            // 彩虹万能：可算作所有标准颜色
            set.addAll(CardColor.standardColors());
        } else {
            set.addAll(colors);
        }
        return List.copyOf(set);
    }

    public CardColor getCurrentColor() { return currentColor; }

    public void setCurrentColor(CardColor color) {
        if (isMultiColorWild() || getApplicableColors().contains(color)) {
            this.currentColor = color;
        }
    }

    /** 将万能牌对齐到指定锚定色系 */
    public void alignToDeclaredColor(CardColor anchor) {
        if (!isWild || anchor == null || anchor == CardColor.NONE || anchor == CardColor.WILD) return;
        if (isMultiColorWild()) {
            this.currentColor = anchor;
            return;
        }
        if (colors.contains(anchor)) {
            this.currentColor = anchor;
        }
    }

    // ---- 属性查询 ----

    public boolean isWild() { return isWild; }

    /** 彩虹万能（拥有所有 10 种标准颜色） */
    public boolean isMultiColorWild() {
        return isWild && colors.size() >= 10;
    }

    /** 是否双色万能（非彩虹） */
    public boolean isBiColor() { return colors.size() == 2 && isWild; }

    /** 双色万能或彩虹万能支持手动切换颜色 */
    public boolean canFlipWildDualColor() {
        return isWild && (isBiColor() || isMultiColorWild());
    }

    /** 可切换的颜色列表 */
    public List<CardColor> getSelectableColors() {
        if (isMultiColorWild()) return CardColor.standardColors();
        return new ArrayList<>(colors);
    }

    public boolean isBase() { return isBase; }
    public boolean isStealable() { return stealable; }

    public boolean hasColor(CardColor color) { return colors.contains(color); }

    public boolean isSingleColor() { return colors.size() == 1 && !isWild; }

    // ---- 租金 ----

    public int[] getRentLevels() { return rentLevels; }

    public int getRent(int propertyCount) {
        if (rentLevels == null || propertyCount <= 0 || propertyCount > rentLevels.length) return 0;
        return rentLevels[propertyCount - 1];
    }

    public int getFullSetThreshold() {
        return rentLevels != null ? rentLevels.length : Integer.MAX_VALUE;
    }

    // ---- 兼容旧接口 ----

    public CardColor getPrimaryColor() { return colors.isEmpty() ? CardColor.NONE : colors.get(0); }
    public CardColor getSecondaryColor() { return colors.size() > 1 ? colors.get(1) : null; }
}