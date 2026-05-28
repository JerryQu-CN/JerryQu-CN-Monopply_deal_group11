package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.ActionStatePlayerTargeted;
import com.example.monopoly_deal_game.game.model.ActionStateRent;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.game.rules.GameConfig;
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
import java.util.List;
import java.util.Objects;

/**
 * 卡牌效果执行器，对齐 Monopoly-Deal-main 中各 CardAction 的 doPlay 逻辑。
 * 需要目标玩家的行动牌会推入 ActionState 以阻塞其他玩家。
 */
public class CardEffectExecutor {

    private final CardManager cardManager;

    public CardEffectExecutor(CardManager cardManager) {
        this.cardManager = Objects.requireNonNull(cardManager);
    }

    // ---- 可执行性判断 ----

    public boolean canUseActionEffect(ActionCard ac, GameSession session) {
        return canUseActionEffect(ac, session, CardPlayOptions.auto());
    }

    public boolean canUseActionEffect(ActionCard ac, GameSession session, CardPlayOptions opt) {
        return PlayEligibility.canUseActionEffect(ac, session, opt);
    }

    public boolean canUseActionEffectForUi(ActionCard ac, GameSession session) {
        return PlayEligibility.canUseActionEffectForUi(ac, session);
    }

    // ---- 出牌总入口 ----

    public void execute(Card card, GameSession session) {
        execute(card, session, CardPlayOptions.auto());
    }

