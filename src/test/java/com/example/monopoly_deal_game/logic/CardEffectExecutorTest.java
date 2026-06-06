package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.ActionState;
import com.example.monopoly_deal_game.game.state.ActionStateRent;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.*;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardEffectExecutorTest {

    private CardEffectExecutor executor;
    private GameSession session;
    private Player player;
    private Player opponent;

    private static PropertyCard singleColor(int id, String name, int value, CardColor color, int[] rentLevels) {
        return new PropertyCard(id, name, value, color, rentLevels);
    }

    @BeforeEach
    void setUp() {
        executor = new CardEffectExecutor(new CardManager());
        player = new Player("Player", false);
        opponent = new Player("Opponent", false);
        session = new GameSession();
        session.getPlayers().add(player);
        session.getPlayers().add(opponent);
        session.getGameState().setCurrentPlayerIndex(0);
    }

    // ---- execute: null currentPlayer throws ----

    @Test
    void execute_nullCurrentPlayer_throws() {
        session.getGameState().setCurrentPlayerIndex(-1); // no current player
        BankCard c = new BankCard(1, "1M", 1);
        assertThrows(IllegalStateException.class, () -> executor.execute(c, session));
    }

    // ---- execute: BankCard ----

    @Test
    void execute_bankCard_addsToBank() {
        BankCard c = new BankCard(1, "1M", 1);
        executor.execute(c, session);
        assertTrue(player.getBank().getCards().contains(c));
    }

    // ---- execute: PropertyCard ----

    @Test
    void execute_propertyCard_createsGroup() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        executor.execute(brown, session);
        assertFalse(player.getProperties().isEmpty());
        assertEquals(CardColor.BROWN, player.getProperties().get(0).getEffectiveColor());
    }

    @Test
    void execute_propertyCard_mergesIntoCompatibleGroup() {
        PropertyCard brown1 = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        executor.execute(brown1, session);
        executor.execute(brown2, session);
        // Both should be in the same group
        assertEquals(1, player.getProperties().size());
        assertEquals(2, player.getProperties().get(0).getCards().size());
    }

    // ---- execute: RuleCard ----

    @Test
    void execute_ruleCard_discards() {
        RuleCard rc = new RuleCard(100, "Rule", "Rule card description");
        executor.execute(rc, session);
        assertTrue(session.getDiscardPile().contains(rc));
    }

    // ---- execute: RentCard ----

    @Test
    void execute_rentCard_discardsRentCard() {
        // Give landlord a property so rent has a value
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        com.example.monopoly_deal_game.model.Property prop = new com.example.monopoly_deal_game.model.Property();
        prop.addCard(brown);
        player.addProperty(prop);

        RentCard rc = new RentCard(50, 1, List.of(CardColor.BROWN), false);
        executor.execute(rc, session);
        assertTrue(session.getDiscardPile().contains(rc));
    }

    @Test
    void execute_rentCard_singleColor_addsActionState() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        com.example.monopoly_deal_game.model.Property prop = new com.example.monopoly_deal_game.model.Property();
        prop.addCard(brown);
        player.addProperty(prop);

        RentCard rc = new RentCard(50, 1, List.of(CardColor.BROWN), false);
        executor.execute(rc, session, CardPlayOptions.auto().withActionTarget(opponent));
        // Should have added an action state for rent
        assertNotNull(session.getGameState().getActionState());
    }

    @Test
    void execute_rentCard_noProperties_noRent() {
        RentCard rc = new RentCard(50, 1, List.of(CardColor.BROWN), false);
        executor.execute(rc, session);
        // No rent to collect, no action state added (only turnState remains)
        assertNull(session.getGameState().getActionState());
    }

    // ---- execute: wild rent chargesAll ----

    @Test
    void execute_wildRent_addsActionStateForAll_chargesAll() {
        // Give landlord a blue property
        PropertyCard blue = singleColor(10, "Blue 1", 4, CardColor.BLUE, new int[]{3, 8});
        com.example.monopoly_deal_game.model.Property prop = new com.example.monopoly_deal_game.model.Property();
        prop.addCard(blue);
        player.addProperty(prop);

        RentCard rc = new RentCard(51, 3, List.of(), true);
        executor.execute(rc, session);
        // Wild rent charges all by default, so action state should be added
        assertNotNull(session.getGameState().getActionState());
    }

    // ---- execute: dual-color rent chargesAll ----

    @Test
    void execute_dualColorRent_addsActionStateForAll_chargesAll() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        com.example.monopoly_deal_game.model.Property prop = new com.example.monopoly_deal_game.model.Property();
        prop.addCard(brown);
        player.addProperty(prop);

        RentCard rc = new RentCard(52, 1, List.of(CardColor.BROWN, CardColor.LIGHT_BLUE), false);
        executor.execute(rc, session);
        // Dual-color rent charges all, action state should be added
        assertNotNull(session.getGameState().getActionState());
    }

    // ---- execute: null opt handled ----

    @Test
    void execute_nullOpt_defaultsToAuto() {
        BankCard c = new BankCard(1, "1M", 1);
        executor.execute(c, session, null);
        assertTrue(player.getBank().getCards().contains(c));
    }
}