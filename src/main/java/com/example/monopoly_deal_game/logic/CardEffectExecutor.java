package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.ActionStateRent;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.model.Player;
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
            ac.doPlay(cur, session, opt);
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

    /** 兼容旧接口 */
    public void execute(GameSession session, ActionCard card, Player user, Player target) {
        execute(card, session, CardPlayOptions.auto().withActionTarget(target));
    }
}