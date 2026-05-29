package com.example.monopoly_deal_game.model.cards;

/**
 * Card type enumeration, aligned with official Monopoly Deal card types.
 * House and Hotel are implemented as subclasses of ActionCard (ActionCardHouse / ActionCardHotel).
 */
public enum CardType {
    PROPERTY,   // Property card
    ACTION,     // Action card
    RENT,       // Rent card
    CURRENCY,   // Currency card
    RULE        // Rule card (4 official cards, may be removed from deck at game start)
}