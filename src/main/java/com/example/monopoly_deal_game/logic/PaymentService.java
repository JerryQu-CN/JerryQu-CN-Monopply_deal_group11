package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 银行支付与地产作抵押：真人可自选卡牌；机器人或关闭 UI 时使用自动大额优先抵扣。
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
        if (session != null && activatingActor != null) {
            String text =
                    justSayNoSituation != null && !justSayNoSituation.isBlank()
                            ? justSayNoSituation
                            : "需要你支付 $" + amountM + "M。";
            if (JustSayNoMediator.tryBlockAgainstPlayer(payer, activatingActor, session, text)) {
                return 0;
            }
        }
        if (!payer.isAI()) {
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
                    if (isValidManualPaymentChoice(payer, choice, amountM)) {
                        return executeTransferChosen(payer, receiver, choice, session);
                    }
                }
                // 选人取消或校验失败：降级自动抵扣
            }
        }
        return automatedPayRemainder(payer, receiver, amountM, session);
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
    static boolean isPayableHeldByPlayer(Player payer, Card c) {
        if (payer == null || c == null) {
            return false;
        }
        if (payer.getBank().getCards().contains(c)) {
            return true;
        }
        return findPropertyOwningCard(payer, c) != null;
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

    private static int executeTransferChosen(Player payer, Player receiver, List<Card> sel, GameSession session) {
        int paidTotal = 0;
        List<Card> copy = new ArrayList<>(sel);
        for (Card c : copy) {
            if (payer.getBank().removeCard(c)) {
                receiver.getBank().addCard(c);
                paidTotal += Math.max(0, c.getValue());
            } else if (c instanceof PropertyCard pc) {
                PropertyPlayHelper.removePropertyCardFromBoard(payer, pc, session);
                receiver.getBank().addCard(pc);
                paidTotal += Math.max(0, pc.getValue());
            }
        }
        return paidTotal;
    }

    private static int automatedPayRemainder(Player payer, Player receiver, int amountM, GameSession session) {
        int remaining = amountM;
        int paidTotal = 0;

        List<Card> bankSnapshot = new ArrayList<>(payer.getBank().getCards());
        bankSnapshot.sort(Comparator.comparingInt(Card::getValue).reversed());
        for (Card c : bankSnapshot) {
            if (remaining <= 0) {
                break;
            }
            if (payer.getBank().removeCard(c)) {
                receiver.getBank().addCard(c);
                int v = Math.max(0, c.getValue());
                remaining -= v;
                paidTotal += v;
            }
        }

        if (remaining <= 0) {
            return paidTotal;
        }

        List<PropertyCard> tableProps = new ArrayList<>();
        for (Property ps : payer.getProperties()) {
            for (PropertyCard pc : ps.getCards()) {
                tableProps.add(pc);
            }
        }
        tableProps.sort(Comparator.comparingInt(PropertyCard::getValue));
        for (PropertyCard pc : tableProps) {
            if (remaining <= 0) {
                break;
            }
            if (pc.getValue() <= 0) {
                continue;
            }
            PropertyPlayHelper.removePropertyCardFromBoard(payer, pc, session);
            receiver.getBank().addCard(pc);
            remaining -= pc.getValue();
            paidTotal += pc.getValue();
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
