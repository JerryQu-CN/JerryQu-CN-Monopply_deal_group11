package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.ActionStateRent;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.GameConfig;
import com.example.monopoly_deal_game.logic.payment.PaymentRequest;
import com.example.monopoly_deal_game.logic.payment.PaymentService;
import com.example.monopoly_deal_game.logic.payment.RentCalculator;
import com.example.monopoly_deal_game.logic.payment.RentRules;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardColor;
import com.example.monopoly_deal_game.model.cards.RentCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Card effect executor, aligned with the doPlay logic of each CardAction in Monopoly-Deal-main.
 * Action cards requiring a target player are pushed into an ActionState to block other players.
 */
public class CardEffectExecutor {

    private final CardManager cardManager;

    public CardEffectExecutor(CardManager cardManager) {
        this.cardManager = Objects.requireNonNull(cardManager);
    }

    // ---- Playability checks ----

    public boolean canUseActionEffect(ActionCard ac, GameSession session) {
        return canUseActionEffect(ac, session, CardPlayOptions.auto());
    }

    public boolean canUseActionEffect(ActionCard ac, GameSession session, CardPlayOptions opt) {
        return PlayEligibility.canUseActionEffect(ac, session, opt);
    }

    public boolean canUseActionEffectForUi(ActionCard ac, GameSession session) {
        return PlayEligibility.canUseActionEffectForUi(ac, session);
    }

    // ---- Main play entry point ----

    public void execute(Card card, GameSession session) {
        execute(card, session, CardPlayOptions.auto());
    }

    public void execute(Card card, GameSession session, CardPlayOptions opt) {
        if (opt == null) opt = CardPlayOptions.auto();
        Player cur = session.getCurrentPlayer();
        if (cur == null) throw new IllegalStateException("No current player");
        card.executePlay(cur, session, opt);
    }

    // ---- Rent resolution ----

    public static void resolveRent(GameSession session, Player landlord, RentCard rc, CardPlayOptions opt) {
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
                PaymentService.payFromTo(new PaymentRequest(player, landlord, finalAmount, session, label));
            });
            session.getGameState().addActionState(rentState);
        }
    }

    public static List<Player> resolveRentVictims(Player landlord, GameSession session, RentCard rc, CardPlayOptions opt) {
        List<Player> others = RentRules.rentersExcludingLandlord(landlord, session);
        if (others.isEmpty()) return List.of();

        Player pick = opt != null ? opt.actionTargetPlayer() : null;
        if (pick != null && others.contains(pick)) {
            return List.of(pick);
        }

        boolean chargesAll = rc.isWildRent()
                ? GameConfig.MULTI_COLOR_RENT_CHARGES_ALL
                : (rc.getApplicableColors().size() >= 2 ? GameConfig.TWO_COLOR_RENT_CHARGES_ALL : false);

        if (chargesAll) {
            return new ArrayList<>(others);
        }

        return List.of();
    }
}