package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.Property;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.BankCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;
import com.example.monopoly_deal_game.model.cards.RuleCard;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 全牌种效果：钞票入账、物业落地、租金、行动牌；目标选择在 2 人局取对手，多人局取第一个非当前玩家。
 */
public class CardEffectExecutor {

    private final CardManager cardManager;

    public CardEffectExecutor(CardManager cardManager) {
        this.cardManager = Objects.requireNonNull(cardManager);
    }

    public boolean canUseActionEffect(ActionCard ac, GameSession session) {
        return canUseActionEffect(ac, session, CardPlayOptions.auto());
    }

    public boolean canUseActionEffect(ActionCard ac, GameSession session, CardPlayOptions opt) {
        return PlayEligibility.canUseActionEffect(ac, session, opt);
    }

    public boolean canUseActionEffectForUi(ActionCard ac, GameSession session) {
        return PlayEligibility.canUseActionEffectForUi(ac, session);
    }

    public void execute(Card card, GameSession session) {
        execute(card, session, CardPlayOptions.auto());
    }

    /**
     * 出牌总入口（{@link com.example.monopoly_deal_game.logic.GameLogic#playCard} 已从手牌移除 {@code card}）。
     * 作银行面值的路径由 {@link GameLogic} 直接入账，不会进入本方法。
     */
    public void execute(Card card, GameSession session, CardPlayOptions opt) {
        if (opt == null) {
            opt = CardPlayOptions.auto();
        }
        Player cur = session.getCurrentPlayer();
        if (cur == null) {
            throw new IllegalStateException("无当前玩家");
        }
        if (card instanceof BankCard b) {
            cur.getBank().addCard(b);
            return;
        }
        if (card instanceof PropertyCard pc) {
            PropertyPlayHelper.placePropertyCard(cur, pc);
            return;
        }
        if (card instanceof RuleCard) {
            session.discardCard(card);
            return;
        }
        if (card instanceof RentCard rc) {
            resolveRent(session, cur, rc, opt);
            session.discardCard(rc);
            return;
        }
        if (card instanceof ActionCard ac) {
            resolveAction(session, cur, ac, opt);
            return;
        }
        session.discardCard(card);
    }

    private void resolveRent(GameSession session, Player landlord, RentCard rc, CardPlayOptions opt) {
        GameState st = session.getGameState();
        boolean dbl = st.isDoubleNextRent();
        if (dbl) {
            st.setDoubleNextRent(false);
        }
        CardColor choice = opt.rentColorChoice();
        int base;
        if (rc.isWildRent()) {
            if (choice != null) {
                base = RentCalculator.rentOnColor(landlord, choice);
            } else {
                int best = 0;
                for (CardColor c : RentCalculator.tableColors()) {
                    best = Math.max(best, RentCalculator.rentOnColor(landlord, c));
                }
                base = best;
            }
        } else if (rc.getApplicableColors().size() >= 2) {
            if (choice != null) {
                base = RentCalculator.rentOnColor(landlord, choice);
            } else {
                base =
                        RentCalculator.bestRentForLandlord(
                                landlord, rc.getApplicableColors(), false);
            }
        } else if (rc.getApplicableColors().size() == 1) {
            CardColor only = rc.getApplicableColors().get(0);
            base = RentCalculator.rentOnColor(landlord, only);
        } else {
            base = 0;
        }
        int amount = dbl ? base * 2 : base;
        Player victim = RentRules.resolvedRentPayer(landlord, session, opt);
        if (victim != null
                && (victim.equals(landlord) || !session.getPlayers().contains(victim))) {
            victim = null;
        }
        if (victim != null && amount > 0) {
            String label =
                    landlord.getName()
                            + " 打出租金向你收取 "
                            + amount
                            + "M"
                            + (dbl ? "（含双倍租金）" : "");
            PaymentService.payFromTo(
                    victim,
                    landlord,
                    amount,
                    session,
                    landlord,
                    label);
        }
    }

