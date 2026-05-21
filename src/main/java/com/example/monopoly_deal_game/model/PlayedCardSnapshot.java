package com.example.monopoly_deal_game.model;

import java.io.Serial;
import java.io.Serializable;

/** 出牌区展示用的只读快照（不持有领域卡牌引用，避免与银行/物业/弃牌堆中的实体牌生命周期歧义）。 */
public record PlayedCardSnapshot(String name, String imageFileName) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
