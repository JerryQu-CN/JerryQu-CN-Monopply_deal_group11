/**
 * <h2>人工智能（AI / Bot）</h2>
 * <p>
 * 职责：为 {@link com.example.monopoly_deal_game.model.ComputerPlayer} 生成合法的 {@link com.example.monopoly_deal_game.logic.PlayerCommand}。
 * 不直接操作 JavaFX；可在轮到机器人时由 {@link com.example.monopoly_deal_game.logic.TurnManager} 或独立调度器调用。
 * </p>
 *
 * <h3>需要实现的功能</h3>
 * <ul>
 *   <li>难度分级：随机合法走法 vs 贪心（租、偷、防 Just Say No 等）。</li>
 *   <li>与需求 1.2 一致：机器人数量与总人数上限由逻辑层校验。</li>
 * </ul>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li><b>→ logic</b>：通过 {@link com.example.monopoly_deal_game.logic.GameLogic} 查询合法行动集合（待提供 API）。</li>
 *   <li><b>→ model</b>：只读 {@link com.example.monopoly_deal_game.model.GameSession} 决策。</li>
 * </ul>
 *
 * <h3>TODO</h3>
 * <ul>
 *   <li>实现 {@link com.example.monopoly_deal_game.ai.BotPolicy} 的具体策略类。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.ai;
