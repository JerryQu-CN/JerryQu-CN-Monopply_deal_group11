package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;

/**
 * 简单的「已选定目标」状态：一个玩家被另一个玩家的行动锁定。
 * 被锁定的玩家可以 JSN 拒绝，或接受后状态完成。
 * <p>
 * setAccepted 只负责状态变更，不触发副作用。
 * 调用方应在 setAccepted 之后显式调用 {@link #executeOnAccepted(Player)} 来执行业务逻辑。
 */
public class ActionStatePlayerTargeted extends ActionState {
    @Serial
    private static final long serialVersionUID = 1L;

    private SerializablePlayerAction onAccepted;

    public ActionStatePlayerTargeted(Player actionOwner, Player actionTarget, String status) {
        super(actionOwner, actionTarget, status);
    }

    public ActionStatePlayerTargeted(Player actionOwner, Player actionTarget) {
        super(actionOwner, actionTarget);
    }

    public void setOnAccepted(SerializablePlayerAction action) {
        this.onAccepted = action;
    }

    public boolean hasOnAccepted() {
        return onAccepted != null;
    }

    /** 执行接受后的业务逻辑（支付、财产转移等），每个目标只应调用一次。 */
    public void executeOnAccepted(Player player) {
        if (onAccepted != null) {
            onAccepted.execute(player);
            onAccepted = null;
        }
    }
}