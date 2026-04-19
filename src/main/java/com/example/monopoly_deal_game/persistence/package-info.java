/**
 * <h2>持久化层（Persistence）</h2>
 * <p>
 * 职责：把 {@link com.example.monopoly_deal_game.model.GameSession}（及必要版本号）写入磁盘，并在读档时恢复，
 * 供 {@link com.example.monopoly_deal_game.controller.LoadGameScreenController} 与 {@link com.example.monopoly_deal_game.logic.GameEngine} 使用。
 * </p>
 *
 * <h3>需要实现的功能</h3>
 * <ul>
 *   <li>原子写盘（临时文件 + rename）避免损坏存档（非功能：可靠性）。</li>
 *   <li>版本字段：以后改模型可做迁移。</li>
 * </ul>
 *
 * <h3>与其它包的关系</h3>
 * <ul>
 *   <li><b>→ model</b>：只序列化模型对象，不序列化 JavaFX Node。</li>
 *   <li><b>← controller</b>：读档界面调 {@link com.example.monopoly_deal_game.persistence.SaveGameService#load(java.nio.file.Path)}。</li>
 * </ul>
 *
 * <h3>TODO</h3>
 * <ul>
 *   <li>选定 JSON（推荐）或二进制格式；实现 {@link com.example.monopoly_deal_game.persistence.SaveGameService}。</li>
 * </ul>
 */
package com.example.monopoly_deal_game.persistence;
