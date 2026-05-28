package com.example.monopoly_deal_game.game.rules;

/**
 * 游戏规则配置常量，对齐 Monopoly-Deal-main 中 GameRules 的默认值。
 * 后续可扩展为从文件加载可配置规则。
 */
public final class GameConfig {

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 5;

    // 手牌与抽牌
    public static final int INITIAL_HAND_SIZE = 5;
    public static final int DRAW_WHEN_HAND_NON_EMPTY = 2;
    public static final int DRAW_WHEN_HAND_EMPTY = 5;
    public static final int MAX_HAND_SIZE_END_TURN = 7;
    public static final int MAX_PLAY_PER_TURN = 3;

    // 获胜条件
    public static final int FULL_SETS_TO_WIN = 3;
    public static final boolean UNIQUE_COLORS_REQUIRED = true;

    // 租金规则
    public static final boolean TWO_COLOR_RENT_CHARGES_ALL = true;
    public static final boolean MULTI_COLOR_RENT_CHARGES_ALL = true;
    public static final boolean ALLOW_MULTIPLE_RENT_MODIFIERS = false;

    // 银行规则
    public static final boolean CAN_BANK_ACTION_CARDS = true;
    public static final boolean CAN_BANK_PROPERTY_CARDS = false;
    public static final boolean IS_BANK_VALUE_VISIBLE = false;

    // Deal Breaker 规则
    public static final boolean DEAL_BREAKERS_DISCARD_SETS = false;

    // 撤销规则
    public static final boolean ALLOW_UNDO = true;

    // 弃牌规则
    public static final boolean CAN_DISCARD_EARLY = false;

    // 双倍租金规则
    public enum DoubleRentPolicy { MULTIPLY, ADD }
    public static final DoubleRentPolicy DOUBLE_RENT_POLICY = DoubleRentPolicy.MULTIPLY;

    private GameConfig() {}
}