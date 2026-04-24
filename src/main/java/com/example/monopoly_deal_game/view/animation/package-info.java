/**
 * <h2>界面动效（与牌面类解耦）</h2>
 * <p>
 * 建议：动效只操作 {@link javafx.scene.Node} 的几何与透明度，不修改 {@link com.example.monopoly_deal_game.model.GameSession}；
 * 动画结束后再由 controller 调 logic 提交「动画已播完」后的状态（若需要与规则同步）。
 * </p>
 *
 * <h3>继承与分工</h3>
 * <pre>
 * {@link com.example.monopoly_deal_game.view.animation.UiMotion} — 统一入口 {@link #play(MotionContext, Runnable)}
 *     ↑
 * {@link com.example.monopoly_deal_game.view.animation.AbstractUiMotion} — 时长、拼接 {@link javafx.animation.Animation} 的模板
 *     ├── {@link com.example.monopoly_deal_game.view.animation.ShuffleMotion}   — 牌堆洗牌（乱序仅视觉；真实顺序仍以 logic 为准）
 *     ├── {@link com.example.monopoly_deal_game.view.animation.DrawMotion}     — 从牌堆到手牌
 *     ├── {@link com.example.monopoly_deal_game.view.animation.PlaceMotion}    — 落到桌面/物业区/银行条
 *     └── {@link com.example.monopoly_deal_game.view.animation.MoveMotion}     — 已有 Node 在父容器内平移/换父（抢牌、归还等）
 * </pre>
 *
 * <p>
 * 需要组合多种动效时，在调用方使用 {@link javafx.animation.SequentialTransition} /
 * {@link javafx.animation.ParallelTransition}，或在本包增加小的编排类（保持各类仍独立可测）。
 * </p>
 */
package com.example.monopoly_deal_game.view.animation;