    public void execute(Card card, GameSession session, CardPlayOptions opt) {
        if (opt == null) opt = CardPlayOptions.auto();
        Player cur = session.getCurrentPlayer();
        if (cur == null) throw new IllegalStateException("无当前玩家");

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

    // ---- 租金结算 ----

    private void resolveRent(GameSession session, Player landlord, RentCard rc, CardPlayOptions opt) {
        GameState st = session.getGameState();
        int dblCount = st.getDoubleRentCount();
        st.setDoubleRentCount(0);
        st.setDoubleNextRent(false);

        CardColor choice = opt.rentColorChoice();
        int base;
        if (rc.isWildRent()) {
            if (choice != null) {
                base = RentCalculator.rentOnColor(landlord, choice);
            } else {
                int best = 0;
                for (CardColor c : CardColor.TABLE_COLORS) {
                    best = Math.max(best, RentCalculator.rentOnColor(landlord, c));
                }
                base = best;
            }
        } else if (rc.getApplicableColors().size() >= 2) {
            if (choice != null) {
                base = RentCalculator.rentOnColor(landlord, choice);
            } else {
                base = RentCalculator.bestRentForLandlord(landlord, rc.getApplicableColors(), false);
            }
        } else if (rc.getApplicableColors().size() == 1) {
            CardColor only = rc.getApplicableColors().get(0);
            base = RentCalculator.rentOnColor(landlord, only);
        } else {
            base = 0;
        }

        int amount = base;
        if (dblCount > 0) {
            switch (GameConfig.DOUBLE_RENT_POLICY) {
                case MULTIPLY -> amount = base * (int) Math.pow(2, dblCount);
                case ADD -> amount = base * (dblCount + 1);
            }
        }

        List<Player> victims = resolveRentVictims(landlord, session, rc, opt);
        if (!victims.isEmpty() && amount > 0) {
            final int finalAmount = amount;
            ActionStateRent rentState = new ActionStateRent(landlord, victims, finalAmount);
            rentState.setOnAccepted(player -> {
                String label = landlord.getName() + " charges " + finalAmount + "M rent.";
                PaymentService.payFromTo(player, landlord, finalAmount, session, landlord, label);
            });
            session.getGameState().addActionState(rentState);
        }
    }

    private List<Player> resolveRentVictims(Player landlord, GameSession session, RentCard rc, CardPlayOptions opt) {
        List<Player> others = RentRules.rentersExcludingLandlord(landlord, session);
        if (others.isEmpty()) return List.of();

        Player pick = opt != null ? opt.actionTargetPlayer() : null;
        if (pick != null && others.contains(pick)) {
            return List.of(pick);
        }

        boolean chargesAll = rc.isWildRent()
                ? GameConfig.MULTI_COLOR_RENT_CHARGES_ALL
                : (rc.getApplicableColors().size() >= 2 ? GameConfig.TWO_COLOR_RENT_CHARGES_ALL : false);

        if (chargesAll && others.size() > 1) {
            return new ArrayList<>(others);
        }

        return List.of();
    }

    // ---- 行动牌结算 ----

    private void resolveAction(GameSession session, Player actor, ActionCard ac, CardPlayOptions opt) {
        if (opt == null) opt = CardPlayOptions.auto();
        switch (ac.getActionType()) {
            case PASS_GO -> {
                for (int i = 0; i < 2; i++) {
                    Card d = cardManager.drawOne(session);
                    if (d != null) actor.getHand().addCard(d);
                }
                session.discardCard(ac);
            }
            case DOUBLE_RENT -> {
                GameState st = session.getGameState();
                st.setDoubleNextRent(true);
                st.setDoubleRentCount(st.getDoubleRentCount() + 1);
                session.discardCard(ac);
            }
            case JUST_SAY_NO -> session.discardCard(ac);
            case ITS_MY_BIRTHDAY -> {
                List<Player> targets = new ArrayList<>();
                for (Player p : new ArrayList<>(session.getPlayers())) {
                    if (!p.equals(actor)) targets.add(p);
                }
                if (!targets.isEmpty()) {
                    ActionStateRent birthdayState = new ActionStateRent(actor, targets, 2);
                    birthdayState.setOnAccepted(player -> {
                        String label = actor.getName() + " plays Birthday: pay 2M.";
                        PaymentService.payFromTo(player, actor, 2, session, actor, label);
                    });
                    birthdayState.setStatus(actor.getName() + " played It's My Birthday!");
                    session.getGameState().addActionState(birthdayState);
                }
                session.discardCard(ac);
            }
            case DEBT_COLLECTOR -> {
                Player victim = PlayEligibility.resolvedActionTarget(actor, session, opt,
                        ActionCard.ActionType.DEBT_COLLECTOR);
                if (victim != null) {
                    ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(actor, victim,
                            actor.getName() + " used Debt Collector against " + victim.getName());
                    state.setOnAccepted(player -> {
                        String label = actor.getName() + " plays Debt Collector: pay 5M.";
                        PaymentService.payFromTo(player, actor, 5, session, actor, label);
                    });
                    session.getGameState().addActionState(state);
                }
                session.discardCard(ac);
            }
            case SLY_DEAL -> {
                Player victimPick = PlayEligibility.resolvedActionTarget(actor, session, opt,
                        ActionCard.ActionType.SLY_DEAL);
                PropertyCard stolen = opt.targetPropertyCard() != null
                        ? opt.targetPropertyCard()
                        : (victimPick != null ? PropertyQuery.firstStealableProperty(victimPick) : null);
                if (stolen == null) throw new IllegalStateException("没有可偷的对手的物业牌");
                Player victimOwner = ownerOfPropertyCard(session, stolen);
                if (victimOwner == null) throw new IllegalStateException("找不到被偷物业牌所属玩家");

                ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(actor, victimOwner,
                        actor.getName() + " used Sly Deal against " + victimOwner.getName());
                state.setOnAccepted(player -> {
                    PropertyPlayHelper.removePropertyCardFromBoard(victimOwner, stolen, session);
                    PropertyPlayHelper.placePropertyCard(actor, stolen);
                });
                session.getGameState().addActionState(state);
                session.discardCard(ac);
            }
            case FORCE_DEAL -> {
                PropertyCard mine = opt.sourcePropertyCard() != null
                        ? opt.sourcePropertyCard() : PropertyQuery.firstTableProperty(actor);
                Player opp = PlayEligibility.resolvedActionTarget(actor, session, opt,
                        ActionCard.ActionType.FORCE_DEAL);
                PropertyCard theirs = opt.targetPropertyCard() != null
                        ? opt.targetPropertyCard() : (opp != null ? PropertyQuery.firstTableProperty(opp) : null);
                if (mine == null || opp == null || theirs == null)
                    throw new IllegalStateException("强制交换需要双方场地上至少各有一张物业牌");

                ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(actor, opp,
                        actor.getName() + " used Forced Deal against " + opp.getName());
                state.setOnAccepted(player -> {
                    PropertyPlayHelper.removePropertyCardFromBoard(actor, mine, session);
                    PropertyPlayHelper.removePropertyCardFromBoard(opp, theirs, session);
                    PropertyPlayHelper.placePropertyCard(actor, theirs);
                    PropertyPlayHelper.placePropertyCard(opp, mine);
                });
                session.getGameState().addActionState(state);
                session.discardCard(ac);
            }
            case DEAL_BREAKER -> {
                Player targetPlayer = PlayEligibility.resolvedActionTarget(actor, session, opt,
                        ActionCard.ActionType.DEAL_BREAKER);
                Property complete = opt.targetPropertyGroup() != null
                        ? opt.targetPropertyGroup()
                        : (targetPlayer != null ? PropertyQuery.firstMonopolyOfPlayer(targetPlayer) : null);
                if (complete == null) throw new IllegalStateException("没有可夺走的完整成套物业");
                Player from = complete.getOwner();
                if (from == null) {
                    for (Player probe : session.getPlayers()) {
                        if (probe.getProperties().contains(complete)) { from = probe; break; }
                    }
                }

                Player finalFrom = from;
                Property finalComplete = complete;
                ActionStatePlayerTargeted state = new ActionStatePlayerTargeted(actor, from,
                        actor.getName() + " used Deal Breaker against " + from.getName());
                state.setOnAccepted(player -> {
                    if (GameConfig.DEAL_BREAKERS_DISCARD_SETS) {
                        List<PropertyCard> propCards = new ArrayList<>(finalComplete.getCards());
                        for (PropertyCard pc : propCards) {
                            PropertyPlayHelper.removePropertyCardFromBoard(finalFrom, pc, session);
                            session.discardCard(pc);
                        }
                        List<Card> buildings = finalComplete.takeAllBuildings();
                        for (Card b : buildings) session.discardCard(b);
                    } else {
                        PropertyPlayHelper.transferPropertyGroup(finalFrom, actor, finalComplete, session);
                    }
                });
                session.getGameState().addActionState(state);
                session.discardCard(ac);
            }
            case HOUSE -> {
                Property row = PropertyQuery.findHouseTarget(actor, true);
                if (row == null || !row.addBuildingCard(ac))
                    throw new IllegalStateException("必须先有完整成套且尚未放置房屋");
            }
            case HOTEL -> {
                Property row = PropertyQuery.findHouseTarget(actor, false);
                if (row == null || !row.addBuildingCard(ac))
                    throw new IllegalStateException("须先在成套上放置房屋后才能建旅馆");
            }
        }
    }

    private static Player ownerOfPropertyCard(GameSession session, PropertyCard pc) {
        for (Player p : session.getPlayers()) {
            for (Property row : p.getProperties()) {
                if (row.getCards().contains(pc)) return p;
            }
        }
        return null;
    }

    /** 兼容旧接口 */
    public void execute(GameSession session, ActionCard card, Player user, Player target) {
        execute(card, session, CardPlayOptions.auto().withActionTarget(target));
    }
}