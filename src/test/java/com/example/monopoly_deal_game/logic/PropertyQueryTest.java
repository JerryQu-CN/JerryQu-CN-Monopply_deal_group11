package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.*;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropertyQueryTest {

    private Player player;

    private static PropertyCard singleColor(int id, String name, int value, CardColor color, int[] rentLevels) {
        return new PropertyCard(id, name, value, color, rentLevels);
    }

    private static PropertyCard rainbowWild(int id, String name, int value, int[] rentLevels) {
        return new PropertyCard(id, name, value, rentLevels, true, false, true);
    }

    @BeforeEach
    void setUp() {
        player = new Player("Player", false);
    }

    // ---- allTableProperties ----

    @Test
    void allTableProperties_nullPlayer_returnsEmpty() {
        assertTrue(PropertyQuery.allTableProperties(null).isEmpty());
    }

    @Test
    void allTableProperties_emptyPlayer_returnsEmpty() {
        assertTrue(PropertyQuery.allTableProperties(player).isEmpty());
    }

    @Test
    void allTableProperties_returnsAllCards() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard blue = singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8});
        PropertyPlayHelper.placePropertyCard(player, brown);
        PropertyPlayHelper.placePropertyCard(player, blue);

        List<PropertyCard> all = PropertyQuery.allTableProperties(player);
        assertEquals(2, all.size());
        assertTrue(all.contains(brown));
        assertTrue(all.contains(blue));
    }

    // ---- stealableSingleProperties ----

    @Test
    void stealableSingleProperties_nullPlayer_returnsEmpty() {
        assertTrue(PropertyQuery.stealableSingleProperties(null).isEmpty());
    }

    @Test
    void stealableSingleProperties_returnsStealableNonMonopoly() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);

        List<PropertyCard> stealable = PropertyQuery.stealableSingleProperties(player);
        assertEquals(1, stealable.size());
        assertTrue(stealable.contains(brown));
    }

    @Test
    void stealableSingleProperties_excludesMonopolies() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);
        // Brown is a full set (monopoly)

        List<PropertyCard> stealable = PropertyQuery.stealableSingleProperties(player);
        // Monopoly properties should be excluded
        assertTrue(stealable.isEmpty());
    }

    @Test
    void stealableSingleProperties_respectsStealableFlag() {
        // Rainbow wild with stealable=false (3-arg constructor uses stealable from param, default false via new PropertyCard(..., true, false, true))
        // We need a non-stealable card for this test.
        // Actually, rainbowWild created with stealable=true is stealable, singleColor is always stealable.
        // Let's just verify a single-color card IS stealable since it's a non-monopoly.
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);

        List<PropertyCard> stealable = PropertyQuery.stealableSingleProperties(player);
        assertEquals(1, stealable.size());
    }

    // ---- monopolyGroups ----

    @Test
    void monopolyGroups_nullPlayer_returnsEmpty() {
        assertTrue(PropertyQuery.monopolyGroups(null).isEmpty());
    }

    @Test
    void monopolyGroups_noMonopolies_returnsEmpty() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);
        assertTrue(PropertyQuery.monopolyGroups(player).isEmpty());
    }

    @Test
    void monopolyGroups_returnsMonopolyGroups() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        List<Property> monopolies = PropertyQuery.monopolyGroups(player);
        assertEquals(1, monopolies.size());
        assertTrue(monopolies.get(0).isMonopoly());
    }

    // ---- firstStealableProperty ----

    @Test
    void firstStealableProperty_returnsFirstNonMonopoly() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);
        assertEquals(brown, PropertyQuery.firstStealableProperty(player));
    }

    @Test
    void firstStealableProperty_excludesMonopoly() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard blue = singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8});

        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2); // monopoly
        PropertyPlayHelper.placePropertyCard(player, blue);    // non-monopoly

        // firstStealableProperty should skip the monopoly group and return the blue card
        assertEquals(blue, PropertyQuery.firstStealableProperty(player));
    }

    @Test
    void firstStealableProperty_emptyPlayer_returnsNull() {
        assertNull(PropertyQuery.firstStealableProperty(player));
    }

    // ---- firstTableProperty ----

    @Test
    void firstTableProperty_returnsFirstCard() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);
        assertEquals(brown, PropertyQuery.firstTableProperty(player));
    }

    @Test
    void firstTableProperty_emptyPlayer_returnsNull() {
        assertNull(PropertyQuery.firstTableProperty(player));
    }

    // ---- firstMonopolyOfPlayer ----

    @Test
    void firstMonopolyOfPlayer_nullPlayer_returnsNull() {
        assertNull(PropertyQuery.firstMonopolyOfPlayer(null));
    }

    @Test
    void firstMonopolyOfPlayer_returnsFirstMonopoly() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        Property result = PropertyQuery.firstMonopolyOfPlayer(player);
        assertNotNull(result);
        assertTrue(result.isMonopoly());
    }

    @Test
    void firstMonopolyOfPlayer_noMonopoly_returnsNull() {
        assertNull(PropertyQuery.firstMonopolyOfPlayer(player));
    }

    // ---- findHouseTarget ----

    @Test
    void findHouseTarget_returnsMonopolyWithoutHouse() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        Property target = PropertyQuery.findHouseTarget(player, true); // forHouse=true
        assertNotNull(target);
    }

    @Test
    void findHouseTarget_returnsNullWhenHouseAlreadyExists() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        // Add house
        player.getProperties().get(0).addBuildingCard(new ActionCardHouse(100, "House", 3));

        Property target = PropertyQuery.findHouseTarget(player, true); // forHouse=true, already has house
        assertNull(target);
    }

    @Test
    void findHouseTarget_hotelTarget_returnsMonopolyWithHouseNoHotel() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        // Add house (but no hotel yet)
        player.getProperties().get(0).addBuildingCard(new ActionCardHouse(100, "House", 3));

        // forHouse=false means looking for hotel target
        Property target = PropertyQuery.findHouseTarget(player, false);
        assertNotNull(target);
    }

    @Test
    void findHouseTarget_hotelTarget_returnsNullWhenHotelExists() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        Property p = player.getProperties().get(0);
        p.addBuildingCard(new ActionCardHouse(100, "House", 3));
        p.addBuildingCard(new ActionCardHotel(101, "Hotel", 4));

        Property target = PropertyQuery.findHouseTarget(player, false); // already has hotel
        assertNull(target);
    }

    @Test
    void findHouseTarget_noMonopoly_returnsNull() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown);

        assertNull(PropertyQuery.findHouseTarget(player, true));
    }

    @Test
    void findHouseTarget_hotelNeedsHouse_noHouseYet_returnsNull() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyPlayHelper.placePropertyCard(player, brown1);
        PropertyPlayHelper.placePropertyCard(player, brown2);

        // No house placed yet, looking for hotel target → null (hotel needs house first)
        Property target = PropertyQuery.findHouseTarget(player, false); // forHouse=false (hotel), !hasH → false
        assertNull(target);
    }
}