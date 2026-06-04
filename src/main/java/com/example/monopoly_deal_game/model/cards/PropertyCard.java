package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PropertyPlayHelper;
import com.example.monopoly_deal_game.model.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Property cards: plain single-color, bi-color wild, rainbow wild.
 * Aligned with the logic in oldmana.md.server.card.CardProperty from Monopoly-Deal-main.
 */
public class PropertyCard extends Card {

    private final List<CardColor> colors;   // Colors the card possesses (1=single, 2=bi-color, 10=rainbow wild)
    private CardColor currentColor;         // Currently declared/aligned color

    private final boolean isWild;           // Whether this is a wild property
    private final boolean isBase;           // Whether this is a base property (non-base=wild, cannot serve as set anchor color)
    private final boolean stealable;        // Whether it can be stolen by Sly Deal/Forced Deal

    private final int[] rentLevels;         // Rent tier array (starting from 1 card)

    /** Single-color property */
    public PropertyCard(int id, String name, int value, CardColor color, int[] rentLevels) {
        super(id, name, value, "Property Card: " + color.getDisplayName());
        this.colors = new ArrayList<>(List.of(color));
        this.currentColor = color;
        this.isWild = false;
        this.isBase = true;
        this.stealable = true;
        this.rentLevels = rentLevels;
        this.innerColorRGB = 0xFFFFFF;
        // Set the outer border color to the corresponding color
        this.outerColorRGB = colorToRGB(color);
    }

    /** Bi-color wild property */
    public PropertyCard(int id, String name, int value, CardColor c1, CardColor c2, int[] rentLevels, boolean isWild) {
        super(id, name, value, "Wild Property Card (" + c1.getDisplayName() + "/" + c2.getDisplayName() + ")");
        this.colors = new ArrayList<>(List.of(c1, c2));
        this.currentColor = c1;
        this.isWild = isWild;
        this.isBase = true;
        this.stealable = true;
        this.rentLevels = rentLevels;
        this.innerColorRGB = 0xFFFFFF;
        this.outerColorRGB = colorToRGB(c1);
    }

    /** Rainbow wild property (all colors) */
    public PropertyCard(int id, String name, int value, int[] rentLevels, boolean isWild, boolean isBase, boolean stealable) {
        super(id, name, value, "Rainbow Wild Property Card");
        this.colors = new ArrayList<>(CardColor.standardColors());
        this.currentColor = CardColor.WILD;
        this.isWild = isWild;
        this.isBase = isBase;
        this.stealable = stealable;
        this.rentLevels = rentLevels;
        this.innerColorRGB = 0xFFFFFF;
        this.outerColorRGB = 0xFFD700; // Gold
    }

    private static int colorToRGB(CardColor color) {
        return switch (color) {
            case BROWN      -> 0x86461B;
            case LIGHT_BLUE -> 0xBBDEF1;
            case PURPLE     -> 0xBD2F83;
            case ORANGE     -> 0xE38B03;
            case RED        -> 0xD71025;
            case YELLOW     -> 0xF9EF04;
            case GREEN      -> 0x50B42F;
            case BLUE       -> 0x405CA5;
            case BLACK       -> 0x11110E;
            case LIGHT_GREEN -> 0xCEE5B7;
            default         -> 0x808080;
        };
    }

    @Override
    public CardType getCardType() { return CardType.PROPERTY; }

    @Override
    public void executePlay(Player player, GameSession session, CardPlayOptions opt) {
        PropertyPlayHelper.placePropertyCard(player, this);
    }

    @Override
    public String getPlayLogText(String who, CardPlayOptions opts, GameSession session) {
        return who + " placed " + getName() + " on the table";
    }

    @Override
    public String getImageFileName() {
        if (isWild()) {
            return isMultiColorWild() ? "propertyWildCard.png" : wildPropertyFileByColors();
        }
        return colorToPropertyFile(getCurrentColor());
    }

