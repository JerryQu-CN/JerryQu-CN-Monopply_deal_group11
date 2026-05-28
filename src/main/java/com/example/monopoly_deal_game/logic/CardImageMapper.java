package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.ActionCardDealBreaker;
import com.example.monopoly_deal_game.model.cards.ActionCardDebtCollector;
import com.example.monopoly_deal_game.model.cards.ActionCardDoubleTheRent;
import com.example.monopoly_deal_game.model.cards.ActionCardForcedDeal;
import com.example.monopoly_deal_game.model.cards.ActionCardHotel;
import com.example.monopoly_deal_game.model.cards.ActionCardHouse;
import com.example.monopoly_deal_game.model.cards.ActionCardItsMyBirthday;
import com.example.monopoly_deal_game.model.cards.ActionCardJustSayNo;
import com.example.monopoly_deal_game.model.cards.ActionCardPassGo;
import com.example.monopoly_deal_game.model.cards.ActionCardSlyDeal;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;
import com.example.monopoly_deal_game.view.CardFaceResolver;

import java.util.List;

/**
 * 将 {@link Card} 映射到资源目录 {@code images/cards} 下的 PNG 文件名。
 * 与 {@link CardFaceResolver} 保持一致，供逻辑层记录出牌展示等使用。
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
            if (ac instanceof ActionCardDealBreaker) return "dealBreaker.png";
            if (ac instanceof ActionCardForcedDeal) return "forcedDeal.png";
            if (ac instanceof ActionCardSlyDeal) return "slyDeal.png";
            if (ac instanceof ActionCardItsMyBirthday) return "birthday.png";
            if (ac instanceof ActionCardDebtCollector) return "debtCollector.png";
            if (ac instanceof ActionCardJustSayNo) return "justSayNo.png";
            if (ac instanceof ActionCardPassGo) return "passGo.png";
            if (ac instanceof ActionCardDoubleTheRent) return "doubleTheRent.png";
            if (ac instanceof ActionCardHouse) return "house.png";
            if (ac instanceof ActionCardHotel) return "hotel.png";
            return "passGo.png";
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
        if (card instanceof PropertyCard pc) {
            if (pc.isWild()) {
                return pc.isMultiColorWild() ? "propertyWildCard.png" : wildPropertyFileByExactColors(pc);
            }
            return colorToPropertyFile(pc.getCurrentColor());
        }
        return "propertyWildCard.png";
    }

    private static String wildPropertyFileByExactColors(PropertyCard pc) {
        CardColor a = pc.getPrimaryColor();
        CardColor b = pc.getSecondaryColor();
        if (pairEquals(a, b, CardColor.BROWN, CardColor.LIGHT_BLUE)) return "brown-lightblueCard.png";
        if (pairEquals(a, b, CardColor.RED, CardColor.YELLOW)) return "red-yellowCard.png";
        if (pairEquals(a, b, CardColor.PURPLE, CardColor.ORANGE)) return "pink-orangeCard.png";
        if (pairEquals(a, b, CardColor.BLUE, CardColor.GREEN)) return "green-blueCard.png";
        if (pairEquals(a, b, CardColor.BLACK, CardColor.GREEN)) return "green-blackCard.png";
        if (pairEquals(a, b, CardColor.LIGHT_BLUE, CardColor.BLACK)) return "lightBlue-blackCard.png";
        if (pairEquals(a, b, CardColor.BLACK, CardColor.LIGHT_GREEN)) return "black-lightGreenCard.png";
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
            case BLACK, LIGHT_GREEN -> "blackCard.png";
            case WILD -> "propertyWildCard.png";
            default -> throw new IllegalStateException("Unexpected value: " + c);
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
        if (pairEquals(a, b, CardColor.GREEN, CardColor.BLACK)
                || pairEquals(a, b, CardColor.GREEN, CardColor.LIGHT_GREEN)) {
            return "black-lightGreenRent.png";
        }
        if (pairEquals(a, b, CardColor.BLACK, CardColor.LIGHT_GREEN)) {
            return "black-lightGreenRent.png";
        }
        return "rainbowRent.png";
    }

    private static boolean pairEquals(CardColor a, CardColor b, CardColor x, CardColor y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    private static boolean containsPair(List<CardColor> colors, CardColor a, CardColor b) {
        return colors != null && colors.contains(a) && colors.contains(b);
    }
}
