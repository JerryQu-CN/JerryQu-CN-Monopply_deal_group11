# Logic Layer Weekly Task Specification

This document is for the students responsible for **rules and flows** (`com.example.monopoly_deal_game.logic` and the collaborating `GameEngine`/`GameSession`/`GameState`). Please first read through **`PACKAGE.txt`** and **`package-info.java`** (architecture boundaries), then proceed in the order below.

---

## 1. Prerequisites (must read)

Logic is not responsible for drawing UI, but **must have read/write access to the domain data within a game session**. The following parts of the current repository are still placeholders and **need alignment with model / game.model teammates on who lands first**:

| Location | Current status | Minimum contract Logic needs to complete work |
|------|------|-------------------------------|
| `game.model.GameSession` | `getPlayers()` etc. throw `UnsupportedOperationException` | Must provide: `List<Player>`, draw pile, discard pile (or equivalent structure), and a reference to `GameState`; must ensure `CardManager` can shuffle, draw, discard without losing cards. |
| `game.model.GameState` | Empty shell | Suggests at minimum: **current player index**, **current phase** (e.g., in-turn / waiting for card selection payment / Just Say No chain), **win/loss decided** and other mutable fields consistent with `TurnManager` and `GameLogic#endTurn`. |
| `logic.PlayerCommand` | Only `Placeholder` | This week, at least list the planned `record` type names and fields (see section 5), to facilitate alignment with networking and Bot. |

**Suggested collaboration order:** First complete the fields and accessors for `GameSession` + `GameState` (simple implementations are fine), then implement `CardManager` -> `TurnManager` -> `GameLogic` main path, and finally fill in `CardEffectExecutor` and `CardFactory` details.

Constants should follow **`game.rules.GameConfig`** (5-card starting hand, draw 2 when hand non-empty / draw 5 when empty, end-turn hand <= 7, max 3 cards played per turn, Just Say No does not consume quota).

---

## 2. Master Task Checklist (checkbox style)

Can be divided among team members; **before checking off, self-test one complete path: two players start -> draw -> play -> end turn -> placeholder win/loss determination**.

- [ ] **A. Model and Session** (can pair with model teammates)
  - [ ] `GameSession`: Player order, draw/discard piles, `GameState` reference.
  - [ ] `GameState`: Current player, turn phase, flags related to "waiting for payment / Just Say No" (can start with enum + boolean).
- [ ] **B. `CardFactory`**
  - [ ] `createFullDeck()` returns the **complete card list** (matching the course project/physical card list; quantity, action types, and `ActionCard.ActionType`, rent card colors, etc. must align).
- [ ] **C. `CardManager`**
  - [ ] `drawOne`: Take top card from draw pile; if empty, trigger discard pile shuffle and continue (see Requirement 1.5, 2.4).
  - [ ] `shuffleDrawPileFromDiscard`: Shuffle discard into draw pile; whether **the top card / just-played card stays in the discard pile** should be decided per your adopted rules and documented in a comment.
  - [ ] Support a loop of "draw N cards" potentially with multiple re-shuffles (reusable by `GameLogic#drawCard`, Pass Go, etc.).
- [ ] **D. `TurnManager`**
  - [ ] `beginTurn`: Reset "cards played this turn" count, consistent with `hasDrawnThisTurn` and other flags on `Player` / `GameState`.
  - [ ] `onCardPlayed`: When `countsTowardPlayLimit == false` (e.g., Just Say No), do not increment played count; otherwise increment and validate <= `GameConfig.MAX_PLAY_PER_TURN`.
- [ ] **E. `GameLogic` (main flow)**
  - [ ] `initGame`: Create `GameSession`, build card piles with `CardFactory`, shuffle, deal `INITIAL_HAND_SIZE` to each player, initialize `GameState` / current player.
  - [ ] `drawCard`: Draw 2 or 5 depending on whether hand is empty; only allowed at legal turn timing (consistent with `TurnManager` / phase); mark drawn.
  - [ ] `playCard`: Validate turn, play count, card in hand; dispatch: money/property into bank or property area, action/rent cards to `CardEffectExecutor` or specialized branch.
  - [ ] `endTurn`: If hand > `MAX_HAND_SIZE_END_TURN`, enter discard flow (UI can submit multiple times or internally store "to-discard list"); then switch to next player, `beginTurn`.
  - [ ] `checkGameOver`: Any player with **3 full sets of different colors (monopoly)** wins (consistent with Requirement 16); or agreed-upon draw/surrender extension points.
- [ ] **F. `CardEffectExecutor`**
  - [ ] `execute(...)` dispatches by `ActionCard.ActionType` to private methods or a small strategy table.
  - [ ] Covers: `DEAL_BREAKER`, `FORCE_DEAL`, `SLY_DEAL`, `ITS_MY_BIRTHDAY`, `DEBT_COLLECTOR`, `JUST_SAY_NO`, `PASS_GO`, `DOUBLE_RENT` (coordinating with the Rent chain).
  - [ ] Rent chain: `RentCard` color selection, read `Property`/rent tier, `DOUBLE_RENT` multiplier, **payment order** (bank -> property collateral, etc., per the course project document).
  - [ ] `JUST_SAY_NO`: **Chained cancellation** (can be played back and forth between multiple players until no one plays further); consistent with `TurnManager` "does not count toward the 3-card limit".
