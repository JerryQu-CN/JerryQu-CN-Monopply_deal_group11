/**
 * <h2>Network Layer</h2>
 * <p>
 * Responsibility: {@link com.example.monopoly_deal_game.network.NetworkClient} and
 * {@link com.example.monopoly_deal_game.network.GameServer} transmit messages;
 * the host runs the same {@link com.example.monopoly_deal_game.logic.GameLogic} as single-player mode,
 * clients send {@link com.example.monopoly_deal_game.logic.PlayerCommand},
 * and receive {@link com.example.monopoly_deal_game.game.model.GameSession} snapshots or incremental updates.
 * </p>
 *
 * <h3>Features to implement</h3>
 * <ul>
 *   <li>Bank/property data consistent with the host in online mode (Requirement 4-7 non-functional: reliability).</li>
 *   <li>Disconnect reconnection, heartbeat (optional, classes are reserved).</li>
 * </ul>
 *
 * <h3>Relationships with other packages</h3>
 * <ul>
 *   <li><b>→ logic</b>: Only the host directly modifies state; the client updates its local read-only model after receiving results.</li>
 *   <li><b>→ controller</b>: Suggest {@code SyncController} to route messages to the current interface.</li>
 * </ul>
 *
 * <h3>TODO (where to write)</h3>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.network.GameServer}: Listen port, rooms, broadcast.</li>
 *   <li>{@link com.example.monopoly_deal_game.network.NetworkClient}: Connect, send commands, receive snapshots.</li>
 *   <li>{@link com.example.monopoly_deal_game.network.MessageRouter}: Message type -> handler.</li>
 *   <li>{@link com.example.monopoly_deal_game.network.ConnectionManager} / {@link com.example.monopoly_deal_game.network.HeartbeatService}: Connection lifecycle.</li>
 * </ul>
 */
package com.example.monopoly_deal_game.network;
