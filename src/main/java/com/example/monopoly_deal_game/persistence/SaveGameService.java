package com.example.monopoly_deal_game.persistence;

import com.example.monopoly_deal_game.game.model.GameSession;

import java.nio.file.Path;

/**
 * Save/load read-write facade.
 *
 * TODO(persistence): implement load/save; verify magic number and version number.
 */
public class SaveGameService {

    public GameSession load(Path file) {
        throw new UnsupportedOperationException("TODO(persistence): implement load");
    }

    public void save(Path file, GameSession session) {
        throw new UnsupportedOperationException("TODO(persistence): implement save");
    }
}
