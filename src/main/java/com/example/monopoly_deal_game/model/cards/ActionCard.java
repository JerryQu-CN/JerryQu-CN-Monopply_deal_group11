package com.example.monopoly_deal_game.model.cards;

/**
 * 行动牌：Deal Breaker、Sly Deal、生日、收债、Just Say No、Pass Go 等。
 *
 * <p>实现细节：
 * <ul>
 *   <li>对应需求 6、9–14。</li>
 *   <li>区分具体行动类型（标签化），供 Logic 层分发效果。</li>
 *   <li>特殊规则适配：Just Say No 不计入每回合 3 张的出牌限制（需求 14）。</li>
 * </ul>
 */
public class ActionCard extends Card {

    /**
     * 行动类型枚举：对应文档中定义的各种功能指令
     */
    public enum ActionType {
        DEAL_BREAKER,    // 需求 9: 抢夺一套完整地产
        FORCE_DEAL,      // 需求 10: 强制交换一张地产
        SLY_DEAL,        // 需求 11: 偷取一张地产
        ITS_MY_BIRTHDAY, // 需求 12: 向全体玩家收取 2M
        DEBT_COLLECTOR,  // 需求 13: 向指定玩家收取 5M
        JUST_SAY_NO,     // 需求 14: 抵挡对方的行动
        PASS_GO,         // 需求 6: 额外抽取 2 张牌
        DOUBLE_RENT,     // 需求 8: 使下一张租金卡金额翻倍
        HOUSE,           // 官方牌组: 置于成套物业上，加租（具体由 logic 结算）
        HOTEL            // 官方牌组: 置于已有房屋的全套上
    }

    private final ActionType actionType;
    private final boolean countsTowardLimit; // 是否计入每回合 3 张的出牌限额

    /**
     * 行动卡构造函数
     *
     * @param id    唯一编号
     * @param name  卡牌名称
     * @param value 银行面值（注意：所有行动卡都可以存入银行）
     * @param type  行动具体类型
     */
    public ActionCard(int id, String name, int value, ActionType type) {
        super(id, name, value, "Action Card: " + name);
        this.actionType = type;
        if (type == ActionType.JUST_SAY_NO) {
            this.countsTowardLimit = false;
        } else {
            this.countsTowardLimit = true;
        }
    }

    @Override
    public CardType getCardType() {
        return CardType.ACTION;
    }

    public ActionType getActionType() {
        return actionType;
    }

    /** 供 TurnManager 调用，判定此卡是否消耗玩家本回合的出牌次数 */
    @Override
    public boolean isCountsTowardLimit() {
        return countsTowardLimit;
    }
}
