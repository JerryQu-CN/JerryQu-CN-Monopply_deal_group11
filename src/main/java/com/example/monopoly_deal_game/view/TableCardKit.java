package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.model.PlayedCardSnapshot;
import com.example.monopoly_deal_game.model.cards.Card;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;

/**
 * Table area read-only card node: fixed placeholder, scaled to appropriate size, avoids confusion with hand area interaction logic.
 */
public final class TableCardKit {

    public static final double TABLE_CARD_BASE_W = 110;
    public static final double TABLE_CARD_BASE_H = 160;

    private TableCardKit() {}

    public static StackPane wrapTableCard(CardView cardView, double scale) {
        StackPane holder = new StackPane(cardView);
        holder.setAlignment(Pos.CENTER);
        holder.setCursor(Cursor.DEFAULT);
        holder.setFocusTraversable(false);
        double w = TABLE_CARD_BASE_W * scale;
        double h = TABLE_CARD_BASE_H * scale;
        holder.setPrefSize(w, h);
        holder.setMinSize(w, h);
        holder.setMaxSize(w, h);
        cardView.setScaleX(scale);
        cardView.setScaleY(scale);
        return holder;
    }

    public static StackPane createFromSnapshot(PlayedCardSnapshot snap, double scale) {
        if (snap == null) {
            return wrapTableCard(new CardView("propertyWildCard.png", "", ""), scale);
        }
        CardView cv =
                new CardView(
                        snap.imageFileName() != null ? snap.imageFileName() : "propertyWildCard.png",
                        snap.name() != null ? snap.name() : "",
                        "");
        cv.setHandInteraction(null);
        cv.setHoverZoomEnabled(false);
        return wrapTableCard(cv, scale);
    }

    /**
     * Table model card read-only wrapper: scaled placeholder matches visual appearance (avoids layout compression when using only {@code Scale}).
     */
    public static StackPane createReadOnlyCard(Card domainCard, double scale) {
        if (domainCard == null) {
            CardView fallback = new CardView("propertyWildCard.png", "", "");
            fallback.setReadOnly(true);
            return wrapTableCard(fallback, scale);
        }
        CardView cv =
                new CardView(
                        CardFaceResolver.imageFileFor(domainCard),
                        domainCard.getName() != null ? domainCard.getName() : "",
                        "");
        cv.setHandInteraction(null);
        cv.setHoverZoomEnabled(false);
        cv.setReadOnly(true);
        return wrapTableCard(cv, scale);
    }
}
