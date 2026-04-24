/**
 * <h2>「场景」在 GUI 上的划分（与 FXML 一屏一文件对应）</h2>
 * <p>
 * 大厅、加人、读档等已是独立 FXML + {@link com.example.monopoly_deal_game.controller}；
 * 本包只描述<b>对局内</b>图层与职责，方便把动画、弹窗挂到正确 {@link javafx.scene.layout.Pane}。
 * </p>
 *
 * <h3>推荐分层（从底到顶）</h3>
 * <ol>
 *   <li>{@link com.example.monopoly_deal_game.view.scene.UiSceneLayer#TABLE} — 牌桌、对手区、手牌条（主交互）</li>
 *   <li>{@link com.example.monopoly_deal_game.view.scene.UiSceneLayer#ACTION_MODAL} — 选目标、多选牌（{@code actionLayer}）</li>
 *   <li>{@link com.example.monopoly_deal_game.view.scene.UiSceneLayer#MENU_OVERLAY} — 暂停菜单（半透明叠层）</li>
 * </ol>
 *
 * <p>
 * {@link com.example.monopoly_deal_game.view.scene.ScenePaneResolver} 把枚举解析为
 * {@link com.example.monopoly_deal_game.view.GameplayUiBundle} 里已有 {@link javafx.scene.layout.Pane}，
 * 避免在动效类里硬编码 {@code fx:id} 字符串。
 * </p>
 */
package com.example.monopoly_deal_game.view.scene;
