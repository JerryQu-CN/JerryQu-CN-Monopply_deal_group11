package com.example.monopoly_deal_game.demo.autorun;

import javafx.application.Application;

/**
 * 一键启动：直接进入 {@link DemoGameplayApplication}（人机 1+1、GameplayScreen），
 * 便于肉眼看「model 状态 ↔ view 表现」是否一致。
 *
 * <p>运行示例（需已配置 JAVA_HOME）：</p>
 * <pre>
 * mvn -q javafx:run@autorun
 * </pre>
 */
public final class AutorunFrontendLauncher {

    private AutorunFrontendLauncher() {}

    public static void main(String[] args) {
        Application.launch(DemoGameplayApplication.class, args);
    }
}
