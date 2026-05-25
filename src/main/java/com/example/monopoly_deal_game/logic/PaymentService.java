package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 银行支付与地产作抵押：付款者必须自选桌面资产；不从手牌支付，也不自动扣款。
 */
public final class PaymentService {

    private PaymentService() {}

    public static int payFromTo(Player payer, Player receiver, int amountM, GameSession session) {
        return payFromTo(payer, receiver, amountM, session, null, null);
    }

    public static int payFromTo(
            Player payer,
            Player receiver,
            int amountM,
            GameSession session,
            Player activatingActor,
            String justSayNoSituation) {
        if (amountM <= 0 || payer == receiver || receiver == null) {
            return 0;
        }
        String roomId = AppContext.get().networkLobbyState().getRoomId();
        boolean networked = roomId != null && !roomId.isBlank();
        if (!networked && session != null && activatingActor != null) {
            String text =
                    justSayNoSituation != null && !justSayNoSituation.isBlank()
                            ? justSayNoSituation
                            : "需要你支付 $" + amountM + "M。";
            if (JustSayNoMediator.tryBlockAgainstPlayer(payer, activatingActor, session, text)) {
                return 0;
            }
        }
        if (payer.isAI()) {
            return autoPayFromTo(payer, receiver, amountM, session);
        }
        var ui = PaymentPickerMediator.getUiOrNull();
        if (ui != null) {
            var picked =
                    ui.chooseCardsToPay(
                            payer,
                            amountM,
                            receiver,
                            session,
                            justSayNoSituation != null
                                    ? justSayNoSituation
                                    : "请支付 $" + amountM + "M。");
            if (picked.isPresent()) {
                List<Card> choice = picked.get();
                if (choice.isEmpty() && totalLiquidityValue(payer) <= 0) {
                    return 0;
                }
                if (isValidManualPaymentChoice(payer, choice, amountM)) {
                    return executeTransferChosen(payer, receiver, choice, session);
                }
            }
            return 0;
        }
        return autoPayFromTo(payer, receiver, amountM, session);
    }

    /** AI 或无 UI 时自动选卡支付：银行大额优先，物业补足余款。 */
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
            if (!isPayableHeldByPlayer(payer, c)) {
                return false;
            }
        }
        return true;
    }

    /** 银行或可视为现金的桌面上物业（不能选择手牌）。 */
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

    /** UI 校验：所选牌均属可支付区且面值满足抵债规则（不足则必须全交）。 */
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

    /** 可回收作现金的桌面上物业（含面值>0的单卡）之和 + 银行。 */
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

    public static Player firstOpponent(Player actor, GameSession session) {
        Objects.requireNonNull(session);
        for (Player p : session.getPlayers()) {
            if (p != null && !p.equals(actor)) {
                return p;
            }
        }
        return null;
    }
}
