# Logic 层本周任务说明书

本文档面向负责 **规则与流程**（`com.example.monopoly_deal_game.logic` 及与之协作的 `GameEngine`/`GameSession`/`GameState`）的同学。请先通读 **`PACKAGE.txt`** 与 **`package-info.java`**（架构边界），再按下面顺序动工。

---

## 1. 前置依赖（必读）

logic 不负责画 UI，但**必须有权读写一局里的领域数据**。当前仓库里以下部分仍为占位，**需与 model / game.model 同伴对齐谁先落地**：

| 位置 | 现状 | Logic 要完成工作所需的最小契约 |
|------|------|-------------------------------|
| `game.model.GameSession` | `getPlayers()` 等抛 `UnsupportedOperationException` | 需提供：`List<Player>`、抽牌堆、弃牌堆（或等价结构）、以及对 `GameState` 的引用；能保证 `CardManager` 能洗牌、摸牌、弃牌且不丢牌。 |
| `game.model.GameState` | 空壳 | 建议至少：**当前玩家索引**、**本阶段**（例如：回合内 / 等待选牌支付 / Just Say No 链）、**胜负是否已决**等与 `TurnManager`、`GameLogic#endTurn` 一致的可变字段。 |
| `logic.PlayerCommand` | 仅 `Placeholder` | 周内至少列出计划中的 `record` 类型名称与字段（见 §5），便于联机与 Bot 对齐。 |

**建议协作顺序：** 先把 `GameSession` + `GameState` 的字段与 accessor 补齐（可仍留简单实现），再实现 `CardManager` → `TurnManager` → `GameLogic` 主链路，最后填 `CardEffectExecutor` 与 `CardFactory` 细节。

常量以 **`game.rules.GameConfig`** 为准（起手 5 张、有牌摸 2 / 空手摸 5、回合结束手牌≤7、每回合出牌≤3、Just Say No 不占额度）。

---

## 2. 任务总清单（勾选式）

可按小组分工；**勾选前请自测一条完整路径：俩人开局 → 摸牌 → 出牌 → 结束回合 → 判负/判胜占位**。

- [ ] **A. 模型与会话**（可与 model 同学结对）  
  - [ ] `GameSession`：玩家顺序、抽/弃牌堆、`GameState` 引用。  
  - [ ] `GameState`：当前玩家、回合阶段、与「等待支付 / Just Say No」相关的标志（可先枚举 + 布尔）。  
- [ ] **B. `CardFactory`**  
  - [ ] `createFullDeck()` 返回**完整牌表**（与课设/实体牌列表一致；数量、行动类型与 `ActionCard.ActionType`、租金卡颜色等需可对上号）。  
- [ ] **C. `CardManager`**  
  - [ ] `drawOne`：从抽牌堆顶取牌；空则触发弃牌洗回再继续（见需求 1.5、2.4）。  
  - [ ] `shuffleDrawPileFromDiscard`：弃牌洗回抽牌堆，**顶牌/刚出的牌是否保留在弃牌堆**按你们 adopted 规则写死并写注释。  
  - [ ] 支持「一次摸 N 张」且中途可能多次洗牌的循环（供 `GameLogic#drawCard`、Pass Go 等复用）。  
- [ ] **D. `TurnManager`**  
  - [ ] `beginTurn`：重置「本回合已打出张数」、与 `Player` / `GameState` 上 `hasDrawnThisTurn` 等标志一致。  
  - [ ] `onCardPlayed`：`countsTowardPlayLimit == false` 时（如 Just Say No）不增加出牌计数；否则增加并校验 ≤ `GameConfig.MAX_PLAY_PER_TURN`。  
- [ ] **E. `GameLogic`（主流程）**  
  - [ ] `initGame`：建 `GameSession`、用 `CardFactory` 建牌堆、洗牌、每人发 `INITIAL_HAND_SIZE`、初始化 `GameState` / 当前玩家。  
  - [ ] `drawCard`：按手牌是否为空决定摸 2 或 5；仅允许在回合合法时机调用（与 `TurnManager` / 阶段一致）；标记已摸牌。  
  - [ ] `playCard`：校验回合、出牌次数、牌是否在手牌；分派：货币/物业入银行或物业区、行动牌/租金牌交 `CardEffectExecutor` 或专用分支。  
  - [ ] `endTurn`：若手牌 > `MAX_HAND_SIZE_END_TURN` 则进入弃牌流程（可由 UI 多次提交或内部暂存「待弃列表」）；然后切下家、`beginTurn`。  
  - [ ] `checkGameOver`：任一玩家有 **3 套不同颜色且完整（垄断）** 的物业则胜（与课设需求 16 一致）；或约定平局/认输扩展点。  
- [ ] **F. `CardEffectExecutor`**  
  - [ ] `execute(...)` 按 `ActionCard.ActionType` 分发到私有方法或小策略表。  
  - [ ] 覆盖：`DEAL_BREAKER`、`FORCE_DEAL`、`SLY_DEAL`、`ITS_MY_BIRTHDAY`、`DEBT_COLLECTOR`、`JUST_SAY_NO`、`PASS_GO`、`DOUBLE_RENT`（与 Rent 链路配合）。  
  - [ ] 租金链路：`RentCard` 选色、读取 `Property`/租金等级、`DOUBLE_RENT` 倍率、**支付顺序**（银行 → 物业顶账等按课设文档）。  
  - [ ] `JUST_SAY_NO`：**链式抵消**（多人间可反复打出直到无人再出）；与 `TurnManager`「不计入 3 张」一致。  
