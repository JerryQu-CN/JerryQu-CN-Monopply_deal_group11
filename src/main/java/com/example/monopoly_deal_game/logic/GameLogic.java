package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.state.ActionState;
import com.example.monopoly_deal_game.game.state.ActionStatePlayerTurn;
import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.game.state.GameState;
import com.example.monopoly_deal_game.logic.GameConfig;
import com.example.monopoly_deal_game.logic.payment.RentRules;
import com.example.monopoly_deal_game.model.PlayedCardSnapshot;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.ActionCardJustSayNo;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.CardType;
import com.example.monopoly_deal_game.model.cards.RentCard;

import java.util.Collection;
import java.util.List;

/**
 * Core game rules: initialization, drawing, play flow, and win/loss determination.
 * Aligned with the GameState / MDServer game lifecycle from Monopoly-Deal-main.
 */
public class GameLogic {

    private final CardManager cardManager = new CardManager();
    private final TurnManager turnManager = new TurnManager();
    private final CardEffectExecutor effectExecutor;

    public GameLogic() {
        this.effectExecutor = new CardEffectExecutor(cardManager);
    }

    /** Initialize the game: shuffle, deal cards, set the first player. */
    public void initGame(GameSession session) {
        CardFactory factory = new CardFactory();
        List<Card> deck = new java.util.ArrayList<>(factory.createFullDeck());
        deck.removeIf(c -> c.getCardType() == CardType.RULE);
        cardManager.initDeck(session, deck);

        List<Player> players = session.getPlayers();
        for (Player p : players) {
            p.clearPlayedCardsDisplay();
            for (int i = 0; i < GameConfig.INITIAL_HAND_SIZE; i++) {
                Card c = cardManager.drawOne(session);
                if (c != null) p.getHand().add(c);
            }
        }

        session.getGameState().setCurrentPlayerIndex(0);
        turnManager.beginTurn(session);
    }

    /** Draw at turn start: 5 cards if hand is empty, otherwise 2; draw only once per turn. */
    public void drawCard(GameSession session) {
        Player current = session.getCurrentPlayer();
        GameState state = session.getGameState();
        ActionStatePlayerTurn ts = state.getTurnState();
        if (ts == null || !ts.isDrawing() || current == null) return;

        int count = current.getHand().isEmpty()
                ? GameConfig.DRAW_WHEN_HAND_EMPTY
                : GameConfig.DRAW_WHEN_HAND_NON_EMPTY;
        for (int i = 0; i < count; i++) {
            Card c = cardManager.drawOne(session);
            if (c != null) current.getHand().add(c);
        }
        ts.setDrawn();
    }

    /**
     * Main entry point for playing a card.
     * @return false when: haven't drawn yet, already played max cards this turn,
     *         card not found in hand, or effect preconditions are not met
     */
    public boolean playCard(GameSession session, Card card) {
        return playCard(session, card, CardPlayOptions.auto());
    }

    public boolean playCard(GameSession session, Card card, CardPlayOptions options) {
        if (options == null) options = CardPlayOptions.auto();

        Player owner = findCardOwner(session, card);
        if (owner == null) return false;

        Player currentPlayer = session.getCurrentPlayer();
        boolean isReaction = currentPlayer != null && !owner.equals(currentPlayer);

        boolean isJSN = card instanceof ActionCardJustSayNo;

        if (!canPlayerActNow(owner, session, isReaction, isJSN, options.asBankMoney())) return false;
        if (!isCardEligible(card, owner, session, options, isJSN)) return false;

        boolean fromHand = owner.getHand().getCards().contains(card);
        if (fromHand) {
            owner.getHand().removeCard(card);
        } else if (!owner.getBank().removeCard(card)) {
            return false;
        }
        try {
            if (options.asBankMoney()) {
                owner.getBank().addCard(card);
            } else if (options.jsnBlocked()) {
                session.discardCard(card);
            } else if (isJSN) {
                GameState gs = session.getGameState();
                ActionState as = gs.getActionState();
                if (as != null && as != gs.getTurnState()) {
                    Player refuser = owner;
                    Player target = as.getActionOwner();
                    as.refuse(refuser, target);
                }
                session.discardCard(card);
            } else {
                effectExecutor.execute(card, session, options);
            }
            if (shouldRecordOnPlayTable(card) && !isReaction) {
                owner.recordPlayedCardForDisplay(
                        new PlayedCardSnapshot(card.getName(), CardImageMapper.imageFileFor(card)));
            }
            turnManager.onCardPlayed(session, card.isCountsTowardLimit() && !isReaction && !isJSN);
            return true;
        } catch (RuntimeException ex) {
            if (fromHand) {
                owner.getHand().addCard(card);
            } else {
                owner.getBank().addCard(card);
            }
            throw ex;
        }
    }