    private String wildPropertyFileByColors() {
        CardColor a = getPrimaryColor();
        CardColor b = getSecondaryColor();
        if (pairMatches(a, b, CardColor.BROWN, CardColor.LIGHT_BLUE)) return "brown-lightblueCard.png";
        if (pairMatches(a, b, CardColor.RED, CardColor.YELLOW)) return "red-yellowCard.png";
        if (pairMatches(a, b, CardColor.PURPLE, CardColor.ORANGE)) return "pink-orangeCard.png";
        if (pairMatches(a, b, CardColor.BLUE, CardColor.GREEN)) return "green-blueCard.png";
        if (pairMatches(a, b, CardColor.BLACK, CardColor.GREEN)) return "green-blackCard.png";
        if (pairMatches(a, b, CardColor.LIGHT_BLUE, CardColor.BLACK)) return "lightBlue-blackCard.png";
        if (pairMatches(a, b, CardColor.BLACK, CardColor.LIGHT_GREEN)) return "black-lightGreenCard.png";
        return "propertyWildCard.png";
    }

    private static boolean pairMatches(CardColor a, CardColor b, CardColor x, CardColor y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    private static String colorToPropertyFile(CardColor c) {
        if (c == null || c == CardColor.NONE) return "propertyWildCard.png";
        return switch (c) {
            case RED -> "redCard.png";
            case BLUE -> "blueCard.png";
            case GREEN -> "greenCard.png";
            case YELLOW -> "yellowCard.png";
            case ORANGE -> "orange.png";
            case PURPLE -> "pinkCard.png";
            case LIGHT_BLUE -> "lightBlueCard.png";
            case BROWN -> "brownCard.png";
            case BLACK -> "blackCard.png";
            case LIGHT_GREEN -> "lightGreenCard.png";
            case WILD -> "propertyWildCard.png";
            default -> "propertyWildCard.png";
        };
    }

    // ---- Color management ----

    public List<CardColor> getColors() { return new ArrayList<>(colors); }

    /** Set of eligible colors (used for set completion and rent determination) */
    public List<CardColor> getApplicableColors() {
        LinkedHashSet<CardColor> set = new LinkedHashSet<>();
        if (isMultiColorWild()) {
            // Rainbow wild: counts as all standard colors
            set.addAll(CardColor.standardColors());
        } else {
            set.addAll(colors);
        }
        return List.copyOf(set);
    }

    public CardColor getCurrentColor() { return currentColor; }

    public void setCurrentColor(CardColor color) {
        if (isMultiColorWild() || getApplicableColors().contains(color)) {
            this.currentColor = color;
        }
    }

    /** Align a wild card to the specified anchor color */
    public void alignToDeclaredColor(CardColor anchor) {
        if (!isWild || anchor == null || anchor == CardColor.NONE || anchor == CardColor.WILD) return;
        if (isMultiColorWild()) {
            this.currentColor = anchor;
            return;
        }
        if (colors.contains(anchor)) {
            this.currentColor = anchor;
        }
    }

    // ---- Property queries ----

    public boolean isWild() { return isWild; }

    /** Rainbow wild (possesses all 10 standard colors) */
    public boolean isMultiColorWild() {
        return isWild && colors.size() >= 10;
    }

    /** Whether this is a bi-color wild (non-rainbow) */
    public boolean isBiColor() { return colors.size() == 2 && isWild; }

    /** Bi-color wild or rainbow wild supports manual color switching */
    public boolean canFlipWildDualColor() {
        return isWild && (isBiColor() || isMultiColorWild());
    }

    /** List of selectable colors */
    public List<CardColor> getSelectableColors() {
        if (isMultiColorWild()) return CardColor.standardColors();
        return new ArrayList<>(colors);
    }

    public boolean isBase() { return isBase; }
    public boolean isStealable() { return stealable; }

    public boolean hasColor(CardColor color) { return colors.contains(color); }

    public boolean isSingleColor() { return colors.size() == 1 && !isWild; }

    // ---- Rent ----

    public int[] getRentLevels() { return rentLevels; }

    public int getRent(int propertyCount) {
        if (rentLevels == null || propertyCount <= 0 || propertyCount > rentLevels.length) return 0;
        return rentLevels[propertyCount - 1];
    }

    public int getFullSetThreshold() {
        return rentLevels != null ? rentLevels.length : Integer.MAX_VALUE;
    }

    // ---- Backward-compatible interface ----

    public CardColor getPrimaryColor() { return colors.isEmpty() ? CardColor.NONE : colors.get(0); }
    public CardColor getSecondaryColor() { return colors.size() > 1 ? colors.get(1) : null; }
}