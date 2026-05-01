/**
 * <h2>游戏运行时结构（对齐课设分层：门面 / 常量 / 会话聚合）</h2>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.game.engine.GameEngine} — 门面与对战入口。</li>
 *   <li>{@link com.example.monopoly_deal_game.game.rules.GameConfig} — 人数上下限与规则常量。</li>
 *   <li>{@link com.example.monopoly_deal_game.game.model.GameSession}、{@link com.example.monopoly_deal_game.game.model.GameState} — 一局对局聚合根与进行状态。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.game;
