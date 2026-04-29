package com.example.monopoly_deal_game.model;

import java.util.List;

/**
 * 租金卡：用于向其他玩家收取特定颜色物业的租金。
 *
 * TODO(model): 适用颜色范围、万能租金判定；需求 7–8。
 */
public class RentCard extends Card {

    private final List<CardColor> applicableColors; // 该租金卡支持的颜色（通常是2种，全彩色租金卡为多个或WILD）
    private final boolean isWildRent;               // 是否为全彩色万能租金卡（面向所有颜色）

    /**
     * @param id               卡牌ID
     * @param value            银行面值
     * @param applicableColors 适用的颜色列表
     * @param isWildRent       是否为万能租金卡
     */
    public RentCard(int id, int value, List<CardColor> applicableColors, boolean isWildRent) {
        super(id, "Rent", value, "Collect rent for properties of specific colors.");
        this.applicableColors = applicableColors;
        this.isWildRent = isWildRent;
    }

    @Override
    public void use(Player user, Player target) {
        // 1. 如果是双色租金，让玩家选一个颜色；如果是万能租金，选任意已有颜色。
        // 2. 根据 PropertyCard 里的 rentLevels 计算金额。
        // 3. 发起支付请求。
        throw new UnsupportedOperationException("TODO(logic): 判定地产颜色组并计算租金");
    }

    public List<CardColor> getApplicableColors() {
        return applicableColors;
    }

    public boolean isWildRent() {
        return isWildRent;
    }
}