- [ ] **G. `PlayerCommand` (interface contract)**
  - [ ] Extend sealed subtypes (see section 5), and agree on a single execution path through `GameEngine` / `GameServer`.
- [ ] **H. Integration with `GameEngine`**
  - [ ] `startLocalGame`: Create session and `initGame`, return an observable `GameSession` (for UI subscription).
  - [ ] (Optional this week) `startLanGame` shares the same `GameLogic` instance with `GameServer`.
- [ ] **I. Tests**
  - [ ] Unit tests: draw counts, discard to 7, play limit, Just Say No not consuming quota, discard pile reshuffle replenishment.
  - [ ] Integration tests: Shortest game path or repeatable scenario with fixed seed shuffle.

---

## 3. Class-Level Specification: What to do & What input is needed

### 3.1 `CardFactory`

- **Needs:** The complete card list specified in class (spreadsheet or handout); constructor conventions for `PropertyCard`, `ActionCard`, `RentCard`, `BankCard` (money), building cards, etc.
- **Delivers:** `createFullDeck()` generates `List<Card>` in one call; order does not matter (shuffled by `initGame`).

### 3.2 `CardManager`

- **Needs:** Mutable references to the draw/discard piles within `GameSession`.
- **Delivers:** If not thread-safe, at least document the assumption of "single-machine single-thread / UI thread" in comments.

### 3.3 `TurnManager`

- **Needs:** Field meanings related to the current turn in `GameSession` / `GameState` / `Player` must be consistent (who counts as "current player" must have a single source of truth).

### 3.4 `GameLogic`

- **Needs:** The above components + **legal operation decision order** (if the requirements specify whether to draw first or play first, it must be followed).
- **Delivers:** Public methods keep pure domain side effects (modify `GameSession`), do not touch JavaFX.

### 3.5 `CardEffectExecutor`

- **Needs:** `Player`, `Property`, `PropertyCard`, APIs for moving cards in hand and bank (find existing methods in the `model` package; ask teammates to add if missing).
- **`execute` signature:** If "multiple targets", "card selection", "color selection" are actually needed, either extend the parameter object, or mount "pending interaction" on `GameState` and let the UI complete it (in the second week it can be simplified to local selection before LAN bot play).

---

## 4. Mapping to Requirements Chapters (memo)

| Requirement block | Main implementing classes |
|--------|------------|
| Initial deal, card piles | `GameLogic#initGame`, `CardFactory`, `CardManager`, `GameSession` |
| Turn draw (2/5) | `GameLogic#drawCard`, `GameConfig` |
| Play 0-3 cards, end turn, hand <= 7 | `TurnManager`, `GameLogic#playCard`, `#endTurn` |
| Pass Go / Double Rent | `ActionCard`, `CardEffectExecutor`, `RentCard` |
| Various actions and Just Say No chain | `CardEffectExecutor` |
| Three-color-set monopoly victory | `GameLogic#checkGameOver`, `Property`/color determination |

Details follow the course "Feature Specification" document chapters (if the repository does not include the PDF, ask the TA for it).

---

## 5. `PlayerCommand` Suggested Extensions (aligning with future network/ai)

Add under the sealed interface (names can be tweaked, but **meanings should be stable**):

| Suggested `record` | Purpose |
|---------------|------|
| `DrawTurnStart` or merged into engine auto-logic | If the engine auto-draws, this type may be unnecessary |
| `PlayCardFromHand` | Which card to play, target player id (if needed) |
| `PlaceProperty` / `BankMoney` | If "placing property/money into zone" and "playing" are split in rules |
| `PayRentSelection` | Choose which cards to pay with |
| `CounterWithJustSayNo` | Cancel one layer of the current top action on the stack |
| `DiscardToHandLimit` | Discard choice to reach 7 cards |
| `EndTurn` | Declare end of turn |

Serialization, `GameServer` validation, and **idempotency** can be deferred; this week, logic can first complete the closed loop by manually calling `GameLogic` methods.

---

## 7. Acceptance Criteria (minimum demonstrable)

1. Locally, a session can be generated from `GameEngine#startLocalGame` (or equivalent test fixture); after `initGame`, each player has **5** hand cards, and the deck count decrements correctly.
2. Any player's turn start: draw **2** if hand non-empty, **5** if empty; when the draw pile is exhausted, can shuffle discard pile and continue drawing the required amount.
3. Play at most **3** cards per turn; `JUST_SAY_NO` does **not** count.
4. At turn end, if hand exceeds **7**, must be able to complete the discard logic (can first be tested by directly calling `endTurn` with preconditions).
5. `checkGameOver` returns **true** when "manually arranged" for three color-set monopolies.
6. At least **one** non-trivial action card (recommended `PASS_GO` or `DEBT_COLLECTOR`) has a runnable implementation in `CardEffectExecutor`.

---

## 8. Reference File Paths (this repository)

- `game.engine.GameEngine` — Facade and `GameLogic` holding relationship
- `game.rules.GameConfig` — Numeric constants
- `game.model.GameSession` / `GameState` — Aggregate root (needs alignment)
- `model.Player`, `model.Property`, `model.cards.*` — Domain objects
- `ai.BotPolicy` — Produces legal intents validated and executed by logic

If there are questions, first align within the team on "single source of state truth", then write rules, to avoid repeatedly changing `GameSession` fields.
