package com.example.monopoly_deal_game.model;

/**
 * 所有手牌/牌堆中的卡牌基类（设计图：抽象 Card）。
 *
 * TODO(model): 定义编号、名称、卡面描述、是否计入「每回合 3 张」限制等；
 * {@link #use(Player, Player)} 可仅作钩子，真正结算在 {@link com.example.monopoly_deal_game.logic.CardEffectExecutor}。
 */
public abstract class Card {
<<<<<<< HEAD
=======
    protected int id;                // 唯一编号
    protected String name;           // 名称
    protected String description;    // 卡面描述
    protected int value;             // 银行面值 (存款价值)
    protected boolean countsTowardLimit = true; // 是否计入「每回合 3 张」限制，默认为 true

    public Card(int id, String name, int value, String description) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
    }
>>>>>>> ec928dc (Initial commit: rename folder and add all files)

    /**
     * @param user   出牌玩家
     * @param target 目标玩家（部分行动牌可为 null）
     */
    public abstract void use(Player user, Player target);
<<<<<<< HEAD
=======

    // Getter & Setter
    public int getValue() { return value; }
    public String getName() { return name; }
    public boolean isCountsTowardLimit() { return countsTowardLimit; }
>>>>>>> ec928dc (Initial commit: rename folder and add all files)
}
