package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

/**
 * 一次出牌的可选参数：作银行面值、租金指定颜色、行动/租金目标，以及具体房产目标。
 */
public record CardPlayOptions(
        boolean asBankMoney,
        CardColor rentColorChoice,
        Player actionTargetPlayer,
        PropertyCard sourcePropertyCard,
        PropertyCard targetPropertyCard,
        Property targetPropertyGroup,
        boolean jsnAlreadyChecked) {

    public static CardPlayOptions auto() {
        return new CardPlayOptions(false, null, null, null, null, null, false);
    }

    public static CardPlayOptions bankOnly() {
        return new CardPlayOptions(true, null, null, null, null, null, false);
    }

    public static CardPlayOptions rentWithColor(CardColor color) {
        return new CardPlayOptions(false, color, null, null, null, null, false);
    }

    public static CardPlayOptions rentWithColorAndPlayer(CardColor color, Player payer) {
        return new CardPlayOptions(false, color, payer, null, null, null, false);
    }

    public CardPlayOptions withActionTarget(Player player) {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, player, sourcePropertyCard, targetPropertyCard, targetPropertyGroup, jsnAlreadyChecked);
    }

    public CardPlayOptions withSourcePropertyCard(PropertyCard card) {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, card, targetPropertyCard, targetPropertyGroup, jsnAlreadyChecked);
    }

    public CardPlayOptions withTargetPropertyCard(PropertyCard card) {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, card, targetPropertyGroup, jsnAlreadyChecked);
    }

    public CardPlayOptions withTargetPropertyGroup(Property group) {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, targetPropertyCard, group, jsnAlreadyChecked);
    }

    public CardPlayOptions withJsnAlreadyChecked() {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, targetPropertyCard, targetPropertyGroup, true);
    }
}
