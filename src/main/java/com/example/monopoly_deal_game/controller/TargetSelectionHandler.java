package com.example.monopoly_deal_game.controller;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.logic.*;
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
import java.util.function.Consumer;

public class TargetSelectionHandler {
    private final Stage stage;
    private final AtomicBoolean dialogBusy;

    public TargetSelectionHandler(Stage stage, AtomicBoolean dialogBusy) {
        this.stage = stage;
        this.dialogBusy = dialogBusy;
    }

    public CardPlayOptions mergeTargets(GameSession session, Card card,
                                         CardPlayOptions options,
                                         Consumer<String> feedback,
                                         Runnable refreshUi,
                                         NetworkSyncHelper networkSync,
                                         HandCardPicker handCardPicker) {
        if (session == null || options == null) return options;
        Player me = session.getCurrentPlayer();
        if (me == null || options.asBankMoney()) return options;

        if (card instanceof RentCard rc) {
            return mergeRentTargets(session, rc, options, me);
        }
        if (card instanceof ActionCard ac) {
            return mergeActionTargets(session, ac, options, me, feedback, refreshUi,
                    networkSync, handCardPicker);
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
        return ActionTargetDialogs.chooseOpponent(stage, "收取租金",
                        "请选择向哪位玩家收取这条租金（预览约 " + preview + "M）。", renters)
                .map(p -> CardPlayOptions.rentWithColorAndPlayer(cap, p))
                .orElse(null);
    }

    private CardPlayOptions mergeActionTargets(GameSession session, ActionCard ac,
                                                CardPlayOptions options, Player me,
                                                Consumer<String> feedback, Runnable refreshUi,
                                                NetworkSyncHelper networkSync,
                                                HandCardPicker handCardPicker) {
        if (!PlayEligibility.needsChosenOpponent(ac)) return options;
        List<Player> elig = PlayEligibility.eligibleOpponentsForAction(me, session, ac);
        if (elig.isEmpty()) return options;
        CardPlayOptions out = options;
        if (out.actionTargetPlayer() == null || !elig.contains(out.actionTargetPlayer())) {
            out = ActionTargetDialogs.chooseOpponent(stage, "选择对手",
                            "请选择「" + ac.getName() + "」对哪一位对手生效。", elig)
                    .map(out::withActionTarget)
                    .orElse(null);
            if (out == null) return null;
        }

        Player target = out.actionTargetPlayer();
        if (target != null && JustSayNoHandler.playerHasJustSayNo(target)) {
            GameState.Phase prev = session.getGameState().getPhase();
            session.getGameState().setPhase(GameState.Phase.WAITING_FOR_SELECTION);
            try {
                if (JustSayNoMediator.tryBlockAgainstPlayer(target, me, session,
                        me.getName() + " 尝试对你使用「" + ac.getName() + "」。")) {
                    handCardPicker.setSelectedHandCard(null);
                    feedback.accept(target.getName() + " 使用了 Just Say No，效果已被抵消！");
                    networkSync.publishSessionChange(session);
                    session.getGameState().setPhase(prev);
                    refreshUi.run();
                    return out.withJsnBlocked();
                }
            } finally {
                session.getGameState().setPhase(prev);
            }
        }

        if (ac instanceof ActionCardSlyDeal) {
            List<PropertyCard> cards = PropertyQuery.stealableSingleProperties(target);
            if (cards.isEmpty()) return out;
            if (out.targetPropertyCard() == null || !cards.contains(out.targetPropertyCard())) {
                out = pickPropertyCard(session, "偷偷交易",
                        "请选择要从「" + target.getName() + "」处拿走的一张非满套房产。",
                        cards, out);
                if (out == null) return null;
            }
        } else if (ac instanceof ActionCardForcedDeal) {
            List<PropertyCard> mine = PropertyQuery.allTableProperties(me);
            List<PropertyCard> theirs = PropertyQuery.allTableProperties(target);
            if (mine.isEmpty() || theirs.isEmpty()) return out;
            if (out.sourcePropertyCard() == null || !mine.contains(out.sourcePropertyCard())) {
                out = pickPropertyCard(session, "被迫交易",
                        "请选择你要交给「" + target.getName() + "」的一张房产。",
                        mine, out);
                if (out == null) return null;
            }
            if (out.targetPropertyCard() == null || !theirs.contains(out.targetPropertyCard())) {
                out = pickPropertyCard(session, "被迫交易",
                        "请选择要从「" + target.getName() + "」处换来的一张房产。",
                        theirs, out);
                if (out == null) return null;
            }
        } else if (ac instanceof ActionCardDealBreaker) {
            List<Property> groups = PropertyQuery.monopolyGroups(target);
            if (groups.isEmpty()) return out;
            if (out.targetPropertyGroup() == null || !groups.contains(out.targetPropertyGroup())) {
                out = pickPropertyGroup(session, "夺产",
                        "请选择要从「" + target.getName() + "」处夺走的一整套房产。",
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