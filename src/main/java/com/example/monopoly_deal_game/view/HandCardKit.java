package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.model.cards.Card;

/**
 * 在对局手牌区由领域 {@link Card} 生成可点击的 {@link CardView}。
 */
public final class HandCardKit {

    private HandCardKit() {}

    public static CardView createHandCard(
            Card card,
            boolean visuallySelected,
            Runnable onChosen) {
        String file = CardFaceResolver.imageFileFor(card);
        CardView view = new CardView(file, card.getName(), card.getDescription());
        view.setUserData(card);
        view.setHandInteraction(onChosen);
        view.setSelectionOutlineEnabled(true);
        view.setStrongHandSelection(visuallySelected);
        view.setHandSelected(visuallySelected);
        return view;
    }

    /** 手牌扇形排布：加在外层容器上，避免与卡面悬停位移叠加冲突。 */
    public static void applyFanPose(javafx.scene.layout.Region host, int index, int total) {
        if (host == null || total <= 1) {
            return;
        }
        double mid = (total - 1) / 2.0;
        double d = index - mid;
        host.setRotate(d * 5.5);
        host.setTranslateY(Math.min(18, Math.abs(d) * 5));
    }

    /** @return 可能为 null */
    public static Card modelOrNull(CardView view) {
        Object ud = view.getUserData();
        return ud instanceof Card c ? c : null;
    }
}
