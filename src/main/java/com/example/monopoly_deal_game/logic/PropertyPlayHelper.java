package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Places property cards onto a player's board, transfers groups between players,
 * and manages building (house/hotel) attachments.
 */
public final class PropertyPlayHelper {

    private static final List<CardColor> BOARD_COLOR_RANK =
            List.of(CardColor.BROWN, CardColor.LIGHT_BLUE, CardColor.PURPLE, CardColor.ORANGE,
                    CardColor.RED, CardColor.YELLOW, CardColor.GREEN, CardColor.BLUE,
                    CardColor.BLACK, CardColor.LIGHT_GREEN, CardColor.NONE);
    private static final Comparator<Property> PROPERTY_BOARD_ORDER =
            Comparator.comparingInt(PropertyPlayHelper::displayRank);

    private PropertyPlayHelper() {}

    private static int displayRank(Property row) {
        if (row == null) return 999;
        CardColor c = row.getEffectiveColor();
        int i = BOARD_COLOR_RANK.indexOf(c);
        return i >= 0 ? i : 100 + c.ordinal();
    }

    public static void sortBoardPropertiesNaturalOrder(Player owner) {
        if (owner == null) return;
        List<Property> list = owner.getProperties();
        if (list.size() > 1) list.sort(PROPERTY_BOARD_ORDER);
    }

    public static void placePropertyCard(Player owner, PropertyCard card) {
        placePropertyCardInternal(owner, card);
        sortBoardPropertiesNaturalOrder(owner);
    }

    public static void transferPropertyGroup(Player from, Player to, Property group, GameSession session) {
        Objects.requireNonNull(from); Objects.requireNonNull(to); Objects.requireNonNull(group); Objects.requireNonNull(session);
        from.removeProperty(group);
        List<PropertyCard> toPlace = new ArrayList<>(group.getCards());
        List<Card> buildings = new ArrayList<>(group.takeAllBuildings());
        for (PropertyCard pc : toPlace) {
            group.removeCard(pc);
            placePropertyCardInternal(to, pc);
        }
        attachBuildingsFromStolenSet(to, buildings, session);
        sortBoardPropertiesNaturalOrder(to);
    }

    private static void placePropertyCardInternal(Player owner, PropertyCard card) {
        for (Property row : owner.getProperties()) {
            if (row.accepts(card) && chosenColorMatchesRow(row, card)) {
                alignWildToRow(row, card);
                row.addCard(card);
                return;
            }
        }
        Property nov = new Property();
        if (card.isMultiColor()) {
            card.alignToDeclaredColor(card.getCurrentColor());
        }
        nov.addCard(card);
        owner.addProperty(nov);
    }

    private static void attachBuildingsFromStolenSet(Player to, List<Card> buildings, GameSession session) {
        if (buildings == null || buildings.isEmpty()) return;
        buildings.sort(Comparator.comparingInt(b -> b.isHotel() ? 1 : 0));
        for (Card b : buildings) {
            boolean placed = false;
            if (b.isBuilding()) {
                for (Property row : to.getProperties()) {
                    if (row.isMonopoly() && row.addBuildingCard(b)) {
                        placed = true;
                        break;
                    }
                }
            }
            if (!placed) session.discardCard(b);
        }
    }

    private static boolean chosenColorMatchesRow(Property row, PropertyCard incoming) {
        if (row == null || incoming == null || row.getCards().isEmpty()) return true;
        if (!incoming.isMultiColor()) return true;
        CardColor chosen = incoming.getCurrentColor();
        if (chosen == null || chosen == CardColor.NONE) return true;
        CardColor rowColor = row.getEffectiveColor();
        if (rowColor == CardColor.NONE) return true;
        return rowColor == chosen;
    }

    private static void alignWildToRow(Property row, PropertyCard incoming) {
        if (incoming == null || row == null || row.getCards().isEmpty()) return;
        CardColor anchor = row.getEffectiveColor();
        if (anchor == CardColor.NONE) {
            for (PropertyCard pc : row.getCards()) {
                if (pc.isRainbow()) continue;
                CardColor c = pc.getCurrentColor();
                if (c != null && c != CardColor.NONE) { anchor = c; break; }
            }
        }
        if (anchor == null || anchor == CardColor.NONE) return;

        if (incoming.isMultiColor()) {
            incoming.alignToDeclaredColor(anchor);
        } else {
            // Incoming single-color card — shift existing bi-color cards to match
            CardColor incC = incoming.getCurrentColor();
            if (incC != null && incC != CardColor.NONE) {
                for (PropertyCard pc : row.getCards()) {
                    if (pc.isBiColor() && pc.getCurrentColor() == anchor
                            && pc.getApplicableColors().contains(incC)) {
                        pc.alignToDeclaredColor(incC);
                    }
                }
            }
        }
    }

    /** Move a wild property card from its current group to the group matching {@code newColor}. */
    public static void moveWildCardToColor(Player owner, PropertyCard card, CardColor newColor, GameSession session) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(card);
        Objects.requireNonNull(newColor);
        Objects.requireNonNull(session);

        Property oldGroup = null;
        for (Property group : new ArrayList<>(owner.getProperties())) {
            if (group.getCards().contains(card)) {
                oldGroup = group;
                break;
            }
        }
        if (oldGroup == null) return;

        oldGroup.removeCard(card);

        if (oldGroup.getCards().isEmpty()) {
            for (Card b : oldGroup.takeAllBuildings()) session.discardCard(b);
            owner.removeProperty(oldGroup);
        } else if (!oldGroup.isMonopoly()) {
            for (Card b : oldGroup.takeAllBuildings()) session.discardCard(b);
        }

        card.alignToDeclaredColor(newColor);
        placePropertyCard(owner, card);
    }

    public static void removePropertyCardFromBoard(Player victim, PropertyCard pc, GameSession session) {
        Objects.requireNonNull(victim); Objects.requireNonNull(pc); Objects.requireNonNull(session);
        for (Property row : new ArrayList<>(victim.getProperties())) {
            if (!row.removeCard(pc)) continue;
            if (row.getCards().isEmpty()) {
                for (Card b : row.takeAllBuildings()) session.discardCard(b);
                victim.removeProperty(row);
            } else if (!row.isMonopoly()) {
                for (Card b : row.takeAllBuildings()) session.discardCard(b);
            }
            sortBoardPropertiesNaturalOrder(victim);
            return;
        }
    }
}
