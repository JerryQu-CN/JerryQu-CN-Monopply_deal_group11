package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.logic.payment.RentCalculator;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.*;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RentCalculatorTest {

    private Player player;

    // --- helpers ---

    private static PropertyCard singleColor(int id, String name, int value, CardColor color, int[] rentLevels) {
        return new PropertyCard(id, name, value, color, rentLevels);
    }

    private static PropertyCard rainbowWild(int id, String name, int value, int[] rentLevels) {
        return new PropertyCard(id, name, value, rentLevels, true, false, true);
    }

    private Property makeProperty(CardColor color, PropertyCard... cards) {
        Property p = new Property();
        for (PropertyCard c : cards) p.addCard(c);
        p.setOwner(player);
        // If a rainbow wild's currentColor is WILD, align it
        for (PropertyCard c : cards) {
            if (c.isMultiColorWild() && c.getCurrentColor() == CardColor.WILD) {
                c.alignToDeclaredColor(color);
            }
        }
        return p;
    }

    @BeforeEach
    void setUp() {
        player = new Player("TestPlayer", false);
    }

    // ---- rentOnColor: no properties ----

    @Test
    void rentOnColor_noProperties_returnsZero() {
        assertEquals(0, RentCalculator.rentOnColor(player, CardColor.BROWN));
    }

    @Test
    void rentOnColor_hasOtherColor_returnsZero() {
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2})));
        assertEquals(0, RentCalculator.rentOnColor(player, CardColor.BLUE));
    }

    // ---- rentOnColor: single property ----

    @Test
    void rentOnColor_singleBrown_returns1() {
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2})));
        assertEquals(1, RentCalculator.rentOnColor(player, CardColor.BROWN));
    }

    @Test
    void rentOnColor_singleBlue_returns3() {
        player.getProperties().add(makeProperty(CardColor.BLUE,
                singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8})));
        assertEquals(3, RentCalculator.rentOnColor(player, CardColor.BLUE));
    }

    // ---- rentOnColor: full set ----

    @Test
    void rentOnColor_fullSetBrown_returns2() {
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}),
                singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2})));
        assertEquals(2, RentCalculator.rentOnColor(player, CardColor.BROWN));
    }

    @Test
    void rentOnColor_fullSetBlue_returns8() {
        player.getProperties().add(makeProperty(CardColor.BLUE,
                singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8}),
                singleColor(11, "Blue 2", 4, CardColor.BLUE, new int[]{3, 8})));
        assertEquals(8, RentCalculator.rentOnColor(player, CardColor.BLUE));
    }

    @Test
    void rentOnColor_fullSetWithRainbow_returnsFullRent() {
        PropertyCard blue1 = singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8});
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BLUE);
        player.getProperties().add(makeProperty(CardColor.BLUE, blue1, rainbow));
        // 2 cards in blue set → rent = 8
        assertEquals(8, RentCalculator.rentOnColor(player, CardColor.BLUE));
    }

    // ---- rentOnColor: with buildings ----

    @Test
    void rentOnColor_fullSetWithHouse_adds3() {
        Property p = makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}),
                singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2}));
        p.addBuildingCard(new ActionCardHouse(100, "House", 3));
        player.getProperties().add(p);
        // base rent for full brown = 2, house bonus = 3 → 5
        assertEquals(5, RentCalculator.rentOnColor(player, CardColor.BROWN));
    }

    @Test
    void rentOnColor_fullSetWithHouseAndHotel_adds7() {
        Property p = makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}),
                singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2}));
        p.addBuildingCard(new ActionCardHouse(100, "House", 3));
        p.addBuildingCard(new ActionCardHotel(101, "Hotel", 4));
        player.getProperties().add(p);
        // base rent = 2, bonus = 3+4 = 7 → 9
        assertEquals(9, RentCalculator.rentOnColor(player, CardColor.BROWN));
    }

    // ---- rentOnColor: multiple sets of same color ----

    @Test
    void rentOnColor_multipleSets_picksBest() {
        // Set 1: single brown → rent 1
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2})));
        // Set 2: full brown → rent 2
        Property fullBrown = makeProperty(CardColor.BROWN,
                singleColor(3, "Brown 3", 1, CardColor.BROWN, new int[]{1, 2}),
                singleColor(4, "Brown 4", 1, CardColor.BROWN, new int[]{1, 2}));
        fullBrown.addBuildingCard(new ActionCardHouse(100, "House", 3));
        player.getProperties().add(fullBrown);
        // best = full set with house = 2 + 3 = 5
        assertEquals(5, RentCalculator.rentOnColor(player, CardColor.BROWN));
    }

    // ---- bestRentForLandlord ----

    @Test
    void bestRentForLandlord_singleColor() {
        player.getProperties().add(makeProperty(CardColor.RED,
                singleColor(40, "Red 1", 3, CardColor.RED, new int[]{2, 3, 6}),
                singleColor(41, "Red 2", 3, CardColor.RED, new int[]{2, 3, 6})));
        int best = RentCalculator.bestRentForLandlord(player, List.of(CardColor.RED), false);
        // red full set (2 cards) = 3
        assertEquals(3, best);
    }

    @Test
    void bestRentForLandlord_multipleColors_picksBest() {
        // Brown single → 1
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2})));
        // Blue full → 8
        player.getProperties().add(makeProperty(CardColor.BLUE,
                singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8}),
                singleColor(11, "Blue 2", 4, CardColor.BLUE, new int[]{3, 8})));
        int best = RentCalculator.bestRentForLandlord(player, List.of(CardColor.BROWN, CardColor.BLUE), false);
        assertEquals(8, best);
    }

    @Test
    void bestRentForLandlord_doubleRent() {
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}),
                singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2})));
        // full brown = 2, doubled = 4
        int best = RentCalculator.bestRentForLandlord(player, List.of(CardColor.BROWN), true);
        assertEquals(4, best);
    }

    @Test
    void bestRentForLandlord_noMatchingColor_fallsBackToAllColors() {
        player.getProperties().add(makeProperty(CardColor.BLUE,
                singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8})));
        // Pass BROWN but only BLUE exists — falls back to all colors, finds BLUE=3
        int best = RentCalculator.bestRentForLandlord(player, List.of(CardColor.BROWN), false);
        assertEquals(3, best);
    }

    @Test
    void bestRentForLandlord_emptyProperties_returnsZero() {
        int best = RentCalculator.bestRentForLandlord(player, List.of(CardColor.BROWN, CardColor.BLUE), false);
        assertEquals(0, best);
    }

    // ---- bestRentWild ----

    @Test
    void bestRentWild_allColors_picksBest() {
        // Brown single → 1
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2})));
        // Blue full → 8
        player.getProperties().add(makeProperty(CardColor.BLUE,
                singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8}),
                singleColor(11, "Blue 2", 4, CardColor.BLUE, new int[]{3, 8})));
        assertEquals(8, RentCalculator.bestRentWild(player, false));
    }

    @Test
    void bestRentWild_doubleRent() {
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}),
                singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2})));
        // full brown = 2, doubled = 4
        assertEquals(4, RentCalculator.bestRentWild(player, true));
    }

    @Test
    void bestRentWild_noProperties_returnsZero() {
        assertEquals(0, RentCalculator.bestRentWild(player, false));
    }

    // ---- rentOnColor with rent card levels (non-base card has its own rent tiers) ----

    @Test
    void rentOnColor_usesNonWildBaseForRentTier() {
        // A set with a rainbow wild + a blue card should use the blue card's rent tier
        PropertyCard blue1 = singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8});
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3, 4}); // rainbow has 4 tiers
        rainbow.setCurrentColor(CardColor.BLUE);
        player.getProperties().add(makeProperty(CardColor.BLUE, blue1, rainbow));
        // 2 cards, blue card rentLevels[1] = 8
        assertEquals(8, RentCalculator.rentOnColor(player, CardColor.BLUE));
    }

    // ---- bestRentForLandlord: dirty input ----

    @Test
    void bestRentForLandlord_nullColors_tolerated() {
        player.getProperties().add(makeProperty(CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2})));
        // null/NONE/WILD entries in the list are skipped
        int best = RentCalculator.bestRentForLandlord(player, java.util.Arrays.asList(CardColor.BROWN, null, CardColor.NONE, CardColor.WILD), false);
        assertEquals(1, best);
    }
}