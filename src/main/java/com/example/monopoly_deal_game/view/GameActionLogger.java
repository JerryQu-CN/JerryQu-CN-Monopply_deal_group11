package com.example.monopoly_deal_game.view;

import com.example.monopoly_deal_game.game.state.ActionState;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.Card;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Logs game events, state transitions, and card plays to the sidebar log panel.
 */
public class GameActionLogger {

    private final VBox logEntriesBox;
    private final ScrollPane logScrollPane;
    private final StackPane gameOverOverlay;
    private final Label gameOverWinnerLabel;
    private final Label gameOverDetailLabel;

    private String lastKnownTurnPlayer;
    private GameState.Phase lastKnownPhase;
    private String lastLoggedActionOwner;
    private String pendingNetworkLogText;

    public GameActionLogger(VBox logEntriesBox, ScrollPane logScrollPane,
                            StackPane gameOverOverlay, Label gameOverWinnerLabel,
                            Label gameOverDetailLabel) {
        this.logEntriesBox = logEntriesBox;
        this.logScrollPane = logScrollPane;
        this.gameOverOverlay = gameOverOverlay;
        this.gameOverWinnerLabel = gameOverWinnerLabel;
        this.gameOverDetailLabel = gameOverDetailLabel;
    }

    // ---- pending log ----

    public void setPendingLog(String text) {
        pendingNetworkLogText = text;
    }

    public String getPendingLog() {
        return pendingNetworkLogText;
    }

    public void clearPendingLog() {
        pendingNetworkLogText = null;
    }

    // ---- log entries ----

    public void appendLog(String text) {
        appendLog(text, false);
    }

    public void appendLog(String text, boolean important) {
        if (logEntriesBox == null) return;
        Label entry = new Label(text);
        entry.setWrapText(true);
        entry.setMaxWidth(230);
        entry.setFocusTraversable(false);
        entry.setMouseTransparent(true);
        if (important) {
            entry.getStyleClass().add("log-entry-important");
        } else {
            entry.getStyleClass().add("log-entry-normal");
        }
        logEntriesBox.getChildren().add(entry);
        Platform.runLater(() -> logScrollPane.setVvalue(1.0));
    }

    public void appendSeparator(String text) {
        if (logEntriesBox == null) return;
        Label entry = new Label(text);
        entry.setWrapText(true);
        entry.setMaxWidth(230);
        entry.setFocusTraversable(false);
        entry.setMouseTransparent(true);
        entry.getStyleClass().add("log-entry-separator");
        logEntriesBox.getChildren().add(entry);
        Platform.runLater(() -> logScrollPane.setVvalue(1.0));
    }

    // ---- state transition logging ----

    public void logStateTransition(GameSession session) {
        GameState gs = session.getGameState();
        Player cur = session.getCurrentPlayer();
        String curName = cur != null ? cur.getName() : null;
        GameState.Phase phase = gs.getPhase();

        if (!Objects.equals(curName, lastKnownTurnPlayer) && curName != null) {
            lastKnownTurnPlayer = curName;
            appendSeparator("-- " + curName + "'s turn --");
        }

        ActionState as = gs.getActionState();
        if (phase == GameState.Phase.WAITING_FOR_SELECTION
                && as != null && as != gs.getTurnState()) {
            String owner = as.getActionOwner() != null ? as.getActionOwner().getName() : "?";
            String desc = as.getStatus() != null && !as.getStatus().isBlank()
                    ? as.getStatus() : "";
            if (!Objects.equals(owner, lastLoggedActionOwner)
                    || lastKnownPhase != phase) {
                lastLoggedActionOwner = owner;
                appendLog("Waiting for " + owner + "'s action response..." + desc);
            }
        } else {
            lastLoggedActionOwner = null;
        }

        lastKnownPhase = phase;
    }

    public void logRemoteSessionUpdate(GameSession session) {
        GameState gs = session.getGameState();
        ActionState as = gs.getActionState();
        if (as == null || as == gs.getTurnState()) return;

        String owner = as.getActionOwner() != null ? as.getActionOwner().getName() : "?";
        java.util.List<Player> targets = as.getTargetPlayers();
        String desc = as.getStatus() != null && !as.getStatus().isBlank()
                ? "(" + as.getStatus() + ")" : "";

        if (targets != null && !targets.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < targets.size(); i++) {
                if (i > 0) sb.append(i == targets.size() - 1 ? " and " : ", ");
                sb.append(targets.get(i).getName());
            }
            appendLog(owner + " used an action card on " + sb + desc, true);
        } else {
            appendLog(owner + " used an action card" + desc, true);
        }
    }

    // ---- card play log ----

    public void logCardPlay(String who, Card card, CardPlayOptions opts, GameSession session) {
        if (opts.asBankMoney()) {
            setPendingLog(who + " banked " + card.getName() + " (+" + card.getValue() + "M)");
            return;
        }
        setPendingLog(card.getPlayLogText(who, opts, session));
    }

    // ---- game over ----

    public void announceWinner(GameSession session) {
        Player winner = null;
        int maxSets = 0;
        for (Player p : session.getPlayers()) {
            int sets = p.getFullSetCount();
            if (sets > maxSets) { maxSets = sets; winner = p; }
        }
        if (winner != null) {
            setPendingLog(" Game over! " + winner.getName() + " wins! (" + maxSets + " full sets)");
            if (gameOverWinnerLabel != null) {
                gameOverWinnerLabel.setText(winner.getName() + " is the winner!");
            }
            if (gameOverDetailLabel != null) {
                gameOverDetailLabel.setText(maxSets + " full property sets completed");
            }
            if (gameOverOverlay != null) {
                gameOverOverlay.setVisible(true);
                gameOverOverlay.setManaged(true);
            }
        }
    }

    // ---- utility ----

    public static String formatPhase(GameState.Phase phase) {
        if (phase == null) return "--";
        return switch (phase) {
            case DRAW_PHASE -> "Phase: Draw";
            case PLAY_PHASE -> "Phase: Play";
            case DISCARD_PHASE -> "Phase: Discard";
            case WAITING_FOR_SELECTION -> "Phase: Waiting...";
        };
    }
}
