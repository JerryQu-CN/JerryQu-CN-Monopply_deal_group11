package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;
import java.io.Serializable;

/**
 * Card base class: contains ID, name, value, description, and rendering-related properties.
 * Aligned with the data model of oldmana.md.server.card.Card from the Monopoly-Deal-main project.
 */
public abstract class Card implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    protected int id;
    protected String name;
    protected String description;
    protected int value;

    // Card colors
    protected int outerColorRGB;         // Outer border color (RGB int)
    protected int innerColorRGB;         // Inner color (RGB int)

    protected boolean countsTowardLimit = true;
    protected boolean undoable = false;

    public Card(int id, String name, int value, String description) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
        this.outerColorRGB = 0x808080;   // Default gray outer border
        this.innerColorRGB = 0xFFFFFF;   // Default white interior
    }

    public int getId() { return id; }
    public abstract CardType getCardType();

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    // Color
    public int getOuterColorRGB() { return outerColorRGB; }

    public int getInnerColorRGB() { return innerColorRGB; }

    // Undo
    public void setUndoable(boolean undoable) { this.undoable = undoable; }

    public boolean isCountsTowardLimit() { return countsTowardLimit; }
    public void setCountsTowardLimit(boolean c) { this.countsTowardLimit = c; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card c2)) return false;
        return id == c2.id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    public String getImageFileName() { return "propertyWildCard.png"; }

    /** Execute this card's effect when played. Override in subclasses for specific behavior. */
    public void executePlay(Player player, GameSession session, CardPlayOptions opt) {
        session.discardCard(this);
    }

    // ---- Building card queries (overridden by House/Hotel) ----

    public boolean isBuilding() { return false; }
    public boolean isHouse() { return false; }
    public boolean isHotel() { return false; }
    public int getBuildingRentBonus() { return 0; }

    // ---- Play log text (overridden per card type) ----

    public String getPlayLogText(String who, CardPlayOptions opts, GameSession session) {
        return who + " played " + getName();
    }

    @Override
    public String toString() { return getName() + " (" + getValue() + "M)"; }
}