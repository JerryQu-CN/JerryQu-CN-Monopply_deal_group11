package com.example.monopoly_deal_game.game.rules;

/**
 * Game rule configuration constants, aligned with the default values in GameRules from Monopoly-Deal-main.
 * Can later be extended to load configurable rules from a file.
 */
public final class GameConfig {

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 5;

    // Hand and draw
    public static final int INITIAL_HAND_SIZE = 5;
    public static final int DRAW_WHEN_HAND_NON_EMPTY = 2;
    public static final int DRAW_WHEN_HAND_EMPTY = 5;
    public static final int MAX_HAND_SIZE_END_TURN = 7;
    public static final int MAX_PLAY_PER_TURN = 3;

    // Win conditions
    public static final int FULL_SETS_TO_WIN = 3;
    public static final boolean UNIQUE_COLORS_REQUIRED = true;

    // Rent rules
    public static final boolean TWO_COLOR_RENT_CHARGES_ALL = true;
    public static final boolean MULTI_COLOR_RENT_CHARGES_ALL = true;
    public static final boolean ALLOW_MULTIPLE_RENT_MODIFIERS = false;

    // Bank rules
    public static final boolean CAN_BANK_ACTION_CARDS = true;
    public static final boolean CAN_BANK_PROPERTY_CARDS = false;
    public static final boolean IS_BANK_VALUE_VISIBLE = false;

    // Deal Breaker rules
    public static final boolean DEAL_BREAKERS_DISCARD_SETS = false;

    // Undo rules
    public static final boolean ALLOW_UNDO = true;

    // Discard rules
    public static final boolean CAN_DISCARD_EARLY = false;

    // Double rent rules
    public enum DoubleRentPolicy { MULTIPLY, ADD }
    public static final DoubleRentPolicy DOUBLE_RENT_POLICY = DoubleRentPolicy.MULTIPLY;

    private GameConfig() {}
}