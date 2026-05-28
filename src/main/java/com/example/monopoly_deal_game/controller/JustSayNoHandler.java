package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.ActionState;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.JustSayNoMediator;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCardJustSayNo;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkMessage;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class



JustSayNoHandler {
    private final Stage stage;
    private final Consumer<String> feedback;
    private final Runnable refreshUi;
    private final NetworkSyncHelper networkSync;
    private final AtomicBoolean dialogBusy;

    public JustSayNoHandler(Stage stage, Consumer<String> feedback,
                            Runnable refreshUi, NetworkSyncHelper networkSync,
                            AtomicBoolean dialogBusy) {
        this.stage = stage;
        this.feedback = feedback;
        this.refreshUi = refreshUi;
        this.networkSync = networkSync;
        this.dialogBusy = dialogBusy;
    }

    public void installIntoMediator() {
        JustSayNoMediator.installUi(this::promptDialog);
    }

    public void uninstallFromMediator() {
        JustSayNoMediator.clearUi();
    }

    /**
     * Called when a JustSayNoRequest network message is received.
     * Forwards the JSN request to the correct targeted player.
     */
    public void handleRequestMessage(NetworkMessage msg) {
        Platform.runLater(() -> {
            String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
            if (localName == null || !localName.equals(msg.getPayerName())) return;

            GameSession session = msg.getSession() != null
                    ? msg.getSession()
                    : AppContext.get().gameEngine().getCurrentSession();
            if (session == null) return;

            Player respondent = session.findPlayerByName(msg.getPayerName());
            Player activator = msg.getReceiverName() != null
                    ? session.findPlayerByName(msg.getReceiverName())
                    : null;
            if (respondent == null) return;

            boolean used = promptDialog(respondent, activator, session, msg.getText());
            networkSync.sendJustSayNoResponse(msg, localName, respondent, activator, used);
        });
    }

    /**
     * Show Just Say No dialog to the TARGETED player (respondent).
     * If confirmed, JSN is removed directly and the action is refused.
     */
    public boolean promptDialog(Player respondent, Player activator,
                                 GameSession session, String situation) {
        if (dialogBusy.get()) return false;
        dialogBusy.set(true);
        try {
            if (session == null || respondent == null || !playerHasJustSayNo(respondent)) {
                return false;
            }

            // Networked game: forward JSN request to the targeted player's client
            // Only forward when there are actual remote clients connected;
            // in a single-machine game, show the dialog locally instead.
            Player local = session.localPlayer(
                    AppContext.get().networkLobbyState().getLocalPlayerName());
            String roomId = AppContext.get().networkLobbyState().getRoomId();
            boolean shouldForward = false;
            if (roomId != null && !roomId.isBlank()
                    && local != null && !local.getName().equals(respondent.getName())) {
                if (AppContext.get().networkLobbyState().isHost()) {
                    GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
                    shouldForward = room != null && room.hasRemoteClients();
                } else {
                    shouldForward = true;
                }
            }
            if (shouldForward) {
                if (AppContext.get().networkLobbyState().isHost()) {
                    GameServer.Room room = AppContext.get().gameServer().getRoom(roomId);
                    if (room != null) {
                        room.broadcastJustSayNoRequest(
                                UUID.randomUUID().toString(), respondent, activator, session, situation);
                    }
                } else {
                    networkSync.sendJustSayNoRequest(roomId, UUID.randomUUID().toString(),
                            respondent, activator, session, situation);
                }
                return false;
            }

            String act = activator != null ? activator.getName() : "对方";
            String desc = situation != null && !situation.isBlank() ? situation : act + " 对你使用了行动牌";

            ButtonType useNo = new ButtonType("使用 Just Say No（反对）", ButtonBar.ButtonData.APPLY);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    desc + "\n\n你持有「反对（Just Say No）」牌。\n"
                            + "使用：弃掉 JSN 牌并抵消该行动对你的效果。\n"
                            + "不使用：行动照常结算。",
                    useNo, new ButtonType("不使用反对（继续结算）", ButtonBar.ButtonData.CANCEL_CLOSE));
            alert.setTitle("Just Say No");
            if (stage != null) alert.initOwner(stage);
            alert.setHeaderText(respondent.getName() + " — 是否反对「" + act + "」的行动？");

            boolean use = alert.showAndWait().filter(r -> r == useNo).isPresent();
            if (use) {
                Card jsn = JustSayNoMediator.findJustSayNoRespondentHeld(respondent);
                if (jsn != null) {
                    if (!respondent.getHand().removeCard(jsn)) {
                        respondent.getBank().removeCard(jsn);
                    }
                    session.discardCard(jsn);
                }
                ActionState as = session.getGameState().getActionState();
                if (as != null && as != session.getGameState().getTurnState()) {
                    as.refuse(respondent, as.getActionOwner());
                }
            }
            // If JSN not used, caller handles setAccepted + side effects + sync
            return use;
        } finally {
            dialogBusy.set(false);
        }
    }

    public static boolean playerHasJustSayNo(Player player) {
        if (player == null) return false;
        for (Card c : player.getHand().getCards()) {
            if (isJustSayNo(c)) return true;
        }
        for (Card c : player.getBank().getCards()) {
            if (isJustSayNo(c)) return true;
        }
        return false;
    }

    private static boolean isJustSayNo(Card c) {
        return c instanceof ActionCardJustSayNo;
    }
}