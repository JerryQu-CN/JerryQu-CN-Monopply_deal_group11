package com.example.monopoly_deal_game.logic.payment;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.PropertyPlayHelper;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Bank payment and property collateral: the payer must select assets from the table;
 * payments are never made from the hand nor deducted automatically.
 */
public final class PaymentService {

    private static Function<PaymentRequest, Optional<List<Card>>> manualPaymentPicker;

    private PaymentService() {}

    /** Called by the controller layer to inject the UI payment picker. */
    public static void setManualPaymentPicker(Function<PaymentRequest, Optional<List<Card>>> picker) {
        manualPaymentPicker = picker;
    }

    public static int payFromTo(PaymentRequest req) {
        if (req.amountM() <= 0 || req.payer() == req.receiver() || req.receiver() == null) {
            return 0;
        }
        if (req.payer().isAI()) {
            return autoPayFromTo(req.payer(), req.receiver(), req.amountM(), req.session());
        }
        if (manualPaymentPicker != null) {
            var picked = manualPaymentPicker.apply(req);
            if (picked.isPresent()) {
                List<Card> choice = picked.get();
                if (choice.isEmpty() && totalLiquidityValue(req.payer()) <= 0) {
                    return 0;
                }
                if (isValidManualPaymentChoice(req.payer(), choice, req.amountM())) {
                    return executeTransferChosen(req.payer(), req.receiver(), choice, req.session());
                }
            }
            return 0;
        }
        return autoPayFromTo(req.payer(), req.receiver(), req.amountM(), req.session());
    }

    /** Auto-select cards for payment when automated or no local UI, preferring high-value bank cards first. */
    private static int autoPayFromTo(Player payer, Player receiver, int amountM, GameSession session) {
        int cap = totalLiquidityValue(payer);
        if (cap <= 0) {
            return 0;
        }
        int owed = Math.min(amountM, cap);
        List<Card> selected = new ArrayList<>();
        int collected = 0;

        List<Card> bankCards = new ArrayList<>(payer.getBank().getCards());
        bankCards.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (Card c : bankCards) {
            if (collected >= owed) break;
            selected.add(c);
            collected += Math.max(0, c.getValue());
        }

        if (collected < owed) {
            List<PropertyCard> props = new ArrayList<>();
            for (com.example.monopoly_deal_game.model.Property row : payer.getProperties()) {
                for (PropertyCard pc : row.getCards()) {
                    if (pc.getValue() > 0) {
                        props.add(pc);
                    }
                }
            }
            props.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));
            for (PropertyCard pc : props) {
                if (collected >= owed) break;
                selected.add(pc);
                collected += Math.max(0, pc.getValue());
            }
        }

        if (selected.isEmpty()) {
            return 0;
        }
        return executeTransferChosen(payer, receiver, selected, session);
    }

    private static boolean payerOwnsExactlyThesePayableCards(Player payer, List<Card> choice) {
        for (Card c : choice) {
            if (!isPayableHeldByPlayer(payer, c)) return false;
            if (c.getValue() <= 0) return false;
        }
        return true;
    }

    /** Bank cards or properties on the table that count as cash (hand cards cannot be selected). */
    public static boolean isPayableHeldByPlayer(Player payer, Card c) {
        if (payer == null || c == null) {
            return false;
        }
        if (payer.getBank().getCards().contains(c)) {
            return true;
        }
        return c instanceof PropertyCard pc && findPropertyOwningCard(payer, c) != null;
    }

    private static Property findPropertyOwningCard(Player payer, Card c) {
        if (!(c instanceof PropertyCard pc)) {
            return null;
        }
        for (Property row : payer.getProperties()) {
            if (row.getCards().contains(pc)) {
                return row;
            }
        }
        return null;
    }

    /** UI validation: all selected cards must be in the payable area and their face value must satisfy the debt rule (if insufficient, all available must be surrendered). */
    public static boolean isValidManualPaymentChoice(Player payer, List<Card> choice, int amountM) {
        return choice != null
                && !choice.isEmpty()
                && payer != null
                && payerOwnsExactlyThesePayableCards(payer, choice)
                && selectionCoversDebt(payer, choice, amountM);
    }

    private static boolean selectionCoversDebt(Player payer, List<Card> choice, int amountM) {
        int sumSel = selectionValueSum(choice);
        int cap = totalLiquidityValue(payer);
        int owed = Math.min(amountM, cap);
        boolean payingAllPossible = owed == cap && cap < amountM;
        if (payingAllPossible) {
            return sumSel == cap;
        }
        return sumSel >= amountM;
    }

    /** Total of bank cards plus properties on table that can be used as cash (face value > 0). */
    public static int totalLiquidityValue(Player p) {
        if (p == null) {
            return 0;
        }
        int t = p.getBank().getCards().stream().mapToInt(c -> Math.max(0, c.getValue())).sum();
        for (Property ps : p.getProperties()) {
            for (PropertyCard pc : ps.getCards()) {
                if (pc.getValue() > 0) {
                    t += pc.getValue();
                }
            }
        }
        return t;
    }

    private static int selectionValueSum(List<Card> choice) {
        int s = 0;
        for (Card c : choice) {
            s += Math.max(0, c.getValue());
        }
        return s;
    }

    public static int applyChosenPayment(Player payer, Player receiver, List<Card> sel, GameSession session) {
        if (payer == null || receiver == null || sel == null || session == null) {
            return 0;
        }
        return executeTransferChosen(payer, receiver, resolveSelectedCardsFromPayer(payer, sel), session);
    }

    /** Resolve card objects by their IDs from the payer's bank and properties. */
    public static List<Card> resolveByIds(Player payer, List<Integer> cardIds) {
        List<Card> resolved = new ArrayList<>();
        if (payer == null || cardIds == null) return resolved;
        for (int id : cardIds) {
            Card owned = findPayableCardById(payer, id);
            if (owned != null) resolved.add(owned);
        }
        return resolved;
    }

    private static List<Card> resolveSelectedCardsFromPayer(Player payer, List<Card> selected) {
        List<Card> resolved = new ArrayList<>();
        if (payer == null || selected == null) return resolved;
        for (Card selectedCard : selected) {
            Card owned = findPayableCardById(payer, selectedCard != null ? selectedCard.getId() : -1);
            if (owned != null) resolved.add(owned);
        }
        return resolved;
    }

    private static Card findPayableCardById(Player payer, int id) {
        for (Card c : payer.getBank().getCards()) {
            if (c.getId() == id) return c;
        }
        for (Property row : payer.getProperties()) {
            for (PropertyCard pc : row.getCards()) {
                if (pc.getId() == id) return pc;
            }
        }
        return null;
    }

    private static int executeTransferChosen(Player payer, Player receiver, List<Card> sel, GameSession session) {
        int paidTotal = 0;
        List<Card> copy = new ArrayList<>(sel);
        for (Card c : copy) {
            if (payer.getBank().removeCard(c)) {
                receiver.getBank().addCard(c);
                paidTotal += Math.max(0, c.getValue());
            } else if (c instanceof PropertyCard pc) {
                PropertyPlayHelper.removePropertyCardFromBoard(payer, pc, session);
                PropertyPlayHelper.placePropertyCard(receiver, pc);
                paidTotal += Math.max(0, pc.getValue());
            }
        }
        return paidTotal;
    }

}
