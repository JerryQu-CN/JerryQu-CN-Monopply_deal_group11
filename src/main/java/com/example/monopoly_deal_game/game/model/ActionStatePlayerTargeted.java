package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;

/**
 * 简单的「已选定目标」状态：一个玩家被另一个玩家的行动锁定。
 * 被锁定的玩家可以 JSN 拒绝，或接受后状态完成。
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

    @Override
    public void setAccepted(Player player, boolean accepted) {
        if (accepted && onAccepted != null) {
            onAccepted.execute(player);
        }
        super.setAccepted(player, accepted);
    }
}