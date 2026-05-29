package com.example.monopoly_deal_game.model.cards;

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

    // Card display properties
    protected String[] displayName;      // Multi-line display name
    protected int fontSize = 8;
    protected int displayOffsetY;
    protected int descriptionId;         // Description ID (-1 = no description)

    // Card colors
    protected int outerColorRGB;         // Outer border color (RGB int)
    protected int innerColorRGB;         // Inner color (RGB int)

    protected boolean countsTowardLimit = true;
    protected boolean undoable = false;
    protected boolean clearsUndoableCards = false;

    public Card(int id, String name, int value, String description) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.description = description;
        this.outerColorRGB = 0x808080;   // Default gray outer border
        this.innerColorRGB = 0xFFFFFF;   // Default white interior
        this.descriptionId = -1;
    }

    public int getId() { return id; }
    public abstract CardType getCardType();

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Display
    public String[] getDisplayName() { return displayName; }
    public void setDisplayName(String[] displayName) { this.displayName = displayName; }

    public int getFontSize() { return fontSize; }
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }

    public int getDisplayOffsetY() { return displayOffsetY; }
    public void setDisplayOffsetY(int displayOffsetY) { this.displayOffsetY = displayOffsetY; }

    public int getDescriptionId() { return descriptionId; }
    public void setDescriptionId(int descriptionId) { this.descriptionId = descriptionId; }

    // Color
    public int getOuterColorRGB() { return outerColorRGB; }
    public void setOuterColorRGB(int rgb) { this.outerColorRGB = rgb; }

    public int getInnerColorRGB() { return innerColorRGB; }
    public void setInnerColorRGB(int rgb) { this.innerColorRGB = rgb; }

    // Undo
    public boolean isUndoable() { return undoable; }
    public void setUndoable(boolean undoable) { this.undoable = undoable; }

    public boolean shouldClearUndoableCards() { return clearsUndoableCards; }
    public void setClearsUndoableCards(boolean clears) { this.clearsUndoableCards = clears; }

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

    @Override
    public String toString() { return getName() + " (" + getValue() + "M)"; }
}