    private void resolveAction(GameSession session, Player actor, ActionCard ac, CardPlayOptions opt) {
        if (opt == null) {
            opt = CardPlayOptions.auto();
        }
        switch (ac.getActionType()) {
            case PASS_GO -> {
                for (int i = 0; i < 2; i++) {
                    Card d = cardManager.drawOne(session);
                    if (d != null) {
                        actor.getHand().addCard(d);
                    }
                }
                session.discardCard(ac);
            }
            case DOUBLE_RENT -> {
                session.getGameState().setDoubleNextRent(true);
                session.discardCard(ac);
            }
            case JUST_SAY_NO -> {
                actor.getBank().addCard(ac);
            }
            case ITS_MY_BIRTHDAY -> {
                for (Player p : new ArrayList<>(session.getPlayers())) {
                    if (!p.equals(actor)) {
                        String label =
                                actor.getName()
                                        + " 使用「生日」（It's My Birthday）：请支付 $2M。";
                        PaymentService.payFromTo(p, actor, 2, session, actor, label);
                    }
                }
                session.discardCard(ac);
            }
            case DEBT_COLLECTOR -> {
                Player victim =
                        PlayEligibility.resolvedActionTarget(
                                actor, session, opt, ActionCard.ActionType.DEBT_COLLECTOR);
                if (victim != null) {
                    String label = actor.getName() + " 使用「收债专员」向你收取 $5M。";
                    PaymentService.payFromTo(victim, actor, 5, session, actor, label);
                }
                session.discardCard(ac);
            }
            case SLY_DEAL -> {
                Player victimPick =
                        PlayEligibility.resolvedActionTarget(
                                actor, session, opt, ActionCard.ActionType.SLY_DEAL);
                PropertyCard stolen = opt.targetPropertyCard() != null
                        ? opt.targetPropertyCard()
                        : (victimPick != null ? firstTableProperty(victimPick) : null);
                if (stolen == null) {
                    throw new IllegalStateException("没有可偷的对手的物业牌");
                }
                Player victimOwner = ownerOfPropertyCard(session, stolen);
                if (victimOwner == null) {
                    throw new IllegalStateException("找不到被偷物业牌所属玩家");
                }
                if (JustSayNoMediator.tryBlockAgainstPlayer(
                        victimOwner,
                        actor,
                        session,
                        actor.getName() + " 尝试用「偷偷交易」拿走你桌上的一张物业。")) {
                    session.discardCard(ac);
                    return;
                }
                PropertyPlayHelper.removePropertyCardFromBoard(victimOwner, stolen, session);
                PropertyPlayHelper.placePropertyCard(actor, stolen);
                session.discardCard(ac);
            }
            case FORCE_DEAL -> {
                PropertyCard mine = opt.sourcePropertyCard() != null ? opt.sourcePropertyCard() : firstTableProperty(actor);
                Player opp =
                        PlayEligibility.resolvedActionTarget(
                                actor, session, opt, ActionCard.ActionType.FORCE_DEAL);
                PropertyCard theirs = opt.targetPropertyCard() != null ? opt.targetPropertyCard() : (opp != null ? firstTableProperty(opp) : null);
                if (mine == null || opp == null || theirs == null) {
                    throw new IllegalStateException("强制交换需要双方场地上至少各有一张物业牌");
                }
                if (JustSayNoMediator.tryBlockAgainstPlayer(
                        opp,
                        actor,
                        session,
                        actor.getName() + " 使用「被迫交易」要强换你走桌上的一张物业。")) {
                    session.discardCard(ac);
                    return;
                }
                PropertyPlayHelper.removePropertyCardFromBoard(actor, mine, session);
                PropertyPlayHelper.removePropertyCardFromBoard(opp, theirs, session);
                PropertyPlayHelper.placePropertyCard(actor, theirs);
                PropertyPlayHelper.placePropertyCard(opp, mine);
                session.discardCard(ac);
            }
            case DEAL_BREAKER -> {
                Player targetPlayer =
                        PlayEligibility.resolvedActionTarget(
                                actor, session, opt, ActionCard.ActionType.DEAL_BREAKER);
                Property complete = opt.targetPropertyGroup() != null ? opt.targetPropertyGroup() : (targetPlayer != null ? firstMonopolyOfPlayer(targetPlayer) : null);
                if (complete == null) {
                    throw new IllegalStateException("没有可夺走的完整成套物业");
                }
                Player from = complete.getOwner();
                if (from == null) {
                    for (Player probe : session.getPlayers()) {
                        if (probe.getProperties().contains(complete)) {
                            from = probe;
                            break;
                        }
                    }
                }
                if (from != null
                        && JustSayNoMediator.tryBlockAgainstPlayer(
                                from,
                                actor,
                                session,
                                actor.getName()
                                        + " 使用「夺产」要强夺你一整套已垄断物业（含其上房屋）。")) {
                    session.discardCard(ac);
                    return;
                }
                PropertyPlayHelper.transferPropertyGroup(from, actor, complete, session);
                session.discardCard(ac);
            }
            case HOUSE -> {
                Property row = findHouseTarget(actor, true);
                if (row == null || !row.addBuildingCard(ac)) {
                    throw new IllegalStateException("必须先有完整成套且尚未放置房屋");
                }
            }
            case HOTEL -> {
                Property row = findHouseTarget(actor, false);
                if (row == null || !row.addBuildingCard(ac)) {
                    throw new IllegalStateException("须先在成套上放置房屋后才能建旅馆");
                }
            }
        }
    }

    private static Property findHouseTarget(Player actor, boolean forHouse) {
        for (Property ps : actor.getProperties()) {
            if (!ps.isMonopoly()) {
                continue;
            }
            boolean hasH =
                    ps.getBuildingCards().stream()
                            .anyMatch(
                                    c ->
                                            c instanceof ActionCard x
                                                    && x.getActionType() == ActionCard.ActionType.HOUSE);
            boolean hasT =
                    ps.getBuildingCards().stream()
                            .anyMatch(
                                    c ->
                                            c instanceof ActionCard x
                                                    && x.getActionType() == ActionCard.ActionType.HOTEL);
            if (forHouse && !hasH) {
                return ps;
            }
            if (!forHouse && hasH && !hasT) {
                return ps;
            }
        }
        return null;
    }

    private static Property firstMonopolyOfPlayer(Player p) {
        if (p == null) {
            return null;
        }
        for (Property row : p.getProperties()) {
            if (row.isMonopoly()) {
                return row;
            }
        }
        return null;
    }

    private static PropertyCard firstTableProperty(Player p) {
        for (Property row : p.getProperties()) {
            if (!row.getCards().isEmpty()) {
                return row.getCards().get(0);
            }
        }
        return null;
    }

    private static Player ownerOfPropertyCard(GameSession session, PropertyCard pc) {
        for (Player p : session.getPlayers()) {
            for (Property row : p.getProperties()) {
                if (row.getCards().contains(pc)) {
                    return p;
                }
            }
        }
        return null;
    }

    public void execute(GameSession session, ActionCard card, Player user, Player target) {
        execute(card, session, CardPlayOptions.auto().withActionTarget(target));
    }
}
