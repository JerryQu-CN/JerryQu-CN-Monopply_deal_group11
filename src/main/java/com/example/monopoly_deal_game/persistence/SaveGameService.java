package com.example.monopoly_deal_game.persistence;

import com.example.monopoly_deal_game.game.model.GameSession;

import java.nio.file.Path;

/**
 * 存档读写门面。
 *
 * TODO(persistence): load/save 实现；校验魔数与版本号。
 */
public class SaveGameService {

    public GameSession load(Path file) {
        throw new UnsupportedOperationException("TODO(persistence): implement load");
    }

    public void save(Path file, GameSession session) {
        throw new UnsupportedOperationException("TODO(persistence): implement save");
    }
}
