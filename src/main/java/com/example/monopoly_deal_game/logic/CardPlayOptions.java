package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

/**
 * Optional parameters for playing a card: use as bank money, specify a rent color,
 * action/rent target player, and specific property card/group targets.
 */
public record CardPlayOptions(
        boolean asBankMoney,
        CardColor rentColorChoice,
        Player actionTargetPlayer,
        PropertyCard sourcePropertyCard,
        PropertyCard targetPropertyCard,
        Property targetPropertyGroup,
        boolean jsnBlocked) {

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
                asBankMoney, rentColorChoice, player, sourcePropertyCard, targetPropertyCard, targetPropertyGroup, jsnBlocked);
    }

    public CardPlayOptions withSourcePropertyCard(PropertyCard card) {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, card, targetPropertyCard, targetPropertyGroup, jsnBlocked);
    }

    public CardPlayOptions withTargetPropertyCard(PropertyCard card) {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, card, targetPropertyGroup, jsnBlocked);
    }

    public CardPlayOptions withTargetPropertyGroup(Property group) {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, targetPropertyCard, group, jsnBlocked);
    }

    public CardPlayOptions withJsnBlocked() {
        return new CardPlayOptions(
                asBankMoney, rentColorChoice, actionTargetPlayer, sourcePropertyCard, targetPropertyCard, targetPropertyGroup, true);
    }

}