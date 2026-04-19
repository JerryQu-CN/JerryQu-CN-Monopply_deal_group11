/**
 * <h2>控制器层（Controller / MVC中的 C）</h2>
 * <p>
 * 职责：承接 FXML 按钮/输入事件，调用 {@link com.example.monopoly_deal_game.logic.GameEngine}
 * 或 {@link com.example.monopoly_deal_game.logic.GameLogic} 暴露的 API；<strong>不写具体结算规则</strong>。
 * 设计图中 {@code LobbyController}、{@code GameController} 与本包中各 {@code *ScreenController} 对应：
 * 大厅流程由 {@code StartScreen}、{@code AddPlayerScreen} 等共同完成，对局由 {@code GameplayScreen}（待绑定）完成。
 * </p>
 *
 * <h3>需要实现的功能（对照需求文档）</h3>
 * <ul>
 *   <li><b>初始化（需求 1）</b>：座位2–5、人机数量、出牌顺序选择；把选项交给 {@code GameLogic.initGame(...)}。</li>
 *   <li><b>回合与手牌（需求 2–3）</b>：开始回合补牌、0–3 张出牌、结束回合、>7 张弃牌流程 —— 全部通过调用逻辑层完成。</li>
 *   <li><b>支付租金与 Just Say No（需求 7、14）</b>：弹出选择界面，收集用户选择后构造“意图对象”交给逻辑层。</li>
 *   <li><b>UI 交互（需求 17）</b>：悬停/点击查看卡牌说明；展示各玩家手牌张数、银行与物业缩略图。</li>
 * </ul>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li><b>→ logic</b>：唯一允许修改对局状态（通过引擎/API）的地方。</li>
 *   <li><b>→ network</b>：联机时在 {@code SyncController}（待建）中与 {@link com.example.monopoly_deal_game.network.NetworkClient} 同步。</li>
 *   <li><b>→ view</b>：复杂控件可拆到自定义控件；通过 {@link com.example.monopoly_deal_game.view.GameObserver} 订阅模型刷新（待接）。</li>
 *   <li><b>→ persistence</b>：读档界面调用 {@link com.example.monopoly_deal_game.persistence.SaveGameService}。</li>
 * </ul>
 *
 * <h3>TODO（在哪写）</h3>
 * <ul>
 *   <li>各 {@code *ScreenController}：大厅/读档流程与逻辑层对接。</li>
 *   <li>新增 {@code GameplayController}（建议）：绑定 {@code GameplayScreen.fxml}，对接 {@code GameEngine}。</li>
 *   <li>新增 {@code SyncController}（建议）：联机状态与本地 UI 对齐。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.controller;
