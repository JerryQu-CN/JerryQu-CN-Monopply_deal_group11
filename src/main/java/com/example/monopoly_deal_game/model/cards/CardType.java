package com.example.monopoly_deal_game.model.cards;

/**
 * 卡牌类型枚举，对齐官方 Monopoly Deal 牌种。
 * House 和 Hotel 作为 ActionCard 的子类（ActionCardHouse / ActionCardHotel）实现。
 */
public enum CardType {
    PROPERTY,   // 物业卡（房产卡）
    ACTION,     // 行动卡
    RENT,       // 租金卡
    CURRENCY,   // 货币卡
    RULE        // 规则说明书卡（官方 4 张，开局可移出牌库）
}