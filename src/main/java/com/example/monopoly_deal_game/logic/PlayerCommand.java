package com.example.monopoly_deal_game.logic;

/**
 * UI或网络层传入的「玩家意图」（便于联机序列化）。
 *
 * TODO(logic+network): 扩展为 record序列：PlayCard、EndTurn、PayRent、ChooseJustSayNo、DiscardCards 等。
 */
public sealed interface PlayerCommand permits PlayerCommand.Placeholder {

    record Placeholder() implements PlayerCommand {}
}
