/**
 * <h2>视图层（View / MVC中的 V）</h2>
 * <p>
 * 职责：与「如何画出来」相关——自定义 JavaFX 控件、观察者接口、将来可拆 {@code PlayerView}、{@code BankView}、{@code PropertyView}等。
 * 当前课设中大量布局仍在 {@code resources/.../*.fxml}，本包先放接口与复用控件，避免 controller 膨胀。
 * </p>
 *
 * <h3>需要实现的功能（需求 17）</h3>
 * <ul>
 *   <li>卡牌 Tooltip：悬停/点击显示名称、功能、面值、颜色、租金档位。</li>
 *   <li>三区布局：对手缩略 + 中央手牌 + 本人银行/物业详情。</li>
 *   <li>每位玩家手牌<strong>张数</strong>展示（非窥视对手具体牌）。</li>
 * </ul>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li><b>← model / logic</b>：通过 {@link com.example.monopoly_deal_game.view.GameObserver} 或 JavaFX {@code Property} 绑定只读视图模型。</li>
 *   <li><b>→ controller</b>：FXML 中 {@code fx:controller} 仍在 {@link com.example.monopoly_deal_game.controller}。</li>
 * </ul>
 *
 * <h3>TODO</h3>
 * <ul>
 *   <li>实现 {@link com.example.monopoly_deal_game.view.GameObserver} 的具体订阅者（或改用 JavaFX Bean）。</li>
 *   <li>拆分 {@code GameplayScreen} 子控件类到本包。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.view;
