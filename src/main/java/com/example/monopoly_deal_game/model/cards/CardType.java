package com.example.monopoly_deal_game.model.cards;

public enum CardType {
    PROPERTY,   // 地产卡
    ACTION,     // 功能卡
    RENT,       // 租金卡
    CURRENCY,   // 货币卡
    BUILDING,   // 建筑卡（房子/旅馆），亦可用 {@code ActionCard} 表示
    RULE        // 规则说明书卡（官方 4 张，可开局移出牌库）
}
