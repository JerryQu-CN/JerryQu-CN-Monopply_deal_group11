package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.PlayEligibility;
import com.example.monopoly_deal_game.logic.PropertyQuery;
import com.example.monopoly_deal_game.logic.RentCalculator;
import com.example.monopoly_deal_game.logic.RentRules;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.ActionCardDealBreaker;
import com.example.monopoly_deal_game.model.cards.ActionCardForcedDeal;
import com.example.monopoly_deal_game.model.cards.ActionCardSlyDeal;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;

import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

        if (ac instanceof ActionCardSlyDeal) {
            List<PropertyCard> cards = PropertyQuery.stealableSingleProperties(target);
            if (cards.isEmpty()) return out;
            if (out.targetPropertyCard() == null || !cards.contains(out.targetPropertyCard())) {
                out = pickPropertyCard(session, "Sly Deal",
                        "Please select a non-full-set property to take from \"" + target.getName() + "\".",
                        cards, out);
                if (out == null) return null;
            }
        } else if (ac instanceof ActionCardForcedDeal) {
            List<PropertyCard> mine = PropertyQuery.allTableProperties(me);
            List<PropertyCard> theirs = PropertyQuery.allTableProperties(target);
            if (mine.isEmpty() || theirs.isEmpty()) return out;
            if (out.sourcePropertyCard() == null || !mine.contains(out.sourcePropertyCard())) {
                out = pickPropertyCard(session, "Forced Deal",
                        "Please select a property card to give to \"" + target.getName() + "\".",
                        mine, out);
                if (out == null) return null;
            }
            if (out.targetPropertyCard() == null || !theirs.contains(out.targetPropertyCard())) {
                out = pickPropertyCard(session, "Forced Deal",
                        "Please select a property card to take from \"" + target.getName() + "\".",
                        theirs, out);
                if (out == null) return null;
            }
        } else if (ac instanceof ActionCardDealBreaker) {
            List<Property> groups = PropertyQuery.monopolyGroups(target);
            if (groups.isEmpty()) return out;
            if (out.targetPropertyGroup() == null || !groups.contains(out.targetPropertyGroup())) {
                out = pickPropertyGroup(session, "Deal Breaker",
                        "Please select a complete property set to take from \"" + target.getName() + "\".",
                        groups, out);
                if (out == null) return null;
            }
        }
        return out;
    }

    private CardPlayOptions pickPropertyCard(GameSession session, String title,
                                              String prompt, List<PropertyCard> cards,
                                              CardPlayOptions out) {
        GameState.Phase saved = session.getGameState().getPhase();
        session.getGameState().setPhase(GameState.Phase.WAITING_FOR_SELECTION);
        try {
            return ActionTargetDialogs.choosePropertyCard(stage, title, prompt, cards)
                    .map(out::withTargetPropertyCard)
                    .orElse(null);
        } finally {
            session.getGameState().setPhase(saved);
        }
    }

    private CardPlayOptions pickPropertyGroup(GameSession session, String title,
                                               String prompt, List<Property> groups,
                                               CardPlayOptions out) {
        GameState.Phase saved = session.getGameState().getPhase();
        session.getGameState().setPhase(GameState.Phase.WAITING_FOR_SELECTION);
        try {
            return ActionTargetDialogs.choosePropertyGroup(stage, title, prompt, groups)
                    .map(out::withTargetPropertyGroup)
                    .orElse(null);
        } finally {
            session.getGameState().setPhase(saved);
        }
    }
}