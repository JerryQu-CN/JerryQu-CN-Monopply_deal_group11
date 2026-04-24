package com.example.monopoly_deal_game.view.scene;

/**
 * 对局界面内的逻辑图层（不是 JavaFX {@link javafx.scene.Scene} 的「一整个窗口」）。
 */
public enum UiSceneLayer {

    /** 主牌桌：对手、本人银行/物业条、手牌、侧栏牌堆示意。 */
    TABLE,

    /** 交互遮罩：选牌、选玩家、确认/取消。 */
    ACTION_MODAL,

    /** 系统菜单：离开对局、设置等。 */
    MENU_OVERLAY
}
