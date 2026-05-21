package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;

import java.util.List;
import java.util.Optional;

/**
 * 真人玩家交付租金/赔款时自选银行牌与桌面上物业（展示型支付）；机器人仍走 {@link PaymentService} 内部自动抵扣。
 */
public final class PaymentPickerMediator {

    @FunctionalInterface
    public interface Ui {
        /**
         * @return 选中的牌实例（每张须属于 payer 的银行或可支付的桌面物业）；空表示放弃本次自选（逻辑层将改用自动抵扣）。
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
