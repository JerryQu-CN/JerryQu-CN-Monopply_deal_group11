package com.example.monopoly_deal_game.logic;

/**
 * Game rule configuration constants — hand size, draw counts, play limit, and win conditions.
 */
public final class GameConfig {

    // Hand and draw
    public static final int INITIAL_HAND_SIZE = 5;
    public static final int DRAW_WHEN_HAND_NON_EMPTY = 2;
    public static final int DRAW_WHEN_HAND_EMPTY = 5;
    public static final int MAX_HAND_SIZE_END_TURN = 7;
    public static final int MAX_PLAY_PER_TURN = 3;

    // Win conditions
    public static final int FULL_SETS_TO_WIN = 3;

    // Rent rules
    public static final boolean TWO_COLOR_RENT_CHARGES_ALL = true;
    public static final boolean MULTI_COLOR_RENT_CHARGES_ALL = true;

    // Bank rules
    public static final boolean CAN_BANK_ACTION_CARDS = true;
    public static final boolean CAN_BANK_PROPERTY_CARDS = false;

    // Deal Breaker rules
    public static final boolean DEAL_BREAKERS_DISCARD_SETS = false;

    // Double rent rules
    public enum DoubleRentPolicy { MULTIPLY, ADD }
    public static final DoubleRentPolicy DOUBLE_RENT_POLICY = DoubleRentPolicy.MULTIPLY;

    private GameConfig() {}
}