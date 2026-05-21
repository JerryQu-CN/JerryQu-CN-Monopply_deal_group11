package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;

import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * 拦截「直指某位玩家的不利影响」时使用 Just Say No。由控制器在初始化时挂载 UI 对话框；
 * AI 玩家在持有 JSN 时按简易策略自动反对；未挂载或非 FX 线程时安全切回 FX 线程再弹窗。
 */
public final class JustSayNoMediator {

    private static volatile JustSayNoUi ui;

    private JustSayNoMediator() {}

    @FunctionalInterface
    public interface JustSayNoUi {
        /** @return 玩家同意消耗一张 JSN（手牌优先，其次银行） */
        boolean confirmUseJustSayNo(Player respondent, Player activator, String situationText);
    }

    public static void installUi(JustSayNoUi bridge) {
        ui = bridge;
    }

    public static void clearUi() {
        ui = null;
    }

    public static JustSayNoUi getUiOrNull() {
        return ui;
    }

    /**
     * 仅当 {@code respondent} 在手牌或银行中持有 JSN 时才询问；同意后该牌移入弃牌堆。
     *
     * @return true 已用 JSN 抵消本条对自身的效果
     */
    public static boolean tryBlockAgainstPlayer(
            Player respondent, Player activator, GameSession session, String situationText) {
        if (respondent == null || session == null) {
            return false;
        }
        if (respondent.equals(activator)) {
            return false;
        }
        Card jsHeld = findJustSayNoRespondentHeld(respondent);
        if (jsHeld == null) {
            return false;
        }
        if (respondent.isAI()) {
            return aiShouldUseJsN(situationText);
        }
        JustSayNoUi bridge = ui;
        if (bridge == null) {
            return false;
        }
        String text = situationText != null ? situationText : "";
        boolean agrees = invokeUiOnFxThread(() -> bridge.confirmUseJustSayNo(respondent, activator, text));
        if (!agrees) {
            return false;
        }
        if (!cardStillWithRespondent(respondent, jsHeld)) {
            return false;
        }
        if (!removeCardFromRespondentZones(respondent, jsHeld)) {
            return false;
        }
        session.discardCard(jsHeld);
        return true;
    }

    /** 简易 AI：被偷物业 / 被破坏整套 / 被指名支付 ≥4M 时较高概率打出反对 */
    private static boolean aiShouldUseJsN(String situationText) {
        if (situationText == null) {
            return false;
        }
        boolean high =
                situationText.contains("夺产")
                        || situationText.contains("偷偷")
                        || situationText.contains("被迫交易")
                        || situationText.contains("$5")
                        || situationText.contains("5M");
        if (high) {
            return Math.random() < 0.55;
        }
        return situationText.contains("生日") ? Math.random() < 0.25 : Math.random() < 0.30;
    }

    private static boolean invokeUiOnFxThread(java.util.function.Supplier<Boolean> run) {
        if (Platform.isFxApplicationThread()) {
            return Boolean.TRUE.equals(run.get());
        }
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] out = new boolean[1];
        Platform.runLater(
                () -> {
                    try {
                        out[0] = Boolean.TRUE.equals(run.get());
                    } finally {
                        latch.countDown();
                    }
                });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
        return out[0];
    }

    private static boolean cardStillWithRespondent(Player respondent, Card c) {
        return respondent.getHand().getCards().contains(c)
                || respondent.getBank().getCards().contains(c);
    }

    private static Card findJustSayNoRespondentHeld(Player p) {
        for (Card c : p.getHand().getCards()) {
            if (isJustSayNo(c)) {
                return c;
            }
        }
        for (Card c : p.getBank().getCards()) {
            if (isJustSayNo(c)) {
                return c;
            }
        }
        return null;
    }

    private static boolean removeCardFromRespondentZones(Player p, Card jsCard) {
        Objects.requireNonNull(p);
        Objects.requireNonNull(jsCard);
        if (p.getHand().removeCard(jsCard)) {
            return true;
        }
        return p.getBank().removeCard(jsCard);
    }

    private static boolean isJustSayNo(Card c) {
        return c instanceof ActionCard ac
                && ac.getActionType() == ActionCard.ActionType.JUST_SAY_NO;
    }
}
