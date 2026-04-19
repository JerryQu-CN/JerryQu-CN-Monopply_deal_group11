/**
 * <h2>游戏逻辑层（Game Logic）</h2>
 * <p>
 * 职责：实现需求文档中所有<strong>规则与流程</strong>——发牌、抽牌、出牌、回合结束、弃牌、行动牌效果、租金与 Just Say No 链、胜利判定。
 * 对应架构图核心：{@link com.example.monopoly_deal_game.logic.GameEngine} 依赖 {@link com.example.monopoly_deal_game.logic.GameLogic}；
 * {@link com.example.monopoly_deal_game.network.GameServer} 在主机侧也应调用同一套逻辑以保证权威状态。
 * </p>
 *
 * <h3>类与需求映射（方便分工）</h3>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.logic.GameConfig} — 常量：人数上下限、起手5张、回合摸2/5张、手牌上限7、每回合出牌≤3等。</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.GameLogic} — {@code initGame}（需求1）、{@code drawCard}（需求2）、{@code playCard}（需求3–6）、{@code endTurn}（需求3）、{@code checkGameOver}（需求16）。</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.TurnManager} — 当前玩家切换、出牌计数、Just Say No 是否占用额度（需求3.2）。</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.CardManager} — 抽牌堆/弃牌堆、抽空时洗弃牌（需求1.5、2.4）。</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.CardFactory} — 生成完整牌库（与实体卡牌列表一致）。</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.CardEffectExecutor} — Rent/DealBreaker/…/JustSayNo 链（需求8–15）。</li>
 *   <li>{@link com.example.monopoly_deal_game.logic.GameEngine} — {@code launchGame}、{@code startLocalGame}、{@code startLanGame} 入口，组装上面对象。</li>
 * </ul>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li><b>→ model</b>：只修改 {@link com.example.monopoly_deal_game.model.GameSession} 内数据，不直接操作 JavaFX。</li>
 *   <li><b>← controller</b>：控制器只调 {@link com.example.monopoly_deal_game.logic.GameEngine} 的用例方法。</li>
 *   <li><b>← network</b>：客户端发 {@link com.example.monopoly_deal_game.logic.PlayerCommand}，主机执行同一逻辑。</li>
 *   <li><b>← ai</b>：机器人通过策略生成合法 {@link com.example.monopoly_deal_game.logic.PlayerCommand}。</li>
 * </ul>
 *
 * <h3>TODO（总体）</h3>
 * <ul>
 *   <li>先打通「本地双人对战」闭环，再接入联机同步。</li>
 *   <li>为关键规则编写单元测试（尤其摸牌张数、弃牌、Just Say No 链）。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.logic;
