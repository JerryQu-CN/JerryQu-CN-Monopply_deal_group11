package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;

import java.util.List;

/**
 * 将 {@link Card} 映射到资源目录 {@code images/cards} 下的 PNG 文件名。
 * 与 {@link com.example.monopoly_deal_game.view.CardFaceResolver} 保持一致，供逻辑层记录出牌展示等使用。
 */
public final class CardImageMapper {

    private CardImageMapper() {}

    public static String imageFileFor(Card card) {
        if (card instanceof BankCard bc) {
            return switch (bc.getValue()) {
                case 1 -> "1M.png";
                case 2 -> "2M.png";
                case 3 -> "3M.png";
                case 4 -> "4M.png";
                case 5 -> "5M.png";
                case 10 -> "5M.png";
                default -> "1M.png";
            };
        }
        if (card instanceof ActionCard ac) {
            return switch (ac.getActionType()) {
                case DEAL_BREAKER -> "dealBreaker.png";
                case FORCE_DEAL -> "slyDeal.png";
                case SLY_DEAL -> "slyDeal.png";
                case ITS_MY_BIRTHDAY -> "birthday.png";
                case DEBT_COLLECTOR -> "debtCollector.png";
                case JUST_SAY_NO -> "justSayNo.png";
                case PASS_GO -> "passGo.png";
                case DOUBLE_RENT -> "double the ren.png.png";
                case HOUSE -> "house.png";
                case HOTEL -> "hotel.png";
            };
        }
        if (card instanceof PropertyCard pc) {
            if (pc.isWild()) {
                return "propertyWildCard.png";
            }
            return colorToPropertyFile(pc.getCurrentColor());
        }
        if (card instanceof RentCard rc) {
            if (rc.isWildRent()) {
                return "rainbowRent.png";
            }
            List<CardColor> colors = rc.getApplicableColors();
            if (colors != null && colors.size() >= 2) {
                return twoColorRentFile(colors.get(0), colors.get(1));
            }
            if (colors != null && colors.size() == 1) {
                return "rainbowRent.png";
            }
            return "rainbowRent.png";
        }
        return "propertyWildCard.png";
    }

    private static String colorToPropertyFile(CardColor c) {
        if (c == null || c == CardColor.NONE) {
            return "propertyWildCard.png";
        }
        return switch (c) {
            case RED -> "redCard.png";
            case BLUE -> "blueCard.png";
            case GREEN -> "greenCard.png";
            case YELLOW -> "yellowCard.png";
            case ORANGE -> "orange.png";
            case PURPLE -> "pinkCard.png";
            case LIGHT_BLUE -> "lightBlueCard.png";
            case BROWN -> "brownCard.png";
            case RAILROAD, UTILITY -> "blackCard.png";
            case WILD -> "propertyWildCard.png";
            case NONE -> "propertyWildCard.png";
        };
    }

    private static String twoColorRentFile(CardColor a, CardColor b) {
        if (pairEquals(a, b, CardColor.GREEN, CardColor.BLUE)) {
            return "green-blueRent.png";
        }
        if (pairEquals(a, b, CardColor.ORANGE, CardColor.PURPLE)) {
            return "pink-orangeRent.png";
        }
        if (pairEquals(a, b, CardColor.RED, CardColor.YELLOW)) {
            return "red-yellow.png";
        }
        if (pairEquals(a, b, CardColor.BROWN, CardColor.LIGHT_BLUE)) {
            return "brown-lightBlue.png";
        }
        if (pairEquals(a, b, CardColor.GREEN, CardColor.RAILROAD)
                || pairEquals(a, b, CardColor.GREEN, CardColor.UTILITY)) {
            return "black-lightGreenRent.png";
        }
        if (pairEquals(a, b, CardColor.RAILROAD, CardColor.UTILITY)) {
            return "black-lightGreenRent.png";
        }
        return "rainbowRent.png";
    }

    private static boolean pairEquals(CardColor a, CardColor b, CardColor x, CardColor y) {
        return (a == x && b == y) || (a == y && b == x);
    }
}
