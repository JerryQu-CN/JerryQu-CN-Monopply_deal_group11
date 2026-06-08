package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.CardColor;

/**
 * Provides abbreviated display labels for {@link CardColor} values.
 */
public final class CardColorLabel {
    private CardColorLabel() {}

    public static String shortLabel(CardColor c) {
        if (c == null) return "?";
        return switch (c) {
            case BROWN -> "Brn";
            case LIGHT_BLUE -> "LBl";
            case PURPLE -> "Prp";
            case ORANGE -> "Org";
            case RED -> "Red";
            case YELLOW -> "Yel";
            case GREEN -> "Grn";
            case BLUE -> "Blu";
            case BLACK -> "Blk";
            case LIGHT_GREEN -> "LGr";
            case NONE -> "--";
        };
    }
}