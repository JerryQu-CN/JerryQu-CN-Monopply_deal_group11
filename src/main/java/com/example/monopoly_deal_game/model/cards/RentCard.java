package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardColorLabel;
import com.example.monopoly_deal_game.logic.CardEffectExecutor;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

import java.util.List;

/**
 * Rent card — charges other players rent based on owned properties of specific colors.
 * Bi-color and wild rent cards carry applicable color lists.
 */
public class RentCard extends Card {

    private final List<CardColor> applicableColors;
    private final boolean isWildRent;

    /** Backward-compatible constructor: name defaults to "Rent". */
    public RentCard(int id, int value, List<CardColor> applicableColors, boolean isWildRent) {
        this(id, "Rent", value, applicableColors, isWildRent);
    }

    public RentCard(int id, String name, int value, List<CardColor> applicableColors, boolean isWildRent) {
        super(id, name, value, "Charge rent based on your properties in the chosen color(s).");
        this.applicableColors = applicableColors == null ? List.of() : List.copyOf(applicableColors);
        this.isWildRent = isWildRent;
    }

    @Override
    public CardType getCardType() {
        return CardType.RENT;
    }

    @Override
    public void executePlay(Player player, GameSession session, CardPlayOptions opt) {
        CardEffectExecutor.resolveRent(session, player, this, opt);
        session.discardCard(this);
    }

    @Override
    public String getPlayLogText(String who, CardPlayOptions opts, GameSession session) {
        String color = opts.rentColorChoice() != null
                ? CardColorLabel.shortLabel(opts.rentColorChoice()) : "?";
        String dbl = session.getGameState().isDoubleNextRent() ? " (x2)" : "";
        return who + " charged " + color + " rent" + dbl;
    }

    @Override
    public String getImageFileName() {
        if (isWildRent) return "rainbowRent.png";
        if (applicableColors.size() >= 2) {
            return twoColorRentFile(applicableColors.get(0), applicableColors.get(1));
        }
        return "rainbowRent.png";
    }

    private static String twoColorRentFile(CardColor a, CardColor b) {
        if (colorPairMatches(a, b, CardColor.GREEN, CardColor.BLUE)) return "green-blueRent.png";
        if (colorPairMatches(a, b, CardColor.ORANGE, CardColor.PURPLE)) return "pink-orangeRent.png";
        if (colorPairMatches(a, b, CardColor.RED, CardColor.YELLOW)) return "red-yellow.png";
        if (colorPairMatches(a, b, CardColor.BROWN, CardColor.LIGHT_BLUE)) return "brown-lightBlue.png";
        if (colorPairMatches(a, b, CardColor.GREEN, CardColor.BLACK)
                || colorPairMatches(a, b, CardColor.GREEN, CardColor.LIGHT_GREEN)) return "black-lightGreenRent.png";
        if (colorPairMatches(a, b, CardColor.BLACK, CardColor.LIGHT_GREEN)) return "black-lightGreenRent.png";
        return "rainbowRent.png";
    }

    private static boolean colorPairMatches(CardColor a, CardColor b, CardColor x, CardColor y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    public List<CardColor> getApplicableColors() {
        return applicableColors;
    }

    public boolean isWildRent() {
        return isWildRent;
    }
}
