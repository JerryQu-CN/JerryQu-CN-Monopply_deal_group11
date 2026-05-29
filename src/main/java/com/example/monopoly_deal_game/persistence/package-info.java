/**
 * <h2>Persistence Layer</h2>
 * <p>
 * Responsibility: Write {@link com.example.monopoly_deal_game.game.model.GameSession} (and the necessary version number)
 * to disk, and restore it when loading a save,
 * for use by {@link com.example.monopoly_deal_game.controller.LoadGameScreenController}
 * and {@link com.example.monopoly_deal_game.game.engine.GameEngine}.
 * </p>
 *
 * <h3>Features to implement</h3>
 * <ul>
 *   <li>Atomic write (temp file + rename) to avoid save corruption (non-functional: reliability).</li>
 *   <li>Version field: for future model migration.</li>
 * </ul>
 *
 * <h3>Relationships with other packages</h3>
 * <ul>
 *   <li><b>→ model</b>: Only serialize model objects, do not serialize JavaFX Node.</li>
 *   <li><b>← controller</b>: The load save screen calls {@link com.example.monopoly_deal_game.persistence.SaveGameService#load(java.nio.file.Path)}.</li>
 * </ul>
 *
 * <h3>TODO</h3>
 * <ul>
 *   <li>Choose JSON (recommended) or binary format; implement {@link com.example.monopoly_deal_game.persistence.SaveGameService}.</li>
 * </ul>
 */
package com.example.monopoly_deal_game.persistence;
