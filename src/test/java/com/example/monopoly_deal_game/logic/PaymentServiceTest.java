package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.payment.PaymentService;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.*;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class   PaymentServiceTest {

    private Player payer;
    private Player receiver;
    private GameSession session;

    private static PropertyCard singleColor(int id, String name, int value, CardColor color, int[] rentLevels) {
        return new PropertyCard(id, name, value, color, rentLevels);
    }

    @BeforeEach
    void setUp() {
        payer = new Player("Payer", false);
        receiver = new Player("Receiver", false);
        session = new GameSession();
        session.getPlayers().add(payer);
        session.getPlayers().add(receiver);
    }

    // ---- totalLiquidityValue ----

    @Test
    void totalLiquidityValue_nullPlayer_returnsZero() {
        assertEquals(0, PaymentService.totalLiquidityValue(null));
    }

    @Test
    void totalLiquidityValue_emptyPlayer_returnsZero() {
        assertEquals(0, PaymentService.totalLiquidityValue(payer));
    }

    @Test
    void totalLiquidityValue_bankCardsOnly() {
        payer.getBank().addCard(new BankCard(1, "1M", 1));
        payer.getBank().addCard(new BankCard(2, "3M", 3));
        assertEquals(4, PaymentService.totalLiquidityValue(payer));
    }

    @Test
    void totalLiquidityValue_propertiesOnly() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        Property p = new Property();
        p.addCard(brown);
        payer.addProperty(p);
        assertEquals(1, PaymentService.totalLiquidityValue(payer));
    }

    @Test
    void totalLiquidityValue_bankAndProperties() {
        payer.getBank().addCard(new BankCard(1, "1M", 1));
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        PropertyCard brown2 = singleColor(2, "Brown 2", 1, CardColor.BROWN, new int[]{1, 2});
        Property p = new Property();
        p.addCard(brown);
        p.addCard(brown2);
        payer.addProperty(p);
        assertEquals(3, PaymentService.totalLiquidityValue(payer));
    }

    // ---- isPayableHeldByPlayer ----

    @Test
    void isPayableHeldByPlayer_nullPlayer_returnsFalse() {
        BankCard c = new BankCard(1, "1M", 1);
        assertFalse(PaymentService.isPayableHeldByPlayer(null, c));
    }

    @Test
    void isPayableHeldByPlayer_nullCard_returnsFalse() {
        assertFalse(PaymentService.isPayableHeldByPlayer(payer, null));
    }

    @Test
    void isPayableHeldByPlayer_bankCard_returnsTrue() {
        BankCard c = new BankCard(1, "1M", 1);
        payer.getBank().addCard(c);
        assertTrue(PaymentService.isPayableHeldByPlayer(payer, c));
    }

    @Test
    void isPayableHeldByPlayer_propertyCard_returnsTrue() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        Property p = new Property();
        p.addCard(brown);
        payer.addProperty(p);
        assertTrue(PaymentService.isPayableHeldByPlayer(payer, brown));
    }

    @Test
    void isPayableHeldByPlayer_cardNotOwned_returnsFalse() {
        BankCard c = new BankCard(1, "1M", 1);
        assertFalse(PaymentService.isPayableHeldByPlayer(payer, c));
    }

    // ---- isValidManualPaymentChoice ----

    @Test
    void isValidManualPaymentChoice_nullChoice_returnsFalse() {
        assertFalse(PaymentService.isValidManualPaymentChoice(payer, null, 1));
    }

    @Test
    void isValidManualPaymentChoice_emptyChoice_returnsFalse() {
        assertFalse(PaymentService.isValidManualPaymentChoice(payer, List.of(), 1));
    }

    @Test
    void isValidManualPaymentChoice_nullPayer_returnsFalse() {
        BankCard c = new BankCard(1, "1M", 1);
        assertFalse(PaymentService.isValidManualPaymentChoice(null, List.of(c), 1));
    }

    @Test
    void isValidManualPaymentChoice_sufficientPayment_returnsTrue() {
        BankCard c = new BankCard(1, "3M", 3);
        payer.getBank().addCard(c);
        assertTrue(PaymentService.isValidManualPaymentChoice(payer, List.of(c), 2));
    }

    @Test
    void isValidManualPaymentChoice_onlyHas1M_owes3M_mustSurrenderAll() {
        // Payer has 1M total, owes 3M — must surrender everything (1M), so valid
        BankCard c = new BankCard(1, "1M", 1);
        payer.getBank().addCard(c);
        assertTrue(PaymentService.isValidManualPaymentChoice(payer, List.of(c), 3));
    }

    @Test
    void isValidManualPaymentChoice_notAllSelected_whenInsufficient_returnsFalse() {
        // Payer has 3M total (two cards), owes 5M, but only selects 1M card — invalid
        BankCard c1 = new BankCard(1, "1M", 1);
        BankCard c2 = new BankCard(2, "2M", 2);
        payer.getBank().addCard(c1);
        payer.getBank().addCard(c2);
        assertFalse(PaymentService.isValidManualPaymentChoice(payer, List.of(c1), 5));
    }

    @Test
    void isValidManualPaymentChoice_overpayAllowed_returnsTrue() {
        BankCard c = new BankCard(1, "5M", 5);
        payer.getBank().addCard(c);
        assertTrue(PaymentService.isValidManualPaymentChoice(payer, List.of(c), 3));
    }

    @Test
    void isValidManualPaymentChoice_cardNotOwned_returnsFalse() {
        BankCard c = new BankCard(1, "5M", 5);
        // card not in payer's bank
        assertFalse(PaymentService.isValidManualPaymentChoice(payer, List.of(c), 3));
    }

    @Test
    void isValidManualPaymentChoice_insufficientTotal_butAllSelected_returnsTrue() {
        // Payer has 2M total, owes 5M — must surrender all 2M
        BankCard c = new BankCard(1, "2M", 2);
        payer.getBank().addCard(c);
        assertTrue(PaymentService.isValidManualPaymentChoice(payer, List.of(c), 5));
    }

    // ---- applyChosenPayment ----

    @Test
    void applyChosenPayment_nullPayer_returnsZero() {
        assertEquals(0, PaymentService.applyChosenPayment(null, receiver, List.of(), session));
    }

    @Test
    void applyChosenPayment_nullReceiver_returnsZero() {
        assertEquals(0, PaymentService.applyChosenPayment(payer, null, List.of(), session));
    }

    @Test
    void applyChosenPayment_nullSelection_returnsZero() {
        assertEquals(0, PaymentService.applyChosenPayment(payer, receiver, null, session));
    }

    @Test
    void applyChosenPayment_nullSession_returnsZero() {
        BankCard c = new BankCard(1, "3M", 3);
        payer.getBank().addCard(c);
        assertEquals(0, PaymentService.applyChosenPayment(payer, receiver, List.of(c), null));
    }

    @Test
    void applyChosenPayment_transfersBankCard() {
        BankCard c = new BankCard(1, "3M", 3);
        payer.getBank().addCard(c);
        int result = PaymentService.applyChosenPayment(payer, receiver, List.of(c), session);
        assertEquals(3, result);
        assertFalse(payer.getBank().getCards().contains(c));
        assertTrue(receiver.getBank().getCards().contains(c));
    }

    @Test
    void applyChosenPayment_transfersPropertyCard() {
        PropertyCard brown = singleColor(1, "Brown 1", 1, CardColor.BROWN, new int[]{1, 2});
        Property p = new Property();
        p.addCard(brown);
        payer.addProperty(p);
        int result = PaymentService.applyChosenPayment(payer, receiver, List.of(brown), session);
        assertEquals(1, result);
        assertTrue(payer.getProperties().isEmpty() || payer.getProperties().get(0).getCards().isEmpty());
    }
}
