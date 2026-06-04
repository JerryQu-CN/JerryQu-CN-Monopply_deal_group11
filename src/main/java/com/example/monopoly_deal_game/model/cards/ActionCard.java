package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.SerializablePlayerAction;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for action cards — handles opponent targeting, sub-target selection,
 * and pushing targeted action states onto the game state stack.
 */
public abstract class ActionCard extends Card {

    private final boolean countsTowardLimit;

    protected ActionCard(int id, String name, int value, boolean countsTowardLimit) {
        super(id, name, value, "Action Card: " + name);
        this.countsTowardLimit = countsTowardLimit;
    }

    @Override
    public CardType getCardType() {
        return CardType.ACTION;
    }

    @Override
    public boolean isCountsTowardLimit() {
        return countsTowardLimit;
    }

    /** Execute this action card's effect. Called by GameLogic after removing card from hand. */
    public abstract void doPlay(Player player, GameSession session, CardPlayOptions options);

    @Override
    public void executePlay(Player player, GameSession session, CardPlayOptions opt) {
        doPlay(player, session, opt);
    }

    /** Whether this action card requires the player to choose an opponent target. */
    public boolean needsChosenOpponent() { return false; }

    /** List of opponents that are valid targets for this action card. */
    public List<Player> eligibleOpponents(Player actor, GameSession session) { return List.of(); }

    /** Whether this action card's effect can currently be used by the given player. */
    public boolean canUseEffect(Player actor, GameSession session, CardPlayOptions opt) { return false; }

    /** Whether this action card can only be deposited to bank when played actively (not used as an effect). */
    public boolean isOnlyBankable() { return false; }

    /** Type of sub-target selection needed beyond choosing an opponent. */
    public enum SubTarget { NONE, STEALABLE_PROPERTY, FORCED_DEAL_PROPERTIES, MONOPOLY_GROUP }
    public SubTarget subTarget() { return SubTarget.NONE; }

    @Override
    public String getPlayLogText(String who, CardPlayOptions opts, GameSession session) {
        Player target = opts.actionTargetPlayer();
        return target != null
                ? who + " used " + getName() + " on " + target.getName()
                : who + " used " + getName();
    }

    /** Push a targeted action state (player-targeted), register the onAccept callback,
     *  and discard this card. Eliminates repeated boilerplate in subclasses. */
    protected void pushTargetedState(Player actor, Player target, String actionName,
                                      GameSession session, SerializablePlayerAction onAccept) {
        ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(actor, target,
                actor.getName() + " used " + actionName + " against " + target.getName());
        state.setOnAccepted(onAccept);
        session.getGameState().addActionState(state);
        session.discardCard(this);
    }

    /** Helper: all players except the given actor. */
    protected List<Player> otherPlayers(Player actor, GameSession session) {
        List<Player> out = new ArrayList<>();
        for (Player p : session.getPlayers()) {
            if (p != null && !p.equals(actor)) out.add(p);
        }
        return out;
    }
}