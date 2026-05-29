package com.example.monopoly_deal_game.model.cards;

import java.util.List;

/**
 * Rent card: used to charge other players rent based on properties of specific colors.
 *
 * <p>Bi-color rent cards list their {@link #applicableColors}; for wild rent {@link #isWildRent} is true and the color list may be empty.
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

    public List<CardColor> getApplicableColors() {
        return applicableColors;
    }

    public boolean isWildRent() {
        return isWildRent;
    }
}
