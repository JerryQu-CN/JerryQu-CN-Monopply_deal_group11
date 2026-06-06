package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.GameConfig;
import com.example.monopoly_deal_game.logic.payment.RentRules;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.*;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RentRulesTest {

    private Player landlord;
    private Player opponent1;
    private Player opponent2;
    private GameSession session;

    private static PropertyCard singleColor(int id, String name, int value, CardColor color, int[] rentLevels) {
        return new PropertyCard(id, name, value, color, rentLevels);
    }

    private void addPropertyToPlayer(Player p, CardColor color, PropertyCard... cards) {
        Property prop = new Property();
        for (PropertyCard c : cards) prop.addCard(c);
        prop.setOwner(p);
        p.addProperty(prop);
    }

    @BeforeEach
    void setUp() {
        landlord = new Player("Landlord", false);
        opponent1 = new Player("Opponent1", false);
        opponent2 = new Player("Opponent2", false);
        session = new GameSession();
        session.getPlayers().add(landlord);
        session.getPlayers().add(opponent1);
        session.getPlayers().add(opponent2);
    }

    // ---- eligibleChargeColors ----

    @Test
    void eligibleChargeColors_nullRentCard_returnsEmpty() {
        assertTrue(RentRules.eligibleChargeColors(null, landlord).isEmpty());
    }

    @Test
    void eligibleChargeColors_nullPlayer_returnsEmpty() {
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertTrue(RentRules.eligibleChargeColors(rc, null).isEmpty());
    }

    @Test
    void eligibleChargeColors_singleColor_returnsColor() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        List<CardColor> colors = RentRules.eligibleChargeColors(rc, landlord);
        assertEquals(List.of(CardColor.BROWN), colors);
    }

    @Test
    void eligibleChargeColors_dualColor_returnsBoth() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN, CardColor.LIGHT_BLUE), false);
        List<CardColor> colors = RentRules.eligibleChargeColors(rc, landlord);
        assertTrue(colors.contains(CardColor.BROWN));
        assertTrue(colors.contains(CardColor.LIGHT_BLUE));
    }

    @Test
    void eligibleChargeColors_wildRent_returnsAllColorsWithProperties() {
        addPropertyToPlayer(landlord, CardColor.BLUE,
                singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8}));
        RentCard rc = new RentCard(1, 3, List.of(), true);
        List<CardColor> colors = RentRules.eligibleChargeColors(rc, landlord);
        // Blue has rent > 0, so it should be included
        assertTrue(colors.contains(CardColor.BLUE));
    }

    @Test
    void eligibleChargeColors_nonWildRent_returnsCardColorsRegardlessOfProperties() {
        // For non-wild rent, eligibleChargeColors returns the card's declared colors
        // regardless of whether the player owns matching properties.
        // The actual rent validity check happens in canUseRentEffect.
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertEquals(List.of(CardColor.BROWN), RentRules.eligibleChargeColors(rc, landlord));
    }

    @Test
    void eligibleChargeColors_wildRent_noProperties_returnsEmpty() {
        RentCard rc = new RentCard(1, 3, List.of(), true);
        assertTrue(RentRules.eligibleChargeColors(rc, landlord).isEmpty());
    }

    // ---- canUseRentEffect (3-param) ----

    @Test
    void canUseRentEffect_validColor_returnsTrue() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertTrue(RentRules.canUseRentEffect(rc, landlord, CardColor.BROWN));
    }

    @Test
    void canUseRentEffect_wrongColor_returnsFalse() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertFalse(RentRules.canUseRentEffect(rc, landlord, CardColor.BLUE));
    }

    @Test
    void canUseRentEffect_noProperties_returnsFalse() {
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertFalse(RentRules.canUseRentEffect(rc, landlord, CardColor.BROWN));
    }

    // ---- canUseRentEffect (5-param with chargesAll) ----

    @Test
    void canUseRentEffect_5param_dualColor_chargesAll_returnsTrue() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN, CardColor.LIGHT_BLUE), false);
        assertTrue(RentRules.canUseRentEffect(rc, landlord, null, null, session));
    }

    @Test
    void canUseRentEffect_5param_wildRent_chargesAll_returnsTrue() {
        addPropertyToPlayer(landlord, CardColor.BLUE,
                singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8}));
        RentCard rc = new RentCard(1, 3, List.of(), true);
        assertTrue(RentRules.canUseRentEffect(rc, landlord, null, null, session));
    }

    @Test
    void canUseRentEffect_5param_singleColor_noTarget_returnsFalse_forNonAi() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertFalse(RentRules.canUseRentEffect(rc, landlord, null, null, session));
    }

    @Test
    void canUseRentEffect_5param_specificTarget_returnsTrue() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertTrue(RentRules.canUseRentEffect(rc, landlord, CardColor.BROWN, opponent1, session));
    }

    @Test
    void canUseRentEffect_5param_nullSession_returnsFalse() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertFalse(RentRules.canUseRentEffect(rc, landlord, CardColor.BROWN, opponent1, null));
    }

    @Test
    void canUseRentEffect_5param_noOtherPlayers_returnsFalse() {
        GameSession solo = new GameSession();
        solo.getPlayers().add(landlord);
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN, CardColor.LIGHT_BLUE), false);
        assertFalse(RentRules.canUseRentEffect(rc, landlord, null, null, solo));
    }

    // ---- canUseRentEffectForUi ----

    @Test
    void canUseRentEffectForUi_valid_returnsTrue() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertTrue(RentRules.canUseRentEffectForUi(rc, landlord, CardColor.BROWN, session));
    }

    @Test
    void canUseRentEffectForUi_noProperties_returnsFalse() {
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertFalse(RentRules.canUseRentEffectForUi(rc, landlord, CardColor.BROWN, session));
    }

    @Test
    void canUseRentEffectForUi_nullSession_returnsFalse() {
        addPropertyToPlayer(landlord, CardColor.BROWN,
                singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2}));
        RentCard rc = new RentCard(1, 1, List.of(CardColor.BROWN), false);
        assertFalse(RentRules.canUseRentEffectForUi(rc, landlord, CardColor.BROWN, null));
    }

    // ---- rentersExcludingLandlord ----

    @Test
    void rentersExcludingLandlord_returnsOthers() {
        List<Player> renters = RentRules.rentersExcludingLandlord(landlord, session);
        assertEquals(2, renters.size());
        assertTrue(renters.contains(opponent1));
        assertTrue(renters.contains(opponent2));
        assertFalse(renters.contains(landlord));
    }

    @Test
    void rentersExcludingLandlord_nullLandlord_returnsEmpty() {
        assertTrue(RentRules.rentersExcludingLandlord(null, session).isEmpty());
    }

    @Test
    void rentersExcludingLandlord_nullSession_returnsEmpty() {
        assertTrue(RentRules.rentersExcludingLandlord(landlord, null).isEmpty());
    }

    // ---- resolvedRentPayer ----

    @Test
    void resolvedRentPayer_withValidTarget_returnsTarget() {
        CardPlayOptions opt = CardPlayOptions.auto().withActionTarget(opponent1);
        assertEquals(opponent1, RentRules.resolvedRentPayer(landlord, session, opt));
    }

    @Test
    void resolvedRentPayer_AIPlayer_noTarget_returnsFirstOpponent() {
        landlord.setAI(true);
        assertEquals(opponent1, RentRules.resolvedRentPayer(landlord, session, null));
    }

    @Test
    void resolvedRentPayer_nonAI_noTarget_returnsNull() {
        assertNull(RentRules.resolvedRentPayer(landlord, session, null));
    }

    @Test
    void resolvedRentPayer_noRenters_returnsNull() {
        GameSession solo = new GameSession();
        solo.getPlayers().add(landlord);
        assertNull(RentRules.resolvedRentPayer(landlord, solo, null));
    }

    @Test
    void resolvedRentPayer_optWithNullTarget_returnsNull_forNonAi() {
        CardPlayOptions opt = CardPlayOptions.auto(); // no target set
        assertNull(RentRules.resolvedRentPayer(landlord, session, opt));
    }
}