package com.example.monopoly_deal_game.logic;

/**
 * 数值与规则常量（对应设计图 GameConfig，与 GameEngine 组合使用）。
 *
 * TODO(logic): 与官方 Monopoly Deal 牌库数量、各颜色套数一致；课设需求已列出的必须实现：
 * 起手5张、回合开始手牌>0摸2张、=0摸5张、手牌上限7、每回合出牌0–3且Just Say No不计入。
 */
public final class GameConfig {

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 5;
    public static final int MIN_BOTS = 1;
    public static final int MAX_BOTS = 4;

    public static final int INITIAL_HAND_SIZE = 5;
    public static final int DRAW_WHEN_HAND_NON_EMPTY = 2;
    public static final int DRAW_WHEN_HAND_EMPTY = 5;
    public static final int MAX_HAND_SIZE_END_TURN = 7;
    public static final int MAX_PLAY_PER_TURN = 3;

    private GameConfig() {}
}
