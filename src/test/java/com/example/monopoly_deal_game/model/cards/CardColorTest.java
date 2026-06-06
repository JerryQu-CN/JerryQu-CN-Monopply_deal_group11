package com.example.monopoly_deal_game.model.cards;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CardColorTest {

    // ---- getRent ----

    @Test
    void getRent_brown_1card_returns1() {
        assertEquals(1, CardColor.BROWN.getRent(1));
    }

    @Test
    void getRent_brown_2card_returns2() {
        assertEquals(2, CardColor.BROWN.getRent(2));
    }

    @Test
    void getRent_brown_outOfBounds_returns0() {
        assertEquals(0, CardColor.BROWN.getRent(0));
        assertEquals(0, CardColor.BROWN.getRent(3));
    }

    @Test
    void getRent_blue_1card_returns3() {
        assertEquals(3, CardColor.BLUE.getRent(1));
    }

    @Test
    void getRent_blue_2card_returns8() {
        assertEquals(8, CardColor.BLUE.getRent(2));
    }

    @Test
    void getRent_wild_returns0() {
        assertEquals(0, CardColor.WILD.getRent(1));
    }

    @Test
    void getRent_none_returns0() {
        assertEquals(0, CardColor.NONE.getRent(1));
    }

    // ---- getMaxProperties ----

    @Test
    void getMaxProperties_brown_returns2() {
        assertEquals(2, CardColor.BROWN.getMaxProperties());
    }

    @Test
    void getMaxProperties_black_returns4() {
        assertEquals(4, CardColor.BLACK.getMaxProperties());
    }

    @Test
    void getMaxProperties_none_returns0() {
        assertEquals(0, CardColor.NONE.getMaxProperties());
    }

    // ---- isBuildable ----

    @Test
    void isBuildable_tableColors_areBuildable() {
        for (CardColor c : CardColor.TABLE_COLORS) {
            assertTrue(c.isBuildable(), c.name() + " should be buildable");
        }
    }

    @Test
    void isBuildable_wildAndNone_notBuildable() {
        assertFalse(CardColor.WILD.isBuildable());
        assertFalse(CardColor.NONE.isBuildable());
    }

    // ---- TABLE_COLORS ----

    @Test
    void tableColors_contains10Colors() {
        assertEquals(10, CardColor.TABLE_COLORS.size());
    }

    @Test
    void tableColors_excludesWildAndNone() {
        assertFalse(CardColor.TABLE_COLORS.contains(CardColor.WILD));
        assertFalse(CardColor.TABLE_COLORS.contains(CardColor.NONE));
    }

    // ---- standardColors ----

    @Test
    void standardColors_returns10Colors() {
        List<CardColor> colors = CardColor.standardColors();
        assertEquals(10, colors.size());
        assertFalse(colors.contains(CardColor.WILD));
        assertFalse(colors.contains(CardColor.NONE));
    }

    @Test
    void standardColors_consistentWithTableColors() {
        List<CardColor> standard = CardColor.standardColors();
        Set<CardColor> table = CardColor.TABLE_COLORS;
        assertEquals(table.size(), standard.size());
        for (CardColor c : standard) {
            assertTrue(table.contains(c), c + " should be in TABLE_COLORS");
        }
    }

    // ---- getDisplayName ----

    @Test
    void getDisplayName_brown_notNull() {
        assertNotNull(CardColor.BROWN.getDisplayName());
        assertEquals("Brown", CardColor.BROWN.getDisplayName());
    }

    @Test
    void getDisplayName_noneIsNull() {
        assertNull(CardColor.NONE.getDisplayName());
    }

    // ---- getRent edge cases ----

    @Test
    void getRent_red_3card_returns6() {
        assertEquals(6, CardColor.RED.getRent(3));
    }

    @Test
    void getRent_green_3card_returns7() {
        assertEquals(7, CardColor.GREEN.getRent(3));
    }

    @Test
    void getRent_black_4card_returns4() {
        assertEquals(4, CardColor.BLACK.getRent(4));
    }

    @Test
    void getRent_orange_1card_returns1() {
        assertEquals(1, CardColor.ORANGE.getRent(1));
    }
}