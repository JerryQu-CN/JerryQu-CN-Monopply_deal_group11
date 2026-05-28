package com.example.monopoly_deal_game.model.cards;

import java.util.List;
import java.util.Set;
import java.util.EnumSet;

/**
 * 大富翁 Deal 物业颜色枚举，对齐官方 10 种颜色体系。
 * 每种颜色携带标准租金档位、最大成套张数、是否可建造信息。
 */
public enum CardColor {
    BROWN       ("Brown",      new int[] {1, 2},       true),
    LIGHT_BLUE  ("Light Blue", new int[] {1, 2, 3},    true),
    PURPLE      ("Purple",     new int[] {1, 2, 4},    true),
    ORANGE      ("Orange",     new int[] {1, 3, 5},    true),
    RED         ("Red",        new int[] {2, 3, 6},    true),
    YELLOW      ("Yellow",     new int[] {2, 4, 6},    true),
    GREEN       ("Green",      new int[] {2, 4, 7},    true),
    BLUE        ("Dark Blue",  new int[] {3, 8},       true),
    RAILROAD    ("Railroad",   new int[] {1, 2, 3, 4}, false),
    UTILITY     ("Utility",    new int[] {1, 2},       false),
    WILD,
    NONE;

    private final String displayName;
    private final int[] rents;
    private final boolean buildable;

    CardColor() {
        this(null, null, false);
    }

    CardColor(String displayName, int[] rents, boolean buildable) {
        this.displayName = displayName;
        this.rents = rents;
        this.buildable = buildable;
    }

    public String getDisplayName() { return displayName; }

    /** 该颜色对应几张物业卡时的租金金额（1-indexed），例如 rents[0]=1张物业的租金，rents[1]=2张物业的租金 */
    public int[] getRents() { return rents; }

    /** 获取指定张数下的租金档位 */
    public int getRent(int propertyCount) {
        if (rents == null || propertyCount <= 0 || propertyCount > rents.length) return 0;
        return rents[propertyCount - 1];
    }

    /** 最大成套张数 */
    public int getMaxProperties() { return rents != null ? rents.length : 0; }

    /** 该颜色成套上是否允许放置房屋/旅馆 */
    public boolean isBuildable() { return buildable; }

    /** 所有"桌面颜色"（可营收租金的 10 种） */
    public static final Set<CardColor> TABLE_COLORS = EnumSet.of(
            BROWN, LIGHT_BLUE, PURPLE, ORANGE, RED,
            YELLOW, GREEN, BLUE, RAILROAD, UTILITY);

    /** 标准物业颜色（不含 WILD / NONE） */
    public static List<CardColor> standardColors() {
        return List.of(BROWN, LIGHT_BLUE, PURPLE, ORANGE, RED,
                       YELLOW, GREEN, BLUE, RAILROAD, UTILITY);
    }
}