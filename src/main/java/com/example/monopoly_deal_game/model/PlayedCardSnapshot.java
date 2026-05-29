package com.example.monopoly_deal_game.model;

import java.io.Serial;
import java.io.Serializable;

/** Read-only snapshot for the played-cards display area (does not hold domain card references, avoiding lifecycle ambiguity with physical cards in the bank/property/discard pile). */
public record PlayedCardSnapshot(String name, String imageFileName) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
