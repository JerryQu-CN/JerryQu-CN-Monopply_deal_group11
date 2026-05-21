package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.game.model.GameState;
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
 * 规则中枢：初始化、摸牌、出牌主流程及胜负判定（ZouChenyu / astralcattttt 分支逻辑并入）。
 */
public class GameLogic {

    private final CardManager cardManager = new CardManager();
    private final TurnManager turnManager = new TurnManager();
    private final CardEffectExecutor effectExecutor;

    public GameLogic() {
        this.effectExecutor = new CardEffectExecutor(cardManager);
    }

    /** 需求 1: 初始化游戏 */
    public void initGame(GameSession session) {
        CardFactory factory = new CardFactory();
        List<Card> deck = new java.util.ArrayList<>(factory.createFullDeck());
        deck.removeIf(c -> c instanceof com.example.monopoly_deal_game.model.cards.RuleCard);
        cardManager.initDeck(session, deck);

        List<Player> players = session.getPlayers();
        for (Player p : players) {
            p.clearPlayedCardsDisplay();
            for (int i = 0; i < 5; i++) {
                Card c = cardManager.drawOne(session);
                if (c != null) {
                    p.getHand().add(c);
                }
            }
        }

        session.getGameState().setCurrentPlayerIndex(0);
        turnManager.beginTurn(session);
    }

    /** 需求 2: 回合开始摸牌（空手 5 张，否则 2 张）；每回合仅允许摸一次。 */
    public void drawCard(GameSession session) {
        Player current = session.getCurrentPlayer();
        GameState state = session.getGameState();
        if (state.getPhase() != GameState.Phase.DRAW_PHASE || current == null) {
            return;
        }
        if (state.isHasDrawnThisTurn()) {
            return;
        }

        int count = current.getHand().isEmpty() ? 5 : 2;
        for (int i = 0; i < count; i++) {
            Card c = cardManager.drawOne(session);
            if (c != null) {
                current.getHand().add(c);
            }
        }
        state.setHasDrawnThisTurn(true);
        state.setPhase(GameState.Phase.PLAY_PHASE);
    }

    /**
     * 需求 3–6: 出牌主入口（默认：钞票/物业按规则落位；租金卡按可收最高租自动选色）。
     *
     * @return {@code false} 时常见原因：尚未摸牌、本回合已打满 3 张、手牌中无此牌引用、效果前提不满足
     */
    public boolean playCard(GameSession session, Card card) {
        return playCard(session, card, CardPlayOptions.auto());
    }

    public boolean playCard(GameSession session, Card card, CardPlayOptions options) {
        if (!turnManager.canPlayMore(session)) {
            return false;
        }
        Player current = session.getCurrentPlayer();
        if (current == null) {
            return false;
        }
        if (!current.getHand().getCards().contains(card)) {
            return false;
        }

        if (options == null) {
            options = CardPlayOptions.auto();
        }

        if (options.asBankMoney()) {
            if (card instanceof RuleCard) {
                return false;
            }
        } else {
            if (card instanceof ActionCard ac) {
                if (!effectExecutor.canUseActionEffect(ac, session, options)) {
                    return false;
                }
            }
            if (card instanceof RentCard rc) {
                Player pay = RentRules.resolvedRentPayer(current, session, options);
                if (!RentRules.canUseRentEffect(
                        rc, current, options.rentColorChoice(), pay, session)) {
                    return false;
                }
            }
        }

        if (!current.getHand().removeCard(card)) {
            return false;
        }
        try {
            if (options.asBankMoney()) {
                current.getBank().addCard(card);
            } else {
                effectExecutor.execute(card, session, options);
            }
            if (shouldRecordOnPlayTable(card)) {
                current.recordPlayedCardForDisplay(
                        new PlayedCardSnapshot(card.getName(), CardImageMapper.imageFileFor(card)));
            }
            turnManager.onCardPlayed(session, card.isCountsTowardLimit());
            return true;
        } catch (RuntimeException ex) {
            current.getHand().add(card);
            throw ex;
        }
    }

    /**
     * 牌桌「打出展示条」仅记录<strong>房产</strong>（物业牌）与<strong>财产</strong>（钞票入银行）；
     * 行动牌、租金等功能牌不展示于此。
     */
    private static boolean shouldRecordOnPlayTable(Card card) {
        return card instanceof com.example.monopoly_deal_game.model.cards.BankCard
                || card instanceof PropertyCard;
    }

    /** 需求 3: 结束回合 — 若手牌超 7 张则进入弃牌阶段而不推进回合。 */
    public void endTurn(GameSession session) {
        Player current = session.getCurrentPlayer();
        if (current == null) {
            return;
        }

        if (current.getHand().size() > 7) {
            session.getGameState().setPhase(GameState.Phase.DISCARD_PHASE);
            return;
        }

        advanceToNextPlayer(session);
    }

    /** 弃牌阶段：丢弃选中的手牌（须均为当前回合玩家手牌），≤7 张后轮到下家。 */
    public void discardFromHandChosen(GameSession session, Collection<Card> chosen) {
        if (chosen == null || chosen.isEmpty()) {
            return;
        }
        Player cur = session.getCurrentPlayer();
        GameState state = session.getGameState();
        if (cur == null || state.getPhase() != GameState.Phase.DISCARD_PHASE) {
            return;
        }
        for (Card c : chosen) {
            if (c == null || !cur.getHand().getCards().contains(c)) {
                continue;
            }
            if (cur.getHand().removeCard(c)) {
                session.discardCard(c);
            }
        }
        if (cur.getHand().size() <= 7) {
            advanceToNextPlayer(session);
        }
    }

    private void advanceToNextPlayer(GameSession session) {
        if (checkGameOver(session)) {
            session.getGameState().setGameOver(true);
            return;
        }
        List<Player> players = session.getPlayers();
        if (players.isEmpty()) {
            return;
        }
        int nextIndex =
                (session.getGameState().getCurrentPlayerIndex() + 1) % players.size();
        session.getGameState().setCurrentPlayerIndex(nextIndex);
        turnManager.beginTurn(session);
    }

    /** 需求 16: 三套完整物业胜负判定 */
    public boolean checkGameOver(GameSession session) {
        for (Player p : session.getPlayers()) {
            if (p.getFullSetCount() >= 3) {
                System.out.println("Game Over! Winner: " + p.getName());
                session.getGameState().setGameOver(true);
                return true;
            }
        }
        return false;
    }

    public CardManager getCardManager() {
        return cardManager;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public CardEffectExecutor getEffectExecutor() {
        return effectExecutor;
    }
}
