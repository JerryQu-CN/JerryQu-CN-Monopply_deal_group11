package com.example.monopoly_deal_game.game.model;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action state base class. Aligned with the design of ActionState in Monopoly-Deal-main.
 * Each ActionState has one actionOwner and several targets, controlling "who can do what" in the game.
 * When the top of the stack has an ActionState overriding the TurnState, only the relevant players can interact.
 */
public abstract class ActionState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Player actionOwner;
    private final Map<Player, TargetState> targets = new HashMap<>();
    private String status = "";

    protected ActionState(Player actionOwner) {
        this.actionOwner = actionOwner;
    }

    protected ActionState(Player actionOwner, String status) {
        this(actionOwner);
        this.status = status != null ? status : "";
    }

    protected ActionState(Player actionOwner, Player actionTarget) {
        this(actionOwner);
        targets.put(actionTarget, TargetState.TARGETED);
    }

    protected ActionState(Player actionOwner, Player actionTarget, String status) {
        this(actionOwner, actionTarget);
        this.status = status != null ? status : "";
    }

    protected ActionState(Player actionOwner, List<Player> actionTargets) {
        this(actionOwner);
        for (Player p : actionTargets) {
            targets.put(p, TargetState.TARGETED);
        }
    }

    protected ActionState(Player actionOwner, List<Player> actionTargets, String status) {
        this(actionOwner, actionTargets);
        this.status = status != null ? status : "";
    }

    // ---- Lifecycle hooks ----

    public void onAdd() {}
    public void onRemove() {}
    public void onFocus() {}
    public void onUnfocus() {}

    // ---- Stack management (set after construction by GameState) ----

    private GameState gameState;

    void setGameState(GameState gs) {
        this.gameState = gs;
    }

    protected GameState getGameState() {
        return gameState;
    }

    public void removeState() {
        if (gameState != null) {
            gameState.removeActionState(this);
        }
    }

    public void replaceState(ActionState newState) {
        if (gameState != null) {
            gameState.swapActionState(this, newState);
        }
    }

    // ---- Accessors ----

    public Player getActionOwner() {
        return actionOwner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "";
    }

    // ---- Target management ----

    public boolean isTarget(Player player) {
        return targets.containsKey(player);
    }

    public Map<Player, TargetState> getTargets() {
        return targets;
    }

    public Player getTargetPlayer() {
        return targets.isEmpty() ? null : targets.keySet().iterator().next();
    }

    public List<Player> getTargetPlayers() {
        return new ArrayList<>(targets.keySet());
    }

    public List<Player> getTargetPlayers(TargetState ofState) {
        List<Player> result = new ArrayList<>();
        for (var entry : targets.entrySet()) {
            if (entry.getValue() == ofState) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public TargetState getTargetState(Player player) {
        return targets.getOrDefault(player, TargetState.NOT_TARGETED);
    }

    public void setTargetState(Player player, TargetState state) {
        if (state == TargetState.NOT_TARGETED) {
            targets.remove(player);
        } else {
            targets.put(player, state);
        }
        checkFinished();
    }

    // ---- Refuse / Accept ----

    public void setRefused(Player player, boolean refused) {
        setTargetState(player, refused ? TargetState.REFUSED : TargetState.TARGETED);
    }

    public boolean isRefused(Player player) {
        return targets.get(player) == TargetState.REFUSED;
    }

    public List<Player> getRefused() {
        return getTargetPlayers(TargetState.REFUSED);
    }

    public void setAccepted(Player player, boolean accepted) {
        setTargetState(player, accepted ? TargetState.ACCEPTED : TargetState.TARGETED);
    }

    public boolean isAccepted(Player player) {
        return targets.get(player) == TargetState.ACCEPTED;
    }

    public List<Player> getAccepted() {
        return getTargetPlayers(TargetState.ACCEPTED);
    }

    public int getNumberOfRefused() {
        int n = 0;
        for (TargetState ts : targets.values()) {
            if (ts == TargetState.REFUSED) n++;
        }
        return n;
    }

    public int getNumberOfAccepted() {
        int n = 0;
        for (TargetState ts : targets.values()) {
            if (ts == TargetState.ACCEPTED) n++;
        }
        return n;
    }

    public int getNumberOfTargets() {
        return targets.size();
    }

    /**
     * Whether the given player can refuse (play JSN) against the given target.
     * Case 1: player is a target, target is actionOwner, player not yet refused → player can JSN the action
     * Case 2: player is actionOwner, target is a refused target → player can counter-JSN
     */
    public boolean canRefuse(Player player, Player target) {
        return (isTarget(player) && getActionOwner() == target && !isRefused(player))
                || (getActionOwner() == player && isTarget(target) && isRefused(target));
    }

    /**
     * Whether the given player can refuse anyone involved in this state.
     */
    public boolean canRefuseAny(Player player) {
        return (isTarget(player) && !isRefused(player))
                || (getActionOwner() == player && getNumberOfRefused() > 0);
    }

    /**
     * Execute a refuse action. If player is a target and target is the actionOwner,
     * the player becomes REFUSED. If player is the actionOwner and target is REFUSED,
     * the target's refused status is cleared (counter-refuse).
     */
    public void refuse(Player player, Player target) {
        if (isTarget(player) && getActionOwner() == target && !isRefused(player)) {
            setRefused(player, true);
        } else if (getActionOwner() == player && isTarget(target) && isRefused(target)) {
            setRefused(target, false);
        }
    }

    // ---- Finished ----

    private void checkFinished() {
        if (isFinished()) {
            removeState();
        }
    }

    /**
     * Default: finished when all targets have either accepted or refused.
     */
    public boolean isFinished() {
        int total = getNumberOfTargets();
        return total > 0 && (getNumberOfAccepted() + getNumberOfRefused()) >= total;
    }

    /**
     * If false, this state can be removed when a new state is added.
     */
    public boolean isImportant() {
        return true;
    }
}