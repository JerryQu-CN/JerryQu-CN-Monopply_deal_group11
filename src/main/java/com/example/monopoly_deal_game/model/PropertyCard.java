package com.example.monopoly_deal_game.model;

<<<<<<< HEAD
/**
 * 物业牌（含双色系、万能物业 Wild）。
 *
 * TODO(model): 颜色枚举、当前选择颜色、租金档位、是否整套；需求 5.4–5.6。
 */
public class PropertyCard extends Card {

=======
import java.util.ArrayList;
import java.util.List;

/**
 * 物业牌（含普通物业、双色系、万能物业 Wild）。
 * * 对应需求说明书：
 * - 5.4: 必须放置在物业区。
 * - 5.5: 包含租金档位信息，支持整套判定。
 * - 5.6: 颜色切换逻辑。
 */
public class PropertyCard extends Card {

    private final CardColor primaryColor;    // 初始主颜色
    private final CardColor secondaryColor;  // 双色卡的第二颜色（单色卡则为 null）
    private CardColor currentColor;          // 当前选择颜色（核心：用于逻辑判定和 UI 显示）

    private final boolean isWild;            // 是否为双色或全能卡
    private final int[] rentLevels;          // 租金档位（例如：{2, 4, 7} 表示 1张收2M，2张收4M，3张收7M）
    private final int fullSetThreshold;      // 判定“是否整套”的张数标准

    /**
     * 构造函数：单色物业卡
     */
    public PropertyCard(int id, String name, int value, CardColor color, int[] rentLevels) {
        super(id, name, value, "Property Card: " + color);
        this.primaryColor = color;
        this.currentColor = color;
        this.secondaryColor = null;
        this.isWild = false;
        this.rentLevels = rentLevels;
        this.fullSetThreshold = (rentLevels != null) ? rentLevels.length : 0;
    }

    /**
     * 构造函数：双色/全能物业卡
     */
    public PropertyCard(int id, String name, int value, CardColor c1, CardColor c2, int[] rentLevels, boolean isWild) {
        super(id, name, value, "Wild Property Card");
        this.primaryColor = c1;
        this.secondaryColor = c2;
        this.currentColor = c1; // 默认使用第一种颜色
        this.isWild = isWild;
        this.rentLevels = rentLevels;
        this.fullSetThreshold = (rentLevels != null) ? rentLevels.length : 0;
    }

    /**
     * 需求 5.6: 切换当前颜色
     * 逻辑层在处理双色卡切换时调用此方法改变其物理状态。
     */
    public void switchColor() {
        if (isWild && secondaryColor != null && secondaryColor != CardColor.WILD) {
            this.currentColor = (this.currentColor == primaryColor) ? secondaryColor : primaryColor;
        }
    }

    /**
     * 获取当前颜色组在拥有 count 张卡时的租金数额
     */
    public int getRent(int count) {
        if (rentLevels == null || count <= 0) return 0;
        int index = Math.min(count, rentLevels.length) - 1;
        return rentLevels[index];
    }

>>>>>>> ec928dc (Initial commit: rename folder and add all files)
    @Override
    public void use(Player user, Player target) {
        throw new UnsupportedOperationException("TODO(model+logic): 放入物业区而非直接当效果牌使用");
    }
<<<<<<< HEAD
}
=======

    // --- Getter 方法，供 Logic 层判定胜负和计算资产用 ---

    public CardColor getCurrentColor() {
        return currentColor;
    }

    public boolean isWild() {
        return isWild;
    }

    public int getFullSetThreshold() {
        return fullSetThreshold;
    }

    public int[] getRentLevels() {
        return rentLevels;
    }
}
>>>>>>> ec928dc (Initial commit: rename folder and add all files)
