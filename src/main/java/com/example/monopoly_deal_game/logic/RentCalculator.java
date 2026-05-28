package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.List;

/**
 * 租金计算器，基于桌面物业套的当前颜色与张数返回正确的租金档位。
 * 对齐 Monopoly-Deal-main 中 CardProperty.getRent() / PropertyColor.getRent() 的逻辑。
 */
public final class RentCalculator {

    private RentCalculator() {}

    /** 计算 landlord 在指定颜色下可收取的最高租金（遍历该颜色所有成套，取最大值，含建筑加成）。 */
    public static int rentOnColor(Player landlord, CardColor color) {
        int best = 0;
        for (Property row : landlord.getProperties()) {
            if (row.getEffectiveColor() != color) continue;
            List<PropertyCard> cs = row.getCards();
            if (cs.isEmpty()) continue;
            int baseRent = baseRentForSet(cs, color);
            int bonus = row.getBuildingRentBonus();
            best = Math.max(best, baseRent + bonus);
        }
        return best;
    }

    /** 在所有候选颜色中选出 landlord 可收取的最高租金。 */
    public static int bestRentForLandlord(Player landlord, List<CardColor> chosenColors,
                                          boolean doubleRent) {
        int best = 0;
        for (CardColor c : chosenColors) {
            if (c != null && c != CardColor.NONE && c != CardColor.WILD) {
                best = Math.max(best, rentOnColor(landlord, c));
            }
        }
        if (best == 0) {
            for (CardColor c : CardColor.TABLE_COLORS) {
                best = Math.max(best, rentOnColor(landlord, c));
            }
        }
        return doubleRent ? best * 2 : best;
    }

    /** 最佳万能租金（遍历所有 10 种颜色）。 */
    public static int bestRentWild(Player landlord, boolean doubleRent) {
        int best = 0;
        for (CardColor c : CardColor.TABLE_COLORS) {
            best = Math.max(best, rentOnColor(landlord, c));
        }
        return doubleRent ? best * 2 : best;
    }

    private static int baseRentForSet(List<PropertyCard> cs, CardColor color) {
        int n = cs.size();
        // 找一张非万能的基础物业卡获取租金档位
        for (PropertyCard pc : cs) {
            if (!pc.isWild() || pc.isBase()) {
                return pc.getRent(n);
            }
        }
        // fallback: 用第一张的租金档位
        return cs.get(0).getRent(n);
    }
}