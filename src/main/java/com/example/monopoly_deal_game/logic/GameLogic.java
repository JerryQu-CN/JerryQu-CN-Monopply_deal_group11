package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.game.model.GameState;
import com.example.monopoly_deal_game.model.Player;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.game.model.GameSession;

import java.util.List;

/**
 * 规则中枢：实现初始化、摸牌、出牌主流程及胜负判定。
 */
public class GameLogic {

    private final CardManager cardManager = new CardManager();
    private final TurnManager turnManager = new TurnManager();
    private final CardEffectExecutor effectExecutor = new CardEffectExecutor();

    /**
     * 需求 1: 初始化游戏
     */
    public void initGame(GameSession session) {
        // 1. 创建并初始化牌堆
        CardFactory factory = new CardFactory();
        cardManager.initDeck(factory.createFullDeck());

        // 2. 发起手牌（每人 5 张）
        List<Player> players = session.getPlayers();
        for (Player p : players) {
            for (int i = 0; i < 5; i++) {
                p.getHand().add(cardManager.drawOne(session));
            }
        }

        // 3. 设定先手玩家
        session.getGameState().setCurrentPlayerIndex(0);
        turnManager.beginTurn(session);
    }

    /**
     * 需求 2: 回合开始摸牌逻辑（补 2 张或 5 张）
     */
    public void drawCard(GameSession session) {
        Player current = session.getCurrentPlayer();
        GameState state = session.getGameState();

        // 只有在摸牌阶段才能摸牌
        if (state.getPhase() != GameState.Phase.DRAW_PHASE) return;

        // 规则适配：空手摸 5 张，否则摸 2 张
        int count = current.getHand().isEmpty() ? 5 : 2;

        for (int i = 0; i < count; i++) {
            // 1. 先从牌堆摸一张牌，存入临时变量 c
            Card c = cardManager.drawOne(session);

            // 2. 如果摸到了牌（不为 null），再加入手牌
            if (c != null) {
                current.getHand().add(c);
            }
        }
        // 更新状态：已摸牌，进入出牌阶段
        state.setHasDrawnThisTurn(true);
        state.setPhase(GameState.Phase.PLAY_PHASE);
    }

    /**
     * 需求 3–6: 出牌主入口
     */
    public void playCard(GameSession session, Card card) {
        // 1. 校验出牌合法性
        if (!turnManager.canPlayMore(session)) {
            System.out.println("Logic Error: Cannot play more cards this turn.");
            return;
        }

        // 2. 执行效果（委托给 EffectExecutor 或直接处理入账）
        // 假设这里已经根据 UI 选择确定了这张卡是存银行还是发动效果
        effectExecutor.execute(card, session);

        // 3. 更新出牌计数（处理 Just Say No 豁免）
        boolean counts = true;
        if (card instanceof ActionCard) {
            counts = ((ActionCard) card).isCountsTowardLimit();
        }
        turnManager.onCardPlayed(session, counts);
    }

    /**
     * 需求 3: 结束回合
     */
    public void endTurn(GameSession session) {
        Player current = session.getCurrentPlayer();

        // 1. 检查手牌上限 (需求: 结束时手牌需 <= 7)
        if (current.getHand().size() > 7) {
            System.out.println("Logic Warning: Player must discard to 7 cards.");
            // 此处通常在 UI 触发弃牌逻辑，直到满足条件再调用本方法
            return;
        }

        // 2. 检查胜负
        if (checkGameOver(session)) {
            session.getGameState().setGameOver(true);
            return;
        }

        // 3. 切换下家
        int nextIndex = (session.getGameState().getCurrentPlayerIndex() + 1) % session.getPlayers().size();
        session.getGameState().setCurrentPlayerIndex(nextIndex);

        // 4. 开启新回合
        turnManager.beginTurn(session);
    }

    /**
     * 需求 16: 胜利判定（3 套不同颜色完整物业）
     */
    public boolean checkGameOver(GameSession session) {
        for (Player p : session.getPlayers()) {
            // 此处需调用 Player 类中的资产统计方法
            // 假设 Player 类有 getFullSetCount() 方法
            if (p.getFullSetCount() >= 3) {
                System.out.println("Game Over! Winner: " + p.getName());
                return true;
            }
        }
        return false;
    }

    // Getters for Managers...
    public CardManager getCardManager() { return cardManager; }
    public TurnManager getTurnManager() { return turnManager; }
}