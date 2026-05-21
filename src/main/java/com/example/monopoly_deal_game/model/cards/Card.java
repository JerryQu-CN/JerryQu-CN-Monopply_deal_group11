package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.model.Player;

import java.io.Serial;
import java.io.Serializable;

public abstract class Card implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    protected String description;
    protected int value;
    protected boolean countsTowardLimit = true;
    public Card(int id, String name, int value, String description) { this.id=id; this.name=name; this.value=value; this.description=description; }
    public int getId(){return id;}
    public abstract CardType getCardType();
    public abstract void use(Player user, Player target);
    public int getValue(){return value;}
    public String getName(){return name;}
    public String getDescription(){return description;}
    public boolean isCountsTowardLimit(){return countsTowardLimit;}
}
