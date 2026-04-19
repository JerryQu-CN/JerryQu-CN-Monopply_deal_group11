/**
 * <h2>网络层（Network）</h2>
 * <p>
 * 职责：{@link com.example.monopoly_deal_game.network.NetworkClient} 与 {@link com.example.monopoly_deal_game.network.GameServer} 传输消息；
 * 主机运行与单机相同的 {@link com.example.monopoly_deal_game.logic.GameLogic}，客户端发 {@link com.example.monopoly_deal_game.logic.PlayerCommand}，
 * 收到 {@link com.example.monopoly_deal_game.model.GameSession} 快照或增量更新。
 * </p>
 *
 * <h3>需要实现的功能</h3>
 * <ul>
 *   <li>联机时银行/物业数据与主机一致（需求 4–7 非功能：可靠性）。</li>
 *   <li>断线重连、心跳（选做，类已预留）。</li>
 * </ul>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li><b>→ logic</b>：仅主机直接改状态；客户端收到结果后更新本地只读模型。</li>
 *   <li><b>→ controller</b>：建议 {@code SyncController} 把消息路由到当前界面。</li>
 * </ul>
 *
 * <h3>TODO（在哪写）</h3>
 * <ul>
 *   <li>{@link com.example.monopoly_deal_game.network.GameServer}：监听端口、房间、广播。</li>
 *   <li>{@link com.example.monopoly_deal_game.network.NetworkClient}：连接、发送命令、收快照。</li>
 *   <li>{@link com.example.monopoly_deal_game.network.MessageRouter}：消息类型 → 处理器。</li>
 *   <li>{@link com.example.monopoly_deal_game.network.ConnectionManager} / {@link com.example.monopoly_deal_game.network.HeartbeatService}：连接生命周期。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.network;
