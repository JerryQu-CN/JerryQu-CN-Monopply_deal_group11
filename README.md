
# Monopoly Deal Game (Group 11)

JavaFX-based **Monopoly Deal** card game with LAN multiplayer support, following a layered MVC architecture with JPMS module system.

## Environment

- JDK **21** (OpenJDK / Eclipse Temurin)
- Maven 3.9+
- JavaFX **21.0.6** (via Maven dependencies)

## Build & Run

```bash
mvn clean compile
mvn javafx:run
```

The `javafx-maven-plugin` entry point:

`com.example.monopoly_deal_game/com.example.monopoly_deal_game.app.MonopolyDealApplication`

Or run `com.example.monopoly_deal_game.app.Launcher.main()` from your IDE.

## Package Structure

| Package | Responsibility |
|---------|---------------|
| `app` | JavaFX application entry |
| `controller` | FXML controllers, screen navigation, dialogs, lobby |
| `game.engine` | Game lifecycle orchestration |
| `game.state` | Session state, action state machine, player actions |
| `logic` | Game rules: turns, card effects, eligibility, payment |
| `model` | Domain entities: cards, players, properties, bank |
| `network` | LAN multiplayer: server, client, message routing |
| `view` | JavaFX rendering, card views, animations |

## Module System (JPMS)

`module-info.java` exports all packages and opens FXML-controller packages to `javafx.fxml`.

## Architecture

- **Host-authoritative**: The host runs the canonical game state; clients send validated actions and receive session snapshots.
- **State machine**: `ActionState` stack models nested interactions (normal play → targeted response → Just Say No chains).
- **Dependency injection**: `AppContext` singleton provides shared dependencies (`GameEngine`, `GameServer`).

## License

Course project — for academic use.