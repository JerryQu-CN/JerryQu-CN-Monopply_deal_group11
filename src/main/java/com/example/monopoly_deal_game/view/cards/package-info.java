/**
 * <h2>卡牌 GUI（与 {@link com.example.monopoly_deal_game.model.cards.Card} 家族平行）</h2>
 * <p>
 * 建议：<b>牌面长什么样、Tooltip、点击反馈</b> 放在本包；<b>从牌堆飞到手牌</b> 等轨迹动画放在
 * {@link com.example.monopoly_deal_game.view.animation}，通过 {@link javafx.scene.Node#setTranslateX(double)} 等
 * 由动效类驱动，避免在 Card 模型里写 JavaFX。
 * </p>
 *
 * <h3>继承关系（实现顺序可参考）</h3>
 * <pre>
 * {@link com.example.monopoly_deal_game.model.cards.Card} (model)
 *     ├── PropertyCard  →  {@link com.example.monopoly_deal_game.view.cards.PropertyCardNode}
 *     ├── ActionCard    →  {@link com.example.monopoly_deal_game.view.cards.ActionCardNode}
 *     └── BankCard      →  {@link com.example.monopoly_deal_game.view.cards.BankCardNode}
 *
 * {@link com.example.monopoly_deal_game.view.cards.AbstractCardNode} (JavaFX Region，所有牌面共性)
 *     ├── PropertyCardNode
 *     ├── ActionCardNode
 *     └── BankCardNode
 * </pre>
 *
 * <p>
 * 工厂：{@link com.example.monopoly_deal_game.view.cards.CardNodeFactory#from(com.example.monopoly_deal_game.model.cards.Card)}，
 * Controller 或 {@link com.example.monopoly_deal_game.view.GameplayViewCoordinator} 只依赖 {@link com.example.monopoly_deal_game.model.cards.Card}，
 * 不直接 new 具体 Node 子类。
 * </p>
 */
package com.example.monopoly_deal_game.view.cards;
