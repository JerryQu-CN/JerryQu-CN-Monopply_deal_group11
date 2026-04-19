/**
 * <h2>应用入口层（App）</h2>
 * <p>
 * 职责：启动 JavaFX、创建主 {@link javafx.stage.Stage}、加载首屏 FXML。
 * </p>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li>仅依赖 {@link com.example.monopoly_deal_game.controller} 中的 {@code ScreenNavigation} 做首屏加载；不编写游戏规则。</li>
 *   <li>全局异常处理、图标、窗口大小策略可在此包扩展。</li>
 * </ul>
 *
 * <h3>TODO（在哪写）</h3>
 * <ul>
 *   <li>{@code MonopolyDealApplication}：启动参数、默认分辨率、异常兜底。</li>
 *   <li>{@code Launcher}：{@code main}入口；课设提交时与 JavaFX 运行配置一致即可。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.app;
