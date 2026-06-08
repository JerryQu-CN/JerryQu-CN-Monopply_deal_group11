package com.example.monopoly_deal_game.controller.dialog;

import com.example.monopoly_deal_game.game.state.ActionState;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.JustSayNoMediator;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.network.GameServer;
import com.example.monopoly_deal_game.network.NetworkMessage;
import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.controller.NetworkSyncHelper;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Handles Just Say No dialogs and network forwarding — prompts targets
 * to accept or counter an action with a Just Say No card.
 */
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
     * Shows the JSN dialog to the local targeted player, then publishes
     * the session change so the server updates its authoritative state.
     */
    public void handleRequestMessage(NetworkMessage msg) {
        Platform.runLater(() -> {
            String localName = AppContext.get().networkLobbyState().getLocalPlayerName();
            if (localName == null || !localName.equals(msg.getPayerName())) return;

            GameSession session = AppContext.get().gameEngine().getCurrentSession();
            if (session == null) return;

            Player respondent = session.findPlayerByName(msg.getPayerName());
            Player activator = msg.getReceiverName() != null
                    ? session.findPlayerByName(msg.getReceiverName())
                    : null;
            if (respondent == null) return;

            boolean used = promptDialog(respondent, activator, session, msg.getText());
            networkSync.sendJustSayNoResponse(msg, localName, respondent, activator, used);

            if (used) {
                networkSync.publishSessionChange(session,
                        respondent.getName() + " used Just Say No to refuse "
                                + (activator != null ? activator.getName() : "the opponent")
                                + "'s action");
            } else {
                ActionState as = session.getGameState().getActionState();
                if (as != null && as != session.getGameState().getTurnState()
                        && as.isTarget(respondent)
                        && !as.isRefused(respondent) && !as.isAccepted(respondent)) {
                    as.setAccepted(respondent, true);
                    as.tryExecuteOnAccepted(respondent);
                    networkSync.publishSessionChange(session);
                }
            }
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
            Player local = session.localPlayer(
                    AppContext.get().networkLobbyState().getLocalPlayerName());
            if (NetworkSyncHelper.shouldForwardToRemote(local, respondent)) {
                String roomId = AppContext.get().networkLobbyState().getRoomId();
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

            boolean use = showJustSayNoAlert(respondent, activator, situation);
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

    private boolean showJustSayNoAlert(Player respondent, Player activator, String situation) {
        String act = activator != null ? activator.getName() : "the opponent";
        String desc = situation != null && !situation.isBlank() ? situation : act + " used an action card against you";

        ButtonType useNo = new ButtonType("Use Just Say No (Decline)", ButtonBar.ButtonData.APPLY);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                desc + "\n\nYou hold a Just Say No card.\n"
                        + "Use: discard the JSN card and cancel the action's effect on you.\n"
                        + "Do not use: the action resolves normally.",
                useNo, new ButtonType("Do Not Use (Continue Resolution)", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Just Say No");
        if (stage != null) alert.initOwner(stage);
        alert.setHeaderText(respondent.getName() + " -- Decline \"" + act + "\"'s action?");
        return alert.showAndWait().filter(r -> r == useNo).isPresent();
    }

    public static boolean playerHasJustSayNo(Player player) {
        return JustSayNoMediator.findJustSayNoRespondentHeld(player) != null;
    }
}