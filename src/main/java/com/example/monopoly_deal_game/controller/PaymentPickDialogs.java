package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.PaymentService;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.view.CardFaceResolver;
import com.example.monopoly_deal_game.view.CardView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * 真人支付时自选「银行」与「桌上物业」（不可选手牌）。
 */
final class PaymentPickDialogs {

    private static final double CARD_SCALE = 0.82;

    private PaymentPickDialogs() {}

    static Optional<List<Card>> choosePaymentCards(
            Stage owner,
            Player payer,
            int amountDueM,
            Player receiverIgnored,
            GameSession sessionIgnored,
            String reasonLines) {

        Dialog<Optional<List<Card>>> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        Window activeOwner = owner;
        if (activeOwner == null && !Window.getWindows().isEmpty()) {
            activeOwner = Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        }
        if (activeOwner != null) {
            dialog.initOwner(activeOwner);
        }
        dialog.setTitle("交付资产");

        DialogPane pane = dialog.getDialogPane();

        LinkedHashSet<Card> picked = new LinkedHashSet<>();

        ButtonType btnConfirm =
                new ButtonType("确认交出所选牌", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAuto =
                new ButtonType("改用自动抵扣", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().setAll(btnConfirm, btnAuto);

        Label titleLbl = new Label(reasonLines != null ? reasonLines : "");
        titleLbl.setWrapText(true);
        titleLbl.setStyle("-fx-font-size:13px;");

        Label capLbl = new Label();
        capLbl.setWrapText(true);
        capLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#37474f;");
        final int liquidity = PaymentService.totalLiquidityValue(payer);

        Runnable refreshHints =
                () -> {
                    int sumSel =
                            picked.stream().mapToInt(c -> Math.max(0, c.getValue())).sum();
                    boolean ok =
                            picked.isEmpty()
                                    ? false
                                    : PaymentService.isValidManualPaymentChoice(
                                            payer, new ArrayList<>(picked), amountDueM);

                    capLbl.setText(
                            String.format(
                                    "需支付 %dM　|　你可抵债的资产总值：%dM\n当前已选：%dM",
                                    amountDueM, liquidity, sumSel)
                                    + (liquidity > 0 && liquidity < amountDueM
                                            ? "\n※ 总资产不足时请勾选全部应交出的牌。"
                                            : ""));

                    Button confirmBtn =
                            (Button)
                                    pane.lookupButton(btnConfirm);
                    confirmBtn.setDisable(!ok);
                };

        FlowPane bankRow = new FlowPane();
        bankRow.setHgap(10);
        bankRow.setVgap(10);
        bankRow.setAlignment(Pos.TOP_LEFT);

        FlowPane propRow = new FlowPane();
        propRow.setHgap(10);
        propRow.setVgap(10);
        propRow.setAlignment(Pos.TOP_LEFT);

        for (Card c : payer.getBank().getCards()) {
            bankRow.getChildren().add(buildChip(c, picked, refreshHints));
        }

        int propTiles = 0;
        for (Property ps : payer.getProperties()) {
            for (PropertyCard pc : ps.getCards()) {
                if (PaymentService.isPayableHeldByPlayer(payer, pc)) {
                    propRow.getChildren().add(buildChip(pc, picked, refreshHints));
                    propTiles++;
                }
            }
        }

        if (bankRow.getChildren().isEmpty()) {
            bankRow.getChildren().add(new Label("（银行暂无牌）"));
        }
        if (propTiles == 0) {
            propRow.getChildren().add(new Label("（桌上暂无物业抵债）"));
        }

        ScrollPane scroll =
                new ScrollPane(
                        new VBox(
                                14,
                                new Label("可从银行勾选："),
                                bankRow,
                                new Separator(),
                                new Label("可从桌上物业勾选："),
                                propRow));
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(380);

        VBox root = new VBox(12, titleLbl, capLbl, scroll);
        root.setPadding(new Insets(16));
        VBox.setVgrow(scroll, Priority.ALWAYS);

        pane.setContent(root);

        dialog.setResultConverter(
                bt -> {
                    if (bt != btnConfirm) {
                        return Optional.empty();
                    }
                    List<Card> out = new ArrayList<>(picked);
                    if (!PaymentService.isValidManualPaymentChoice(payer, out, amountDueM)) {
                        return Optional.empty();
                    }
                    return Optional.of(out);
                });

        Platform.runLater(refreshHints);
        return dialog.showAndWait().orElse(Optional.empty());
    }

    private static StackPane buildChip(
            Card model, LinkedHashSet<Card> picked, Runnable refreshHints) {

        StackPane wrap = new StackPane();
        String fn = CardFaceResolver.imageFileFor(model);
        CardView cv = new CardView(fn, model.getName(), "");
        cv.setHoverZoomEnabled(false);
        cv.setSelectionOutlineEnabled(true);

        Runnable syncVisual =
                () -> {
                    boolean on = picked.contains(model);
                    cv.setHandSelected(on);
                    cv.setStrongHandSelection(on);
                    wrap.setScaleX(on ? 1.06 * CARD_SCALE : CARD_SCALE);
                    wrap.setScaleY(on ? 1.06 * CARD_SCALE : CARD_SCALE);
                };

        cv.setHandInteraction(
                () -> {
                    if (picked.remove(model)) {
                        syncVisual.run();
                    } else {
                        picked.add(model);
                        syncVisual.run();
                    }
                    refreshHints.run();
                });

        wrap.getChildren().add(cv);
        wrap.setPrefSize(110 * CARD_SCALE + 16, 160 * CARD_SCALE + 16);
        wrap.setScaleX(CARD_SCALE);
        wrap.setScaleY(CARD_SCALE);

        Platform.runLater(
                () -> {
                    syncVisual.run();
                    refreshHints.run();
                });

        return wrap;
    }
}
