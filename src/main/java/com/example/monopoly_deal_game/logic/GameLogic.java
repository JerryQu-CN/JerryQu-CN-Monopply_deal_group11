package com.example.monopoly_deal_game.logic;

import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.game.model.GameSession;

/**
 * 规则中枢（架构图 GameLogic：initGame / drawCard / playCard / endTurn / checkGameOver）。
 *
 * TODO(logic): 实现需求 1–3、16 的主流程；行动牌细节委托 {@link CardEffectExecutor}。
 */
public class GameLogic {

    private final CardManager cardManager = new CardManager();
    private final TurnManager turnManager = new TurnManager();
    private final CardEffectExecutor effectExecutor = new CardEffectExecutor();

    public void initGame(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): 需求1 发5张、建抽牌堆");
    }

    public void drawCard(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): 需求2 回合开始补牌");
    }

    public void playCard(GameSession session, Card card) {
        throw new UnsupportedOperationException("TODO(logic): 需求3–6 出牌与效果入口");
    }

    public void endTurn(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): 需求3 结束回合、>7弃牌、切下家");
    }

    public boolean checkGameOver(GameSession session) {
        throw new UnsupportedOperationException("TODO(logic): 需求16 三套不同颜色完整物业");
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
