package com.example.monopoly_deal_game.view;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * 对局主界面在 FXML 中划分的功能区域引用（不含具体卡牌绘制逻辑）。
 * <p>
 * UI 类通常需要这些内容：
 * </p>
 * <ul>
 *   <li><b>根与顶栏</b>：{@link #gameRoot()}、{@link #topbar()} —— 全屏覆盖层、菜单入口。</li>
 *   <li><b>左侧 HUD</b>：{@link #leftSidebar()} —— 牌堆示意、步数、主操作按钮等。</li>
 *   <li><b>牌桌分区</b>：{@link #deckPane()}、{@link #discardPane()}、{@link #voidPane()}、
 *   {@link #opponentsPane()}、{@link #selfBoardPane()}、{@link #handPane()} —— 之后在此挂载自定义 Node 或 Canvas。</li>
 *   <li><b>交互层</b>：{@link #actionLayer()} —— 选牌、选目标等模态交互。</li>
 *   <li><b>菜单遮罩</b>：{@link #menuOverlay()} —— 暂停/离开对局。</li>
 *   <li><b>可选</b>：{@link #chatPane()} —— 仅完整对局界面存在；首次运行/精简布局可为 {@code null}。</li>
 * </ul>
 * <p>
 * 由 {@link com.example.monopoly_deal_game.controller.AbstractGameplayScreenController#toUiBundle()} 从已加载的 FXML 构造，
 * 便于把「布局引用」传给纯 Java 的视图助手类，避免 controller 继续膨胀。
 * </p>
 */
public record GameplayUiBundle(
        AnchorPane gameRoot,
        HBox topbar,
        VBox leftSidebar,
        Pane deckPane,
        Pane discardPane,
        Pane voidPane,
        Pane opponentsPane,
        Pane selfBoardPane,
        Pane handPane,
        Pane actionLayer,
        StackPane menuOverlay,
        Pane chatPane) {

    public GameplayUiBundle {
        Objects.requireNonNull(gameRoot);
        Objects.requireNonNull(topbar);
        Objects.requireNonNull(leftSidebar);
        Objects.requireNonNull(deckPane);
        Objects.requireNonNull(discardPane);
        Objects.requireNonNull(voidPane);
        Objects.requireNonNull(opponentsPane);
        Objects.requireNonNull(selfBoardPane);
        Objects.requireNonNull(handPane);
        Objects.requireNonNull(actionLayer);
        Objects.requireNonNull(menuOverlay);
    }
}
