package com.example.monopoly_deal_game.controller.gameplay;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.logic.payment.RentCalculator;
import com.example.monopoly_deal_game.logic.payment.RentRules;
import com.example.monopoly_deal_game.controller.dialog.ActionTargetDialogs;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;

import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Resolves target player, property card, color, or property group selections
 * for action cards and rent cards.
 */
public class TargetSelectionHandler {
    private final Stage stage;
    private final AtomicBoolean dialogBusy;

    public TargetSelectionHandler(Stage stage, AtomicBoolean dialogBusy) {
        this.stage = stage;
        this.dialogBusy = dialogBusy;
    }

    public CardPlayOptions mergeTargets(GameSession session, Card card,
                                         CardPlayOptions options) {
        if (session == null || options == null) return options;
        Player me = session.getCurrentPlayer();
        if (me == null || options.asBankMoney()) return options;

        if (card instanceof RentCard rc) {
            return mergeRentTargets(session, rc, options, me);
        }
        if (card instanceof ActionCard ac) {
            return mergeActionTargets(session, ac, options, me);
        }
        return options;
    }

    private CardPlayOptions mergeRentTargets(GameSession session, RentCard rc,
                                              CardPlayOptions options, Player me) {
        List<Player> renters = RentRules.rentersExcludingLandlord(me, session);
        if (renters.isEmpty() || !RentRules.canUseRentEffect(rc, me, options.rentColorChoice())) {
            return options;
        }
        CardColor cap = options.rentColorChoice();
        if (options.actionTargetPlayer() != null && renters.contains(options.actionTargetPlayer())) {
            return CardPlayOptions.rentWithColorAndPlayer(cap, options.actionTargetPlayer());
        }
        // Dual-color rent cards charge all players globally, no target selection needed
        if (!rc.isWildRent() && rc.getApplicableColors().size() >= 2) {
            return options;
        }
        boolean dbl = session.getGameState().isDoubleNextRent();
        int preview = cap != null
                ? (dbl ? RentCalculator.rentOnColor(me, cap) * 2 : RentCalculator.rentOnColor(me, cap))
                : (rc.isWildRent()
                        ? RentCalculator.bestRentWild(me, dbl)
                        : RentCalculator.bestRentForLandlord(me, rc.getApplicableColors(), dbl));
        return ActionTargetDialogs.chooseOpponent(stage, "Collect Rent",
                        "Please select which player to collect rent from (estimated: " + preview + "M).", renters)
                .map(p -> CardPlayOptions.rentWithColorAndPlayer(cap, p))
                .orElse(null);
    }

    private CardPlayOptions mergeActionTargets(GameSession session, ActionCard ac,
                                                CardPlayOptions options, Player me) {
        if (!PlayEligibility.needsChosenOpponent(ac)) return options;
        List<Player> elig = PlayEligibility.eligibleOpponentsForAction(me, session, ac);
        if (elig.isEmpty()) return options;
        CardPlayOptions out = options;
        if (out.actionTargetPlayer() == null || !elig.contains(out.actionTargetPlayer())) {
            out = ActionTargetDialogs.chooseOpponent(stage, "Select Opponent",
                            "Please select which opponent to target with \"" + ac.getName() + "\".", elig)
                    .map(out::withActionTarget)
                    .orElse(null);
            if (out == null) return null;
        }

        Player target = out.actionTargetPlayer();

        return switch (ac.subTarget()) {
            case STEALABLE_PROPERTY -> {
                List<PropertyCard> cards = PropertyQuery.stealableSingleProperties(target);
                if (cards.isEmpty()) { yield out; }
                if (out.targetPropertyCard() != null && cards.contains(out.targetPropertyCard())) { yield out; }
                yield pickPropertyCard( "Sly Deal",
                        "Please select a non-full-set property to take from \"" + target.getName() + "\".",
                        cards, out);
            }
            case FORCED_DEAL_PROPERTIES -> {
                List<PropertyCard> mine = PropertyQuery.allTableProperties(me);
                List<PropertyCard> theirs = PropertyQuery.allTableProperties(target);
                if (mine.isEmpty() || theirs.isEmpty()) { yield out; }
                if (out.sourcePropertyCard() == null || !mine.contains(out.sourcePropertyCard())) {
                    out = pickPropertyCard( "Forced Deal",
                            "Please select a property card to give to \"" + target.getName() + "\".",
                            mine, out);
                    if (out == null) { yield null; }
                }
                if (out.targetPropertyCard() == null || !theirs.contains(out.targetPropertyCard())) {
                    out = pickPropertyCard( "Forced Deal",
                            "Please select a property card to take from \"" + target.getName() + "\".",
                            theirs, out);
                }
                yield out;
            }
            case MONOPOLY_GROUP -> {
                List<Property> groups = PropertyQuery.monopolyGroups(target);
                if (groups.isEmpty()) { yield out; }
                if (out.targetPropertyGroup() != null && groups.contains(out.targetPropertyGroup())) { yield out; }
                yield pickPropertyGroup( "Deal Breaker",
                        "Please select a complete property set to take from \"" + target.getName() + "\".",
                        groups, out);
            }
            default -> out;
        };
    }

    private CardPlayOptions pickPropertyCard(String title, String prompt,
                                              List<PropertyCard> cards, CardPlayOptions out) {
        return ActionTargetDialogs.choosePropertyCard(stage, title, prompt, cards)
                .map(out::withTargetPropertyCard)
                .orElse(null);
    }

    private CardPlayOptions pickPropertyGroup(String title, String prompt,
                                               List<Property> groups, CardPlayOptions out) {
        return ActionTargetDialogs.choosePropertyGroup(stage, title, prompt, groups)
                .map(out::withTargetPropertyGroup)
                .orElse(null);
    }
}