- [ ] **G. `PlayerCommand`（接口契约）**  
  - [ ] 扩展 sealed 子类型（见 §5），并约定由 `GameEngine` / `GameServer` 唯一执行路径。  
- [ ] **H. 与 `GameEngine` 衔接**  
  - [ ] `startLocalGame`：创建会话并 `initGame`，返回可观察的 `GameSession`（供 UI 订阅）。  
  - [ ] （可选本周）`startLanGame` 与 `GameServer` 共用同一 `GameLogic` 实例。  
- [ ] **I. 测试**  
  - [ ] 单元测试：摸牌张数、弃牌至 7、出牌上限、Just Say No 不占额度、洗弃牌回补。  
  - [ ] 集成测试：最短局路径或固定种子洗牌的可重复场景。

---

## 3. 类级说明：要做什么 & 要什么输入

### 3.1 `CardFactory`

- **要什么：** 课上规定的完整牌清单（电子版表格或讲义）；`PropertyCard`、`ActionCard`、`RentCard`、`BankCard`（货币）、建筑卡等构造函数约定。  
- **交付：** `createFullDeck()` 一次性生成 `List<Card>`，顺序无关（由 `initGame` 洗牌）。

### 3.2 `CardManager`

- **要什么：** `GameSession` 内对抽/弃 pile 的可变引用。  
- **交付：** 线程安全若不做，至少在文档写明「单机单线程/UI 线程」假定。

### 3.3 `TurnManager`

- **要什么：** `GameSession` / `GameState` / `Player` 上与本回合相关的字段含义一致（谁算「当前玩家」以单一来源为准）。

### 3.4 `GameLogic`

- **要什么：** 上述组件 + **合法操作的判定顺序**（先摸牌还是先出牌若需求有写必须遵守）。  
- **交付：** 公开方法保持纯领域副作用（改 `GameSession`），不触碰 JavaFX。

### 3.5 `CardEffectExecutor`

- **要什么：** `Player`、`Property`、`PropertyCard`、手牌与银行移动牌的 API（从 `model` 包找现成方法，缺则让对方补）。  
- **`execute` 签名：** 若实际需要「多目标」「选卡」「选颜色」，要么扩展参数对象，要么通过 `GameState` 挂载「待定交互」再由 UI 补全（第二周可简化为人机局域网联机前先本地选择）。

---

## 4. 与需求章节对应（备忘）

| 需求块 | 主要落地类 |
|--------|------------|
| 开局发牌、牌堆 | `GameLogic#initGame`、`CardFactory`、`CardManager`、`GameSession` |
| 回合补牌（2/5） | `GameLogic#drawCard`、`GameConfig` |
| 出牌 0–3、结束回合、手牌≤7 | `TurnManager`、`GameLogic#playCard`、`#endTurn` |
| Pass Go / 双倍租金 | `ActionCard`、`CardEffectExecutor`、`RentCard` |
| 各类行动与 Just Say No 链 | `CardEffectExecutor` |
| 三套颜色垄断胜利 | `GameLogic#checkGameOver`、`Property`/颜色判定 |

细则以课程「功能说明」文档章节为准（仓库若未收录 PDF，请向助教索取）。

---

## 5. `PlayerCommand` 建议扩展（与日后的 network/ai 对齐）

在 sealed 接口下增加（名称可微调，但**含义要稳定**）：

| 建议 `record` | 用途 |
|---------------|------|
| `DrawTurnStart` 或合并进引擎自动逻辑 | 若由引擎自动摸牌则可无此类型 |
| `PlayCardFromHand` | 打出哪张、目标玩家 id（若需要） |
| `PlaceProperty` / `BankMoney` | 若「物业/货币放入区」与「打出」在规则上分拆 |
| `PayRentSelection` | 选择用哪些牌支付 |
| `CounterWithJustSayNo` | 对当前栈顶行动取消一层 |
| `DiscardToHandLimit` | 弃至 7 张的选择 |
| `EndTurn` | 宣告回合结束 |

序列化、`GameServer` 校验与 **幂等** 可后置；本周 logic 可先用手动调用 `GameLogic` 的方法完成闭环。

---

## 7. 验收口径（最小可演示）

1. 本地可从 `GameEngine#startLocalGame`（或等价测试夹具）生成一局，`initGame` 后每人 **5** 张手牌，牌堆数目正确递减。  
2. 任一玩家回合开始：手牌非空摸 **2**、为空摸 **5**；摸牌后牌堆用尽能洗弃牌并继续摸够。  
3. 出牌最多 **3** 张，`JUST_SAY_NO` **不**计入。  
4. 回合结束手牌多于 **7** 时必须能走完弃牌逻辑（可先由测试直接调用 `endTurn` 前置条件）。  
5. `checkGameOver` 在「人为摆盘」三包垄断时能返回 **true**。  
6. 至少 **一种**非平凡行动牌（建议 `PASS_GO` 或 `DEBT_COLLECTOR`）在 `CardEffectExecutor` 中有可运行实现。

---

## 8. 参考文件路径（本仓库）

- `game.engine.GameEngine` — 门面与 `GameLogic` 持有关系  
- `game.rules.GameConfig` — 数值常量  
- `game.model.GameSession` / `GameState` — 聚合根（需对齐）  
- `model.Player`、`model.Property`、`model.cards.*` — 领域对象  
- `ai.BotPolicy` — 产出合法意图后由 logic 校验执行  

有问题先在组内对齐「状态单一来源」，再写规则，可避免来回改 `GameSession` 字段。
