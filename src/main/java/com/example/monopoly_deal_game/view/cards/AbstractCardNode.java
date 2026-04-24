package com.example.monopoly_deal_game.view.cards;

import com.example.monopoly_deal_game.model.Card;
import javafx.scene.layout.Region;

/**
 * 单张卡牌在界面上的根节点（尺寸、样式、悬停/选中态的公共约定）。
 * <p>
 * 子类负责根据具体 {@link Card} 类型刷新文字/颜色/角标；不在这里写洗牌/摸牌轨迹（见 {@code view.animation}）。
 * </p>
 */
public abstract class AbstractCardNode extends Region {

    protected AbstractCardNode() {
        getStyleClass().add("monopoly-card");
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    /** 用模型数据刷新外观；调用方保证 {@code card} 类型与本 Node 匹配。 */
    public abstract void renderFrom(Card card);

    /** 牌背朝上时的占位（例如牌堆顶、对手手牌只显示张数时的缩略）。 */
    public void showBack(boolean back) {
        // TODO(view): 切换 CSS pseudo-class 或子节点可见性
    }
}
