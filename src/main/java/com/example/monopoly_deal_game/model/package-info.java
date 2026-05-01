/**
 * <h2>模型层（Model / MVC中的 M）</h2>
 * <p>
 * 职责：描述玩家、物业集合等<strong>纯数据与领域对象</strong>，不包含 JavaFX、不包含 Socket。
 * 卡牌族见 {@link com.example.monopoly_deal_game.model.cards}；一局对参见 {@link com.example.monopoly_deal_game.game.model}。
 * </p>
 *
 * <h3>需要实现的功能（对照需求）</h3>
 * <ul>
 *   <li><b>初始化发牌（需求 1.4–1.5）</b>：每人 5 张起手；剩余为抽牌堆；弃牌堆洗牌回补（数据结构在 {@link com.example.monopoly_deal_game.game.model.GameSession} 或 {@code CardManager} 持有的引用）。</li>
 *   <li><b>物业与银行展示数据（需求 4–5、17）</b>：{@link com.example.monopoly_deal_game.model.Player} 内含手牌、银行区、物业区；支持「双色系」「万能物业」当前颜色字段。</li>
 *   <li><b>胜负状态（需求 16）</b>：{@link com.example.monopoly_deal_game.game.model.GameState} 记录当前玩家、回合、是否进行中；完整套统计在会话对象中。</li>
 * </ul>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li><b>← logic</b>：{@link com.example.monopoly_deal_game.logic.GameLogic}、{@link com.example.monopoly_deal_game.logic.CardManager} 读写本会话数据。</li>
 *   <li><b>← controller</b>：只读视图绑定（通过引擎暴露只读 API 或 DTO，避免 UI 直接改模型）。</li>
 *   <li><b>← persistence</b>：序列化/反序列化 {@link com.example.monopoly_deal_game.game.model.GameSession}。</li>
 *   <li><b>← network</b>：主机广播的状态快照应能还原为 {@link com.example.monopoly_deal_game.game.model.GameSession}。</li>
 * </ul>
 *
 * <h3>TODO（在哪写）</h3>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.model.cards.Card} 体系：属性牌/钞票/行动牌字段与 {@code use} 默认实现策略（具体效果在 logic 层执行器）。</li>
 *   <li>{@link com.example.monopoly_deal_game.model.Player}：手牌上限、银行面值列表、按颜色分组的物业。</li>
 *   <li>{@link com.example.monopoly_deal_game.game.model.GameSession}：玩家列表2–5、抽/弃牌堆引用、当前回合索引。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.model;
