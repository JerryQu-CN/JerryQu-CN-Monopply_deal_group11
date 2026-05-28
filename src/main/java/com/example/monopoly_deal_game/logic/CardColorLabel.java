package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.CardColor;

public final class CardColorLabel {
    private CardColorLabel() {}

    public static String shortLabel(CardColor c) {
        if (c == null) return "?";
        return switch (c) {
            case BROWN -> "棕";
            case LIGHT_BLUE -> "浅蓝";
            case PURPLE -> "紫";
            case ORANGE -> "橙";
            case RED -> "红";
            case YELLOW -> "黄";
            case GREEN -> "绿";
            case BLUE -> "深蓝";
            case BLACK -> "黑";
            case LIGHT_GREEN -> "浅绿";
            case NONE -> "—";
        };
    }
}