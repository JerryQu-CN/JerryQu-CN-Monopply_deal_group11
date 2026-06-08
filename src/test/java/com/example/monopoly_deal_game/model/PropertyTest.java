package com.example.monopoly_deal_game.model;

import com.example.monopoly_deal_game.model.cards.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropertyTest {

    private Property property;

    // --- helpers ---

    private static PropertyCard singleColor(int id, String name, int value, CardColor color, int[] rentLevels) {
        return new PropertyCard(id, name, value, color, rentLevels);
    }

    private static PropertyCard biColorWild(int id, String name, int value, CardColor c1, CardColor c2, int[] rentLevels) {
        return new PropertyCard(id, name, value, c1, c2, rentLevels);
    }

    private static PropertyCard rainbowWild(int id, String name, int value, int[] rentLevels) {
        return new PropertyCard(id, name, value, rentLevels, false, true);
    }

    // Brown single cards — full set at 2
    private static PropertyCard brown1() { return singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}); }
    private static PropertyCard brown2() { return singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2}); }

    // Blue single cards — full set at 2
    private static PropertyCard blue1() { return singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8}); }
    private static PropertyCard blue2() { return singleColor(11, "Blue 2", 4, CardColor.BLUE, new int[]{3, 8}); }

    @BeforeEach
    void setUp() {
        property = new Property();
    }

    // ---- empty property ----

    @Test
    void emptyProperty_hasNoEffectiveColor() {
        assertEquals(CardColor.NONE, property.getEffectiveColor());
    }

    @Test
    void emptyProperty_isNotMonopoly() {
        assertFalse(property.isMonopoly());
    }

    @Test
    void emptyProperty_hasNoSingleColor() {
        assertFalse(property.hasSingleColorProperty());
    }

    // ---- add / remove ----

    @Test
    void addCard_increasesCardCount() {
        property.addCard(brown1());
        assertEquals(1, property.getCards().size());
    }

    @Test
    void addNull_doesNothing() {
        property.addCard(null);
        assertTrue(property.getCards().isEmpty());
    }

    @Test
    void removeCard_returnsTrueAndDecreases() {
        PropertyCard c = brown1();
        property.addCard(c);
        assertTrue(property.removeCard(c));
        assertTrue(property.getCards().isEmpty());
    }

    @Test
    void removeNonExistentCard_returnsFalse() {
        property.addCard(brown1());
        assertFalse(property.removeCard(brown2()));
        assertEquals(1, property.getCards().size());
    }

    // ---- totalValue ----

    // ---- accepts ----

    @Test
    void accepts_anyCard_whenEmpty() {
        assertTrue(property.accepts(brown1()));
        assertTrue(property.accepts(blue1()));
        assertTrue(property.accepts(rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3})));
    }

    @Test
    void accepts_matchingColor() {
        property.addCard(brown1());
        assertTrue(property.accepts(brown2()));
    }

    @Test
    void rejects_nonMatchingColor() {
        property.addCard(brown1());
        assertFalse(property.accepts(blue1()));
    }

    @Test
    void rejects_whenFullSet() {
        property.addCard(brown1());
        property.addCard(brown2());
        PropertyCard brown3 = singleColor(3, "Brown 3", 1, CardColor.BROWN, new int[]{1, 2});
        assertFalse(property.accepts(brown3));
    }

    @Test
    void accepts_biColorWild_whenAnchorMatches() {
        property.addCard(brown1());
        PropertyCard bi = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        assertTrue(property.accepts(bi));
    }

    @Test
    void accepts_rainbowWild_whenAnchorColorSet() {
        property.addCard(brown1());
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        assertTrue(property.accepts(rainbow));
    }

    @Test
    void accepts_rainbowWild_whenOnlyRainbowWildsExist_withDeclaredColor() {
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BLUE);
        property.addCard(rainbow);
        // After adding a rainbow wild with declared color, anchor is BLUE
        assertTrue(property.accepts(blue1()));
    }

    @Test
    void accepts_null_returnsFalse() {
        assertFalse(property.accepts(null));
    }

    // ---- hasSingleColorProperty (bug-fix area) ----

    @Test
    void hasSingleColorProperty_singlePlainCard() {
        property.addCard(brown1());
        assertTrue(property.hasSingleColorProperty());
    }

    @Test
    void hasSingleColorProperty_twoMatchingPlainCards() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertTrue(property.hasSingleColorProperty());
    }

    @Test
    void hasSingleColorProperty_mixedColors_returnsFalse() {
        property.addCard(brown1());
        property.addCard(blue1());
        assertFalse(property.hasSingleColorProperty());
    }

    @Test
    void hasSingleColorProperty_rainbowWildWithDeclaredColor_returnsFalse() {
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.alignToDeclaredColor(CardColor.BROWN);
        property.addCard(rainbow);
        assertFalse(property.hasSingleColorProperty());
    }

    @Test
    void hasSingleColorProperty_rainbowWildWithoutDeclaredColor_returnsFalse() {
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        // currentColor is WILD by default
        property.addCard(rainbow);
        assertFalse(property.hasSingleColorProperty());
    }

    @Test
    void hasSingleColorProperty_biColorWildMatchingPlainAnchor() {
        property.addCard(brown1());
        PropertyCard bi = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        property.addCard(bi);
        assertTrue(property.hasSingleColorProperty());
    }

    @Test
    void hasSingleColorProperty_biColorWildNonMatchingAnchor() {
        property.addCard(blue1());
        PropertyCard bi = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        property.addCard(bi);
        assertFalse(property.hasSingleColorProperty());
    }

    @Test
    void hasSingleColorProperty_plainPlusMatchingRainbow() {
        property.addCard(brown1());
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BROWN);
        property.addCard(rainbow);
        assertTrue(property.hasSingleColorProperty());
    }

    // ---- getEffectiveColor ----

    @Test
    void getEffectiveColor_plainCard() {
        property.addCard(brown1());
        assertEquals(CardColor.BROWN, property.getEffectiveColor());
    }

    @Test
    void getEffectiveColor_prefersNonWildPlainOverRainbow() {
        // Plain blue card + rainbow wild aligned to blue → effective should be the plain BLUE
        property.addCard(blue1());
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BLUE);
        property.addCard(rainbow);
        assertEquals(CardColor.BLUE, property.getEffectiveColor());
    }

    @Test
    void getEffectiveColor_biColorWildsOnly_usesFirstCurrentColor() {
        PropertyCard bi1 = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        PropertyCard bi2 = biColorWild(21, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        bi2.alignToDeclaredColor(CardColor.LIGHT_BLUE);
        property.addCard(bi1);  // currentColor = BROWN
        property.addCard(bi2);  // currentColor = LIGHT_BLUE
        // First bi-color wild's currentColor is BROWN (not shifted by anchor logic)
        assertEquals(CardColor.BROWN, property.getEffectiveColor());
    }

    @Test
    void getEffectiveColor_rainbowWildOnly_withDeclaredColor_returnsNONE() {
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.GREEN);
        property.addCard(rainbow);
        assertEquals(CardColor.NONE, property.getEffectiveColor());
    }

    @Test
    void getEffectiveColor_rainbowWildOnly_withoutDeclaredColor_returnsNONE() {
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        // default currentColor is WILD
        property.addCard(rainbow);
        assertEquals(CardColor.NONE, property.getEffectiveColor());
    }

    @Test
    void getEffectiveColor_plainPlusRainbow_returnsPlainColor() {
        property.addCard(blue1());
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BLUE);
        property.addCard(rainbow);
        assertEquals(CardColor.BLUE, property.getEffectiveColor());
    }

    // ---- isMonopoly ----

    @Test
    void isMonopoly_fullSet_brown() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertTrue(property.isMonopoly());
    }

    @Test
    void isMonopoly_incompleteSet() {
        property.addCard(brown1());
        assertFalse(property.isMonopoly());
    }

    @Test
    void isMonopoly_blueWithRainbow() {
        property.addCard(blue1());
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BLUE);
        property.addCard(rainbow);
        // Blue full set needs 2, we have 2 cards
        assertTrue(property.isMonopoly());
    }

    @Test
    void isMonopoly_rainbowWildAlone_notMonopoly() {
        PropertyCard rainbow = rainbowWild(30, "Rainbow", 3, new int[]{1, 2, 3});
        rainbow.setCurrentColor(CardColor.BLUE);
        property.addCard(rainbow);
        // requiredSetSize for rainbow alone = Integer.MAX_VALUE
        assertFalse(property.isMonopoly());
    }

    @Test
    void isMonopoly_blackFullSet_requires4() {
        PropertyCard black1 = singleColor(50, "Black 1", 1, CardColor.BLACK, new int[]{1, 2, 3, 4});
        PropertyCard black2 = singleColor(51, "Black 2", 1, CardColor.BLACK, new int[]{1, 2, 3, 4});
        PropertyCard black3 = singleColor(52, "Black 3", 1, CardColor.BLACK, new int[]{1, 2, 3, 4});
        property.addCard(black1);
        property.addCard(black2);
        assertFalse(property.isMonopoly());
        property.addCard(black3);
        assertFalse(property.isMonopoly());
        PropertyCard black4 = singleColor(53, "Black 4", 1, CardColor.BLACK, new int[]{1, 2, 3, 4});
        property.addCard(black4);
        assertTrue(property.isMonopoly());
    }

    // ---- buildings ----

    @Test
    void addBuildingCard_houseOnMonopoly_succeeds() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertTrue(property.addBuildingCard(new ActionCardHouse(100, "House", 3)));
        assertEquals(1, property.getBuildingCards().size());
    }

    @Test
    void addBuildingCard_rejectedWhenNotMonopoly() {
        property.addCard(brown1());
        assertFalse(property.addBuildingCard(new ActionCardHouse(100, "House", 3)));
        assertTrue(property.getBuildingCards().isEmpty());
    }

    @Test
    void addBuildingCard_hotelOnHouse_succeeds() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertTrue(property.addBuildingCard(new ActionCardHouse(100, "House", 3)));
        assertTrue(property.addBuildingCard(new ActionCardHotel(101, "Hotel", 4)));
        assertEquals(2, property.getBuildingCards().size());
    }

    @Test
    void addBuildingCard_hotelWithoutHouse_rejected() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertFalse(property.addBuildingCard(new ActionCardHotel(101, "Hotel", 4)));
        assertTrue(property.getBuildingCards().isEmpty());
    }

    @Test
    void addBuildingCard_secondHouse_rejected() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertTrue(property.addBuildingCard(new ActionCardHouse(100, "House", 3)));
        assertFalse(property.addBuildingCard(new ActionCardHouse(102, "House 2", 3)));
        assertEquals(1, property.getBuildingCards().size());
    }

    @Test
    void addBuildingCard_null_rejected() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertFalse(property.addBuildingCard(null));
    }

    @Test
    void addBuildingCard_nonBuildingCard_rejected() {
        property.addCard(brown1());
        property.addCard(brown2());
        assertFalse(property.addBuildingCard(brown1()));
    }

    // ---- buildingRentBonus ----

    @Test
    void buildingRentBonus_noBuildings_isZero() {
        assertEquals(0, property.getBuildingRentBonus());
    }

    @Test
    void buildingRentBonus_houseAdds3() {
        property.addCard(brown1());
        property.addCard(brown2());
        property.addBuildingCard(new ActionCardHouse(100, "House", 3));
        assertEquals(3, property.getBuildingRentBonus());
    }

    @Test
    void buildingRentBonus_hotelAdds4() {
        property.addCard(brown1());
        property.addCard(brown2());
        property.addBuildingCard(new ActionCardHouse(100, "House", 3));
        property.addBuildingCard(new ActionCardHotel(101, "Hotel", 4));
        assertEquals(7, property.getBuildingRentBonus());
    }

    // ---- takeAllBuildings ----

    @Test
    void takeAllBuildings_clearsAndReturns() {
        property.addCard(brown1());
        property.addCard(brown2());
        property.addBuildingCard(new ActionCardHouse(100, "House", 3));
        property.addBuildingCard(new ActionCardHotel(101, "Hotel", 4));
        List<Card> taken = property.takeAllBuildings();
        assertEquals(2, taken.size());
        assertTrue(property.getBuildingCards().isEmpty());
    }

    // ---- owner ----

    @Test
    void setOwner_updatesOwner() {
        Player p = new Player("Test", false);
        property.setOwner(p);
        assertEquals(p, property.getOwner());
    }

    @Test
    void getCards_returnsUnmodifiable() {
        property.addCard(brown1());
        List<PropertyCard> cards = property.getCards();
        assertThrows(UnsupportedOperationException.class, () -> cards.add(brown2()));
    }

    @Test
    void getBuildingCards_returnsUnmodifiable() {
        property.addCard(brown1());
        property.addCard(brown2());
        property.addBuildingCard(new ActionCardHouse(100, "House", 3));
        List<Card> buildings = property.getBuildingCards();
        assertThrows(UnsupportedOperationException.class, () -> buildings.add(new ActionCardHouse(102, "H", 3)));
    }

    // ---- hasSingleColorProperty edge from bug-fix: bi-color wild shifts anchor ----

    @Test
    void hasSingleColorProperty_biWildAnchorShift() {
        // Bi-color wild (BROWN/LIGHT_BLUE) plus a plain LIGHT_BLUE card.
        // In the game, PropertyPlayHelper.alignWildToRow shifts the bi-color
        // to LIGHT_BLUE before adding. Simulate that here manually.
        PropertyCard bi = biColorWild(20, "Brown/LightBlue Wild", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2});
        property.addCard(bi);
        assertTrue(property.hasSingleColorProperty()); // bi wild alone with declared BROWN

        PropertyCard lb = singleColor(60, "Light Blue 1", 1, CardColor.LIGHT_BLUE, new int[]{1, 2, 3});
        bi.alignToDeclaredColor(CardColor.LIGHT_BLUE); // shift before adding (mirrors alignWildToRow)
        property.addCard(lb);
        assertTrue(property.hasSingleColorProperty());
        assertEquals(CardColor.LIGHT_BLUE, property.getEffectiveColor());
    }
}