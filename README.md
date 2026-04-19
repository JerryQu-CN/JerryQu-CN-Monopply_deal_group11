# Monopoly Deal Game（Group 11）

基于 JavaFX 的 **Monopoly Deal** 课设项目，采用模块化分包（`app` / `controller` / `view` / `model` / `logic` / `network` / `persistence` / `ai`），与小组 UML 分层设计对齐。

远程仓库：<https://github.com/JerryQu-CN/JerryQu-CN-Monopply_deal_group11.git>

## 环境要求

- JDK **25**（与 `pom.xml` 中 `maven-compiler-plugin` 配置一致）
- Maven3.9+
- JavaFX **21.0.6**（由 Maven 依赖引入）

## 构建与运行

```bash
mvn -q clean compile
mvn javafx:run
```

`javafx-maven-plugin` 配置的启动类为：

`com.example.monopoly_deal_game/com.example.monopoly_deal_game.app.MonopolyDealApplication`

也可在 IDE 中运行 **`com.example.monopoly_deal_game.app.Launcher`** 的 `main` 方法。

## 包结构速览

| 包 | 职责 |
|----|------|
| `app` | JavaFX 应用入口 |
| `controller` | FXML 控制器、场景切换 |
| `view` | 视图扩展、观察者接口（如 `GameObserver`） |
| `model` | 卡牌、玩家、会话等领域模型 |
| `logic` | 规则与流程：`GameEngine`、`GameLogic`、回合/牌堆/行动牌效果等 |
| `network` | 联机：`GameServer`、`NetworkClient`、消息路由等 |
| `persistence` | 存档读写 `GameSession` |
| `ai` | 机器人策略 `BotPolicy` |

各包目录下另有 **`PACKAGE.txt`**（中文），说明类/接口清单、待实现点与包间关系。

## 资源与界面

- FXML 与图片：`src/main/resources/com/example/monopoly_deal_game/`
- 主菜单：`StartScreen.fxml`；对局界面：`GameplayScreen.fxml`

## 模块（JPMS）

`src/main/java/module-info.java` 对 `controller` 使用 `opens … to javafx.fxml`，其余包按需 `exports`。

## 协作说明

- 业务规则与状态修改集中在 **`logic`** 与 **`model`**，**`controller`** 尽量只做事件转发与绑定。
- 联机时建议由主机执行同一套 **`GameLogic`**，客户端同步会话快照或命令结果。

## License

课设用途；如需开源许可证请组内自行补充。
