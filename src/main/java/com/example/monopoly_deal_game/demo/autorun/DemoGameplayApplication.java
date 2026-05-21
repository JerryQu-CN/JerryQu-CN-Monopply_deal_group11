package com.example.monopoly_deal_game.demo.autorun;

import com.example.monopoly_deal_game.controller.AbstractGameplayScreenController;
import com.example.monopoly_deal_game.controller.AppContext;
import com.example.monopoly_deal_game.controller.ScreenNavigation;
import com.example.monopoly_deal_game.controller.StageAware;
import com.example.monopoly_deal_game.game.engine.GameEngine;
import com.example.monopoly_deal_game.game.model.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.logic.GameLogic;
import com.example.monopoly_deal_game.model.cards.ActionCard;
import com.example.monopoly_deal_game.model.cards.Card;
import com.example.monopoly_deal_game.model.cards.PropertyCard;
import com.example.monopoly_deal_game.model.cards.RentCard;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * 自动进入对局界面：跳过主菜单，直接 {@code startLocalGame(1,1)}，用于观察 UI。
 *
 * <p>默认<strong>不会</strong>自动摸牌、出牌。若需旧版自动演示，启动 JVM 时加：
 * {@code -Dmonopoly.deal.demo.autorun=true}
 */
public class DemoGameplayApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        AppContext.install(AppContext.createDefault());

        GameEngine engine = AppContext.get().gameEngine();
        GameSession session = engine.startLocalGame(1, 1);
        printSessionSnapshot("开局后", session);

        FXMLLoader loader = new FXMLLoader(ScreenNavigation.fxmlUrl(ScreenNavigation.GAMEPLAY_FXML));
        Scene scene = new Scene(loader.load(), ScreenNavigation.SCENE_WIDTH, ScreenNavigation.SCENE_HEIGHT);
        Object controller = loader.getController();
        if (controller instanceof StageAware stageAware) {
            stageAware.setStage(stage);
        }

        stage.setTitle("Demo · View/Model 前端 — Monopoly Deal");
        stage.setScene(scene);
        stage.show();

        if (controller instanceof AbstractGameplayScreenController uiCtl
                && Boolean.getBoolean("monopoly.deal.demo.autorun")) {
            scheduleAutorun(engine, session, uiCtl);
        }
    }

    /**
     * 延迟执行：摸牌 → 连续尝试出牌（直到规则阻止或打满本回合），便于肉眼验证 logic 与 UI。
     */
    private static void scheduleAutorun(
            GameEngine engine, GameSession session, AbstractGameplayScreenController uiCtl) {
        PauseTransition waitDraw = new PauseTransition(Duration.millis(500));
        waitDraw.setOnFinished(
                e -> {
                    engine.getGameLogic().drawCard(session);
                    uiCtl.refreshSessionUi();
                    printSessionSnapshot("自动摸牌后", session);

                    PauseTransition waitPlay = new PauseTransition(Duration.millis(450));
                    waitPlay.setOnFinished(ev -> autoPlayLoop(engine, session, uiCtl));
                    waitPlay.play();
                });
        waitDraw.play();
    }

    private static void autoPlayLoop(GameEngine engine, GameSession session, AbstractGameplayScreenController uiCtl) {
        GameLogic logic = engine.getGameLogic();
        for (int i = 0; i < 8; i++) {
            if (session.getGameState().isGameOver()) {
                break;
            }
            if (!logic.getTurnManager().canPlayMore(session)) {
                break;
            }
            var cur = session.getCurrentPlayer();
            if (cur == null) {
                break;
            }
            var hand = cur.getHand().getCards();
            if (hand.isEmpty()) {
                break;
            }
            Card next = hand.get(0);
            try {
                boolean ok = tryAutoOrBank(logic, session, next);
                if (!ok) {
                    break;
                }
                if (logic.checkGameOver(session)) {
                    session.getGameState().setGameOver(true);
                }
                uiCtl.refreshSessionUi();
                printSessionSnapshot("自动出牌 #" + (i + 1), session);
            } catch (IllegalStateException ex) {
                System.out.println("[demo.autorun] 出牌停止: " + ex.getMessage());
                break;
            }
        }
    }

    /** 先按规则使用效果；若不成立则尝试作银行现金（便于演示机自动出牌）。 */
    private static boolean tryAutoOrBank(GameLogic logic, GameSession session, Card card) {
        if (logic.playCard(session, card, CardPlayOptions.auto())) {
            return true;
        }
        if (card instanceof ActionCard || card instanceof RentCard || card instanceof PropertyCard) {
            return logic.playCard(session, card, CardPlayOptions.bankOnly());
        }
        return false;
    }

    static void printSessionSnapshot(String tag, GameSession s) {
        if (s == null) {
            System.out.println("[demo.autorun] " + tag + " session=null");
            return;
        }
        var cur = s.getCurrentPlayer();
        int bank = 0;
        if (cur != null) {
            for (Card c : cur.getBank().getCards()) {
                bank += Math.max(0, c.getValue());
            }
        }
        System.out.println(
                "[demo.autorun] "
                        + tag
                        + " | 玩家数="
                        + s.getPlayers().size()
                        + " | 抽牌堆="
                        + s.getDrawPile().size()
                        + " | 当前="
                        + (cur != null ? cur.getName() : "?")
                        + " | 手牌="
                        + (cur != null ? cur.getHand().size() : 0)
                        + " | 桌面组="
                        + (cur != null ? cur.getProperties().size() : 0)
                        + " | 银行≈"
                        + bank
                        + "M"
                        + " | 阶段="
                        + s.getGameState().getPhase());
    }
}
