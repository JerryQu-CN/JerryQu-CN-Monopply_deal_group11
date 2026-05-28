package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.ActionState;
import com.example.monopoly_deal_game.game.model.ActionStatePlayerTurn;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.game.rules.GameConfig;
import com.example.monopoly_deal_game.model.PlayedCardSnapshot;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;
import com.example.monopoly_deal_game.model.cards.RuleCard;

import java.util.Collection;
import java.util.List;

/**
 * 游戏规则中枢：初始化、摸牌、出牌主流程及胜负判定。
 * 对齐 Monopoly-Deal-main 中 GameState / MDServer 的游戏生命周期。
 */
public class GameLogic {

    private final CardManager cardManager = new CardManager();
    private final TurnManager turnManager = new TurnManager();
    private final CardEffectExecutor effectExecutor;

    public GameLogic() {
        this.effectExecutor = new CardEffectExecutor(cardManager);
    }

    /** 初始化游戏：洗牌、发牌、设定首轮玩家。 */
    public void initGame(GameSession session) {
        CardFactory factory = new CardFactory();
        List<Card> deck = new java.util.ArrayList<>(factory.createFullDeck());
        deck.removeIf(c -> c instanceof RuleCard);
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

    /** 回合开始摸牌：空手 5 张，否则 2 张；每回合仅允许摸一次。 */
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
     * 出牌主入口。
     * @return false 时常见原因：尚未摸牌、本回合已打满、手牌中无此牌引用、效果前提不满足
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

        boolean isJSN = card instanceof ActionCard ac
                && ac.getActionType() == ActionCard.ActionType.JUST_SAY_NO;

        // Check if player is allowed to act right now
        if (!isJSN) {
            GameState gs = session.getGameState();
            // Non-JSN cards can only be played when focused (your turn, no action state override)
            if (!gs.isPlayerFocused(owner) && !options.asBankMoney()) {
                if (!isReaction) return false;
            }
            if (!isReaction && !turnManager.canPlayMore(session)) return false;
        } else {
            // JSN can only be played when there's a pending action state that targets this player
            GameState gs = session.getGameState();
            ActionState as = gs.getActionState();
            if (as == null || as == gs.getTurnState() || !as.canRefuseAny(owner)) {
                return false;
            }
        }

        // 前置校验
        if (options.asBankMoney()) {
            if (card instanceof RuleCard) return false;
            if (card instanceof PropertyCard && !GameConfig.CAN_BANK_PROPERTY_CARDS) return false;
            if (card instanceof ActionCard && !GameConfig.CAN_BANK_ACTION_CARDS) return false;
        } else if (!options.jsnBlocked() && !isJSN) {
            if (card instanceof ActionCard ac) {
                if (!effectExecutor.canUseActionEffect(ac, session, options)) return false;
            }
            if (card instanceof RentCard rc) {
                Player pay = RentRules.resolvedRentPayer(owner, session, options);
                if (!RentRules.canUseRentEffect(rc, owner, options.rentColorChoice(), pay, session))
                    return false;
            }
        }

        // 从手中移出
        if (!owner.getHand().removeCard(card) && !owner.getBank().removeCard(card)) return false;
        try {
            if (options.asBankMoney()) {
                owner.getBank().addCard(card);
            } else if (options.jsnBlocked()) {
                session.discardCard(card);
            } else if (isJSN) {
                // JSN: call refuse on the current action state, then discard
                GameState gs = session.getGameState();
                ActionState as = gs.getActionState();
                if (as != null && as != gs.getTurnState()) {
                    // The JSN player refuses the action owner
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
            owner.getHand().addCard(card);
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

    private static boolean shouldRecordOnPlayTable(Card card) {
        return card instanceof com.example.monopoly_deal_game.model.cards.BankCard
                || card instanceof PropertyCard;
    }

    /** 结束回合：手牌 > 7 则进入弃牌阶段。 */
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

    /** 弃牌阶段：丢弃选中的手牌，≤7 张后轮到下家。 */
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

    /** 胜负判定：任意玩家达到 {@link GameConfig#FULL_SETS_TO_WIN} 套完整物业。 */
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