package com.example.monopoly_deal_game.logic;



import com.example.monopoly_deal_game.model.Player;

import com.example.monopoly_deal_game.model.Property;

import com.example.monopoly_deal_game.model.cards.CardColor;

import com.example.monopoly_deal_game.model.cards.PropertyCard;



/**

 * 一次出牌的可选参数：作银行面值、租金指定颜色、「向哪位对手结算」的行动目标等。

 *

 * @param asBankMoney {@code true} 时行动牌、物业牌、租金卡均作现金入账，不触发牌面效果。

 * @param rentColorChoice 使用租金卡时声明收取的颜色；{@code null} 表示由逻辑按「可收最高租」自动选取。

 * @param actionTargetPlayer 行动牌指向的对手，或租金卡「由谁支付」；{@code null} 时在仅有一名对手时可自动补齐。

 */

public record CardPlayOptions(

        boolean asBankMoney, CardColor rentColorChoice, Player actionTargetPlayer, PropertyCard sourcePropertyCard, PropertyCard targetPropertyCard, Property targetPropertyGroup) {



    public static CardPlayOptions auto() {

        return new CardPlayOptions(false, null, null, null, null, null);

    }



    public static CardPlayOptions bankOnly() {

        return new CardPlayOptions(true, null, null, null, null, null);

    }



    public static CardPlayOptions rentWithColor(CardColor color) {

        return new CardPlayOptions(false, color, null, null, null, null);

    }



    /** 打租金卡：指定租金颜色（或已由规则推导）以及向哪位对手收租（多人时必须明确）。 */

    public static CardPlayOptions rentWithColorAndPlayer(CardColor color, Player payer) {

        return new CardPlayOptions(false, color, payer, null, null, null);

    }



    public CardPlayOptions withActionTarget(Player p) {

        return new CardPlayOptions(asBankMoney, rentColorChoice, p, sourcePropertyCard, targetPropertyCard, targetPropertyGroup);

    }

    public CardPlayOptions withSourcePropertyCard(PropertyCard card) {

        return new CardPlayOptions(asBankMoney, rentColorChoice, actionTargetPlayer, card, targetPropertyCard, targetPropertyGroup);

    }

    public CardPlayOptions withTargetPropertyCard(PropertyCard card) {

        return new CardPlayOptions(asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, card, targetPropertyGroup);

    }

    public CardPlayOptions withTargetPropertyGroup(Property group) {

        return new CardPlayOptions(asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, targetPropertyCard, group);

    }

}