    private Player findCardOwner(GameSession session, Card card) {
        for (Player p : session.getPlayers()) {
            if (p.getHand().getCards().contains(card)) return p;
            if (p.getBank().getCards().contains(card)) return p;
        }
        return null;
    }

    private boolean canPlayerActNow(Player owner, GameSession session,
                                     boolean isReaction, boolean isJSN, boolean asBankMoney) {
        if (!isJSN) {
            GameState gs = session.getGameState();
            if (!gs.isPlayerFocused(owner) && !asBankMoney && !isReaction) return false;
            return isReaction || turnManager.canPlayMore(session);
        }
        GameState gs = session.getGameState();
        ActionState as = gs.getActionState();
        return as != null && as != gs.getTurnState() && as.canRefuseAny(owner);
    }

    private boolean isCardEligible(Card card, Player owner, GameSession session,
                                    CardPlayOptions options, boolean isJSN) {
        CardType ct = card.getCardType();
        if (options.asBankMoney()) {
            if (ct == CardType.RULE) return false;
            if (ct == CardType.PROPERTY && !GameConfig.CAN_BANK_PROPERTY_CARDS) return false;
            return ct != CardType.ACTION || GameConfig.CAN_BANK_ACTION_CARDS;
        }
        if (options.jsnBlocked() || isJSN) return true;
        if (ct == CardType.ACTION && !effectExecutor.canUseActionEffect((ActionCard) card, session, options)) return false;
        if (ct == CardType.RENT) {
            RentCard rc = (RentCard) card;
            Player pay = RentRules.resolvedRentPayer(owner, session, options);
            return RentRules.canUseRentEffect(rc, owner, options.rentColorChoice(), pay, session);
        }
        return true;
    }

    private static boolean shouldRecordOnPlayTable(Card card) {
        return switch (card.getCardType()) {
            case CURRENCY, PROPERTY -> true;
            default -> false;
        };
    }

    /** End the turn: if hand > 7 cards, enter the discard phase. */
    public void endTurn(GameSession session) {
        Player current = session.getCurrentPlayer();
        GameState state = session.getGameState();
        if (current == null) return;

        ActionStatePlayerTurn ts = state.getTurnState();
        if (ts != null) {
            // Force moves to 0 so updatePhase will transition to DISCARD if needed
            ts.setMoves(0);
        }

        if (current.getHand().size() > GameConfig.MAX_HAND_SIZE_END_TURN) {
            return; // Already in DISCARD phase from setMoves(0)
        }
        advanceToNextPlayer(session);
    }

    /** Discard phase: discard selected hand cards; once hand is <= 7, advance to next player. */
    public void discardFromHandChosen(GameSession session, Collection<Card> chosen) {
        if (chosen == null || chosen.isEmpty()) return;
        Player cur = session.getCurrentPlayer();
        GameState state = session.getGameState();
        ActionStatePlayerTurn ts = state.getTurnState();
        if (cur == null || ts == null || !ts.isDiscarding()) return;
        for (Card c : chosen) {
            if (c == null || !cur.getHand().getCards().contains(c)) continue;
            if (cur.getHand().removeCard(c)) session.discardCard(c);
        }
        ts.updatePhase(); // Re-evaluate: if no longer too many cards, goes back to PLAY
        if (!ts.isDiscarding()) {
            advanceToNextPlayer(session);
        }
    }

    private void advanceToNextPlayer(GameSession session) {
        if (checkGameOver(session)) {
            session.getGameState().setGameOver(true);
            return;
        }
        List<Player> players = session.getPlayers();
        if (players.isEmpty()) return;
        int nextIndex = (session.getGameState().getCurrentPlayerIndex() + 1) % players.size();
        session.getGameState().setCurrentPlayerIndex(nextIndex);
        turnManager.beginTurn(session);
    }

    /** Win/loss check: any player reaches {@link GameConfig#FULL_SETS_TO_WIN} complete property sets. */
    public boolean checkGameOver(GameSession session) {
        for (Player p : session.getPlayers()) {
            if (p.getFullSetCount() >= GameConfig.FULL_SETS_TO_WIN) {
                session.getGameState().setGameOver(true);
                return true;
            }
        }
        return false;
    }

    public CardManager getCardManager() { return cardManager; }
    public TurnManager getTurnManager() { return turnManager; }
    public CardEffectExecutor getEffectExecutor() { return effectExecutor; }
}