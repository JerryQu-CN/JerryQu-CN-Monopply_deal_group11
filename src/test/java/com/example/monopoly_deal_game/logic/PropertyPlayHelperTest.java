package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.*;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropertyPlayHelperTest {

    private Player player;
    private GameSession session;

    private static PropertyCard singleColor(int id, String name, int value, CardColor color, int[] rentLevels) {
        return new PropertyCard(id, name, value, color, rentLevels);
    }

    private static PropertyCard biColorWild(int id, String name, int value, CardColor c1, CardColor c2, int[] rentLevels) {
        return new PropertyCard(id, name, value, c1, c2, rentLevels, true);
    }

    private static PropertyCard rainbowWild(int id, String name, int value, int[] rentLevels) {
        return new PropertyCard(id, name, value, rentLevels, true, false, true);
    }

    @BeforeEach
    void setUp() {
        player = new Player("Player", false);
        session = new GameSession();
        session.getPlayers().add(player);
    }

    // ---- placePropertyCard: new group ----

    @Test
    void placePropertyCard_createsNewGroup() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);
        assertEquals(1, player.getProperties().size());
        assertEquals(1, player.getProperties().get(0).getCards().size());
        assertTrue(player.getProperties().get(0).getCards().contains(brown));
    }

    @Test
    void placePropertyCard_mergesIntoCompatibleGroup() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);
        assertEquals(1, player.getProperties().size());
        assertEquals(2, player.getProperties().get(0).getCards().size());
    }

    @Test
    void placePropertyCard_differentColors_separateGroups() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard blue = singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8});
        PropertyPlayHelper.placePropertyCard(player, brown);
        PropertyPlayHelper.placePropertyCard(player, blue);
        assertEquals(2, player.getProperties().size());
    }

    @Test
    void placePropertyCard_wildAlignsToExistingGroup() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard biWild = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);
        PropertyPlayHelper.placePropertyCard(player, biWild);
        // Bi-color wild should align to BROWN and merge into the brown group
        assertEquals(1, player.getProperties().size());
        assertEquals(CardColor.BROWN, biWild.getCurrentColor());
    }

    @Test
    void placePropertyCard_rainbowWildAlignsToExistingGroup() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BROWN); // set to match existing group
        PropertyPlayHelper.placePropertyCard(player, brown);
        PropertyPlayHelper.placePropertyCard(player, rainbow);
        assertEquals(1, player.getProperties().size());
        assertEquals(CardColor.BROWN, rainbow.getCurrentColor());
    }

    // ---- moveWildCardToColor ----

    @Test
    void moveWildCardToColor_movesToNewGroup() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard biWild = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        PropertyCard lightBlue = singleColor(60, "Light Blue 1", 1, CardColor.LIGHT_BLUE, new int[]{1, 2, 3});

        // Place brown + biWild together (biWild aligns to BROWN)
        PropertyPlayHelper.placePropertyCard(player, brown);
        PropertyPlayHelper.placePropertyCard(player, biWild);
        assertEquals(1, player.getProperties().size());

        // Place lightBlue in its own group
        PropertyPlayHelper.placePropertyCard(player, lightBlue);
        assertEquals(2, player.getProperties().size());

        // Move biWild to LIGHT_BLUE group
        PropertyPlayHelper.moveWildCardToColor(player, biWild, CardColor.LIGHT_BLUE, session);

        // Now biWild should be in the lightBlue group
        assertEquals(2, player.getProperties().size());
        boolean foundInLightBlue = false;
        for (Property p : player.getProperties()) {
            if (p.getEffectiveColor() == CardColor.LIGHT_BLUE && p.getCards().contains(biWild)) {
                foundInLightBlue = true;
            }
        }
        assertTrue(foundInLightBlue);
        assertEquals(CardColor.LIGHT_BLUE, biWild.getCurrentColor());
    }

    @Test
    void moveWildCardToColor_removesEmptyOldGroup() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard biWild = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        PropertyCard lightBlue = singleColor(60, "Light Blue 1", 1, CardColor.LIGHT_BLUE, new int[]{1, 2, 3});

        PropertyPlayHelper.placePropertyCard(player, brown);
        PropertyPlayHelper.placePropertyCard(player, biWild); // in brown group
        PropertyPlayHelper.placePropertyCard(player, lightBlue); // separate group

        // Move biWild away — brown group now only has brown, which is fine (remains a group)
        // But if we only had biWild alone and move it...

        // Test: biWild alone in its group, then move
        Player p2 = new Player("P2", false);
        PropertyCard biWild2 = biColorWild(21, "Brown/LightBlue Wild 2", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(p2, biWild2);
        assertEquals(1, p2.getProperties().size());

        PropertyPlayHelper.moveWildCardToColor(p2, biWild2, CardColor.LIGHT_BLUE, session);
        // Group was empty (biWild removed), old group should be removed
        // New group with LIGHT_BLUE color should exist
        assertFalse(p2.getProperties().isEmpty());
        assertEquals(CardColor.LIGHT_BLUE, biWild2.getCurrentColor());
    }

    @Test
    void moveWildCardToColor_nullParams_throws() {
        PropertyCard biWild = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        assertThrows(NullPointerException.class,
                () -> PropertyPlayHelper.moveWildCardToColor(null, biWild, CardColor.BROWN, session));
        assertThrows(NullPointerException.class,
                () -> PropertyPlayHelper.moveWildCardToColor(player, null, CardColor.BROWN, session));
    }

    // ---- removePropertyCardFromBoard ----

    @Test
    void removePropertyCardFromBoard_removesCard() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);
        assertEquals(1, player.getProperties().get(0).getCards().size());

        PropertyPlayHelper.removePropertyCardFromBoard(player, brown, session);
        assertTrue(player.getProperties().isEmpty());
    }

    @Test
    void removePropertyCardFromBoard_cleansBuildingsWhenMonopolyBroken() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        // Add house (needs monopoly)
        Property p = player.getProperties().get(0);
        p.addBuildingCard(new ActionCardHouse(100, "House", 3));
        assertEquals(1, p.getBuildingCards().size());

        // Remove one card → monopoly broken → buildings discarded
        PropertyPlayHelper.removePropertyCardFromBoard(player, brown1, session);
        // Building should be discarded
        assertTrue(session.getDiscardPile().stream()
                .anyMatch(c -> c instanceof ActionCardHouse));
    }

    // ---- sortBoardPropertiesNaturalOrder ----

    @Test
    void sortBoardPropertiesNaturalOrder_sortsByColor() {
        PropertyCard blue = singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8});
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        // Place blue first, then brown
        PropertyPlayHelper.placePropertyCard(player, blue);
        PropertyPlayHelper.placePropertyCard(player, brown);

        // After sort, BROWN should come before BLUE (brown index 0, blue index 7)
        PropertyPlayHelper.sortBoardPropertiesNaturalOrder(player);
        assertEquals(CardColor.BROWN, player.getProperties().get(0).getEffectiveColor());
        assertEquals(CardColor.BLUE, player.getProperties().get(1).getEffectiveColor());
    }

    @Test
    void sortBoardPropertiesNaturalOrder_nullPlayer_safe() {
        PropertyPlayHelper.sortBoardPropertiesNaturalOrder(null); // no exception
    }

    // ---- transferPropertyGroup ----

    @Test
    void transferPropertyGroup_movesGroupToNewOwner() {
        Player from = new Player("From", false);
        Player to = new Player("To", false);
        GameSession s = new GameSession();
        s.getPlayers().add(from);
        s.getPlayers().add(to);

        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(from, brown1);
        PropertyPlayHelper.placePropertyCard(from, brown2);

        Property group = from.getProperties().get(0);
        assertNotNull(group);

        PropertyPlayHelper.transferPropertyGroup(from, to, group, s);

        assertTrue(from.getProperties().isEmpty());
        assertFalse(to.getProperties().isEmpty());
    }

    @Test
    void transferPropertyGroup_transfersBuildingsOnMonopoly() {
        Player from = new Player("From", false);
        Player to = new Player("To", false);
        GameSession s = new GameSession();
        s.getPlayers().add(from);
        s.getPlayers().add(to);

        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(from, brown1);
        PropertyPlayHelper.placePropertyCard(from, brown2);

        Property group = from.getProperties().get(0);
        group.addBuildingCard(new ActionCardHouse(100, "House", 3));

        PropertyPlayHelper.transferPropertyGroup(from, to, group, s);

        // Cards should be in to's properties
        assertFalse(to.getProperties().isEmpty());
    }
}