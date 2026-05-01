package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.view.animation.MotionContext;
import com.example.monopoly_deal_game.view.scene.ScenePaneResolver;

/**
 * 将 {@link GameplayUiBundle} 与模型更新串起来的占位协调类（后续可实现 {@link GameObserver} 或绑定只读 VM）。
 * <p>
 * Controller 在 {@code initialize} 里构造本类并保存引用；逻辑层推送 {@link GameSession} 后调用
 * {@link #refreshFromSession(GameSession)} 刷新各分区。
 * </p>
 */
public class GameplayViewCoordinator {

    private final GameplayUiBundle zones;
    private final ScenePaneResolver sceneResolver;

    public GameplayViewCoordinator(GameplayUiBundle zones) {
        this.zones = zones;
        this.sceneResolver = new ScenePaneResolver(zones);
    }

    public GameplayUiBundle zones() {
        return zones;
    }

    public ScenePaneResolver sceneResolver() {
        return sceneResolver;
    }

    /** 供 {@link com.example.monopoly_deal_game.view.animation.UiMotion} 使用；时长可按需调整。 */
    public MotionContext motionContext() {
        return MotionContext.forTable(
                zones.deckPane(), zones.handPane(), zones.discardPane(), zones.actionLayer());
    }

    /** TODO(view+logic): 根据 session 重绘手牌区、对手缩略、银行/物业等。 */
    public void refreshFromSession(GameSession session) {
        if (session == null) {
            return;
        }
        // 占位：避免空实现被误删后忘记接线上文
    }
}
