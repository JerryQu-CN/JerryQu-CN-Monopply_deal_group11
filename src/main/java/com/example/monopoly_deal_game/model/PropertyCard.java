package com.example.monopoly_deal_game.model;

/**
 * 物业牌（含双色系、万能物业 Wild）。
 *
 * TODO(model): 颜色枚举、当前选择颜色、租金档位、是否整套；需求 5.4–5.6。
 */
public class PropertyCard extends Card {

    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("TODO(model+logic): 放入物业区而非直接当效果牌使用");
    }
}
