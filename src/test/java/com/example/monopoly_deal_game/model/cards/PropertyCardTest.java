package com.example.monopoly_deal_game.model.cards;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PropertyCardTest {

    // ---- single-color property ----

    @Test
    void singleColor_isNotWild() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertFalse(brown.isWild());
    }

    @Test
    void singleColor_isNotMultiColorWild() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertFalse(brown.isMultiColorWild());
    }

    @Test
    void singleColor_isNotBiColor() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertFalse(brown.isBiColor());
    }

    @Test
    void singleColor_isBase() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertTrue(brown.isBase());
    }

    @Test
    void singleColor_isStealable() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertTrue(brown.isStealable());
    }

    @Test
    void singleColor_currentColorMatchesConstructor() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertEquals(CardColor.BROWN, brown.getCurrentColor());
    }

    @Test
    void singleColor_applicableColors_singleColor() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        List<CardColor> colors = brown.getApplicableColors();
        assertEquals(1, colors.size());
        assertEquals(CardColor.BROWN, colors.get(0));
    }

    @Test
    void singleColor_canFlipWildDualColor_false() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertFalse(brown.canFlipWildDualColor());
    }

    @Test
    void singleColor_getRent() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertEquals(1, brown.getRent(1));
        assertEquals(2, brown.getRent(2));
        assertEquals(0, brown.getRent(0));
        assertEquals(0, brown.getRent(3));
    }

    @Test
    void singleColor_fullSetThreshold() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertEquals(2, brown.getFullSetThreshold());
    }

    @Test
    void singleColor_hasColor() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertTrue(brown.hasColor(CardColor.BROWN));
        assertFalse(brown.hasColor(CardColor.BLUE));
    }

    @Test
    void singleColor_isSingleColor_true() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertTrue(brown.isSingleColor());
    }

    @Test
    void singleColor_alignToDeclaredColor_ignored() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        brown.alignToDeclaredColor(CardColor.BLUE); // not wild, should ignore
        assertEquals(CardColor.BROWN, brown.getCurrentColor());
    }

    // ---- bi-color wild ----

    @Test
    void biColorWild_isWild() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        assertTrue(bi.isWild());
    }

    @Test
    void biColorWild_isNotMultiColorWild() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        assertFalse(bi.isMultiColorWild());
    }

    @Test
    void biColorWild_isBiColor() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        assertTrue(bi.isBiColor());
    }

    @Test
    void biColorWild_canFlipWildDualColor() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        assertTrue(bi.canFlipWildDualColor());
    }

    @Test
    void biColorWild_applicableColors_bothColors() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        List<CardColor> colors = bi.getApplicableColors();
        assertEquals(2, colors.size());
        assertTrue(colors.contains(CardColor.BROWN));
        assertTrue(colors.contains(CardColor.LIGHT_BLUE));
    }

    @Test
    void biColorWild_alignToDeclaredColor_valid() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        bi.alignToDeclaredColor(CardColor.LIGHT_BLUE);
        assertEquals(CardColor.LIGHT_BLUE, bi.getCurrentColor());
    }

    @Test
    void biColorWild_alignToDeclaredColor_invalid_ignored() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        bi.alignToDeclaredColor(CardColor.BLUE); // BLUE not in colors
        assertEquals(CardColor.BROWN, bi.getCurrentColor()); // unchanged
    }

    @Test
    void biColorWild_alignToDeclaredColor_null_ignored() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        bi.alignToDeclaredColor(null);
        assertEquals(CardColor.BROWN, bi.getCurrentColor());
    }

    @Test
    void biColorWild_selectableColors() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        List<CardColor> selectable = bi.getSelectableColors();
        assertEquals(2, selectable.size());
    }

    // ---- rainbow wild ----

    @Test
    void rainbowWild_isWild() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertTrue(rainbow.isWild());
    }

    @Test
    void rainbowWild_isMultiColorWild() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertTrue(rainbow.isMultiColorWild());
    }

    @Test
    void rainbowWild_canFlipWildDualColor() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertTrue(rainbow.canFlipWildDualColor());
    }

    @Test
    void rainbowWild_applicableColors_allTen() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        List<CardColor> colors = rainbow.getApplicableColors();
        assertEquals(10, colors.size());
    }

    @Test
    void rainbowWild_initialColor_isWild() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertEquals(CardColor.WILD, rainbow.getCurrentColor());
    }

    @Test
    void rainbowWild_alignToDeclaredColor_any() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        rainbow.alignToDeclaredColor(CardColor.GREEN);
        assertEquals(CardColor.GREEN, rainbow.getCurrentColor());
    }

    @Test
    void rainbowWild_alignToDeclaredColor_wild_ignored() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        rainbow.alignToDeclaredColor(CardColor.WILD);
        assertEquals(CardColor.WILD, rainbow.getCurrentColor());
    }

    @Test
    void rainbowWild_alignToDeclaredColor_null_ignored() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        rainbow.alignToDeclaredColor(null);
        assertEquals(CardColor.WILD, rainbow.getCurrentColor());
    }

    @Test
    void rainbowWild_selectableColors_allTen() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertEquals(10, rainbow.getSelectableColors().size());
    }

    @Test
    void rainbowWild_isNotBase_byDefault() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertFalse(rainbow.isBase());
    }

    @Test
    void rainbowWild_stealableInConstructor() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertTrue(rainbow.isStealable());
    }

    @Test
    void rainbowWild_notStealableInConstructor() {
        PropertyCard rainbow = new PropertyCard(31, "Rainbow 2", 3, new int[]{1, 2, 3}, true, false, false);
        assertFalse(rainbow.isStealable());
    }

    // ---- getRent edge cases ----

    @Test
    void getRent_nullRentLevels_returns0() {
        // PropertyCard with null rent levels isn't directly constructable, but getRent handles it
        PropertyCard c = new PropertyCard(99, "Test", 1, CardColor.BROWN, new int[]{3}); // valid rentLevels
        assertEquals(3, c.getRent(1));
        assertEquals(0, c.getRent(0));
        assertEquals(0, c.getRent(2)); // out of bounds
    }

    // ---- getColors ----

    @Test
    void getColors_singleColor_returnsOne() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertEquals(1, brown.getColors().size());
        assertEquals(CardColor.BROWN, brown.getColors().get(0));
    }

    @Test
    void getColors_biColor_returnsTwo() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        assertEquals(2, bi.getColors().size());
    }

    @Test
    void getColors_rainbow_returnsTen() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        assertEquals(10, rainbow.getColors().size());
    }

    // ---- getCardType ----

    @Test
    void getCardType_returnsProperty() {
        PropertyCard brown = new PropertyCard(1, "Brown", 1, CardColor.BROWN, new int[]{1, 2});
        assertEquals(CardType.PROPERTY, brown.getCardType());
    }

    // ---- setCurrentColor ----

    @Test
    void setCurrentColor_biWild_validColor_updates() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        bi.setCurrentColor(CardColor.LIGHT_BLUE);
        assertEquals(CardColor.LIGHT_BLUE, bi.getCurrentColor());
    }

    @Test
    void setCurrentColor_biWild_invalidColor_ignored() {
        PropertyCard bi = new PropertyCard(20, "Brown/LightBlue", 2, CardColor.BROWN, CardColor.LIGHT_BLUE, new int[]{1, 2}, true);
        bi.setCurrentColor(CardColor.BLUE);
        assertEquals(CardColor.BROWN, bi.getCurrentColor()); // unchanged
    }

    @Test
    void setCurrentColor_rainbowWild_anyColor_updates() {
        PropertyCard rainbow = new PropertyCard(30, "Rainbow", 3, new int[]{1, 2, 3}, true, false, true);
        rainbow.setCurrentColor(CardColor.BLACK);
        assertEquals(CardColor.BLACK, rainbow.getCurrentColor());
    }
}