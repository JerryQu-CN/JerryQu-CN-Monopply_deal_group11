package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.logic.CardImageMapper;
import com.example.monopoly_deal_game.model.cards.Card;

/**
 * Generates clickable {@link CardView} from domain {@link Card} in the in-game hand area.
 */
public final class HandCardKit {

    private HandCardKit() {}

    public static CardView createHandCard(
            Card card,
            boolean visuallySelected,
            Runnable onChosen) {
        String file = CardImageMapper.imageFileFor(card);
        CardView view = new CardView(file, card.getName(), card.getDescription());
        view.setUserData(card);
        view.setHandInteraction(onChosen);
        view.setSelectionOutlineEnabled(true);
        view.setStrongHandSelection(visuallySelected);
        view.setHandSelected(visuallySelected);
        return view;
    }

    /** Fan layout for hand cards: arc rotation and vertical offset from center. */
    public static void applyFanPose(javafx.scene.layout.Region host, int index, int total) {
        if (host == null || total <= 1) {
            return;
        }
        double mid = (total - 1) / 2.0;
        double d = index - mid;
        host.setRotate(d * 7.0);
        host.setTranslateY(Math.abs(d) * (total > 10 ? 4.5 : 6.5));
    }

    /** @return may be null */
    public static Card modelOrNull(CardView view) {
        Object ud = view.getUserData();
        return ud instanceof Card c ? c : null;
    }
}
