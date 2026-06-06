package com.example.monopoly_deal_game.controller.dialog;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.payment.PaymentRequest;
import com.example.monopoly_deal_game.logic.payment.PaymentService;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.logic.CardImageMapper;
import com.example.monopoly_deal_game.view.CardView;
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
 * Manual payment selection: choose from bank and table properties (hand cards not allowed).
 */
public final class PaymentPickDialogs {

    private static final double CARD_SCALE = 0.82;

    private PaymentPickDialogs() {}

    static Optional<List<Card>> choosePaymentCards(Stage owner, PaymentRequest req) {
        Player payer = req.payer();
        int amountDueM = req.amountM();
        String reasonLines = req.description();
        final int liquidity = PaymentService.totalLiquidityValue(payer);

        // No assets → simple confirm dialog, no card selection needed
        if (liquidity <= 0) {
            return showSimpleConfirm(owner, payer, reasonLines);
        }

        Dialog<Optional<List<Card>>> dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        Window activeOwner = ActionTargetDialogs.resolveOwnerWindow(owner);
        if (activeOwner != null) {
            dialog.initOwner(activeOwner);
        }
        dialog.setTitle("Deliver Assets");

        DialogPane pane = dialog.getDialogPane();

        LinkedHashSet<Card> picked = new LinkedHashSet<>();

        ButtonType btnConfirm = new ButtonType("Confirm Hand Over Selected Cards", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(btnConfirm);

        Label titleLbl = new Label(reasonLines != null ? reasonLines : "");
        titleLbl.setWrapText(true);
        titleLbl.setStyle("-fx-font-size:13px;");

        Label capLbl = new Label();
        capLbl.setWrapText(true);
        capLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#37474f;");

        Runnable refreshHints =
                () -> {
                    int sumSel = picked.stream().mapToInt(c -> Math.max(0, c.getValue())).sum();
                    boolean ok =
                            liquidity <= 0
                                    || (!picked.isEmpty()
                                            && PaymentService.isValidManualPaymentChoice(
                                                    payer, new ArrayList<>(picked), amountDueM));
                    capLbl.setText(
                            String.format(
                                            "Amount due: %dM | Your total liquidatable assets: %dM\nCurrently selected: %dM",
                                            amountDueM, liquidity, sumSel)
                                    + (liquidity > 0 && liquidity < amountDueM
                                            ? "\n* Insufficient assets: please select all cards you must hand over."
                                            : ""));
                    Button confirmBtn = (Button) pane.lookupButton(btnConfirm);
                    confirmBtn.setDisable(!ok);
                };

        ScrollPane scroll = buildPaymentCardScroll(payer, picked, refreshHints);

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
                    if (PaymentService.totalLiquidityValue(payer) <= 0) {
                        return Optional.of(List.of());
                    }
                    if (!PaymentService.isValidManualPaymentChoice(payer, out, amountDueM)) {
                        return Optional.empty();
                    }
                    return Optional.of(out);
                });

        refreshHints.run();
        return dialog.showAndWait().orElse(Optional.empty());
    }

    private static Optional<List<Card>> showSimpleConfirm(Stage owner, Player payer, String reasonLines) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Due");
        alert.setHeaderText(reasonLines != null ? reasonLines : "You need to pay, but have no assets.");
        alert.setContentText(payer.getName() + " has no bank cards or table properties to pay with.\nThe debt is cleared automatically.");
        Window activeOwner = ActionTargetDialogs.resolveOwnerWindow(owner);
        if (activeOwner != null) {
            alert.initOwner(activeOwner);
        }
        alert.showAndWait();
        return Optional.of(List.of());
    }

    private static ScrollPane buildPaymentCardScroll(Player payer,
                                                      LinkedHashSet<Card> picked,
                                                      Runnable refreshHints) {
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
            bankRow.getChildren().add(new Label("(No cards in bank)"));
        }
        if (propTiles == 0) {
            propRow.getChildren().add(new Label("(No property cards on table to pay with)"));
        }

        ScrollPane scroll = new ScrollPane(
                new VBox(14,
                        new Label("Select from bank:"),
                        bankRow,
                        new Separator(),
                        new Label("Select from table properties:"),
                        propRow));
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(380);
        return scroll;
    }

    private static StackPane buildChip(Card model, LinkedHashSet<Card> picked, Runnable refreshHints) {
        StackPane wrap = new StackPane();
        String fn = CardImageMapper.imageFileFor(model);
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

        syncVisual.run();
        refreshHints.run();

        return wrap;
    }
}
