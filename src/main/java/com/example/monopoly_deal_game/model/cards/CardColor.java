package com.example.monopoly_deal_game.model.cards;

import java.util.List;
import java.util.Set;
import java.util.EnumSet;

/**
 * Monopoly Deal property color enumeration, aligned with the official 10-color system.
 * Each color carries standard rent tiers, maximum set size, and buildable flag.
 */
public enum CardColor {
    BROWN       ("Brown",       new int[] {1, 2},       true),
    LIGHT_BLUE  ("Light Blue",  new int[] {1, 2, 3},    true),
    PURPLE      ("Purple",      new int[] {1, 2, 4},    true),
    ORANGE      ("Orange",      new int[] {1, 3, 5},    true),
    RED         ("Red",         new int[] {2, 3, 6},    true),
    YELLOW      ("Yellow",      new int[] {2, 4, 6},    true),
    GREEN       ("Green",       new int[] {2, 4, 7},    true),
    BLUE        ("Dark Blue",   new int[] {3, 8},       true),
    BLACK       ("Black",       new int[] {1, 2, 3, 4}, true),
    LIGHT_GREEN ("Light Green", new int[] {1, 2},       true),
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

    /** The rent amounts for this color based on property count (1-indexed), e.g. rents[0]=rent for 1 property, rents[1]=rent for 2 properties */
    public int[] getRents() { return rents; }

    /** Get the rent tier for a given property count */
    public int getRent(int propertyCount) {
        if (rents == null || propertyCount <= 0 || propertyCount > rents.length) return 0;
        return rents[propertyCount - 1];
    }

    /** Maximum number of properties in a full set */
    public int getMaxProperties() { return rents != null ? rents.length : 0; }

    /** Whether houses/hotels can be placed on a full set of this color */
    public boolean isBuildable() { return buildable; }

    /** All "table colors" (the 10 colors that can collect rent) */
    public static final Set<CardColor> TABLE_COLORS = EnumSet.of(
            BROWN, LIGHT_BLUE, PURPLE, ORANGE, RED,
            YELLOW, GREEN, BLUE, BLACK, LIGHT_GREEN);

    /** Standard property colors (excluding WILD / NONE) */
    public static List<CardColor> standardColors() {
        return List.of(BROWN, LIGHT_BLUE, PURPLE, ORANGE, RED,
                       YELLOW, GREEN, BLUE, BLACK, LIGHT_GREEN);
    }
}