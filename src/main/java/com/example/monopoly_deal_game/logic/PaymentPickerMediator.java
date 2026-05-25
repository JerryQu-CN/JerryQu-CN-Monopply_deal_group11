package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.List;
import java.util.Optional;

/**
 * 支付请求桥：本地热座时直接弹付款者选择窗口；联机时可由 UI 桥把请求转发给付款者客户端。
 */
public final class PaymentPickerMediator {

    @FunctionalInterface
    public interface Ui {
        /**
         * @return 付款者选中的桌面资产；Optional.empty 表示当前端无法处理或玩家取消。
         */
        Optional<List<Card>> chooseCardsToPay(
                Player payer, int amountDueM, Player receiver, GameSession session, String reasonText);
    }

    private static volatile Ui ui;

    private PaymentPickerMediator() {}

    public static void installUi(Ui bridge) {
        ui = bridge;
    }

    public static void clearUi() {
        ui = null;
    }

    public static Ui getUiOrNull() {
        return ui;
    }
}
