package com.example.monopoly_deal_game.model.cards;

/**
 * Just Say No card: a reactive response card that can be optionally played to cancel an action card effect when an opponent plays an action card against you.
 * After use, this card goes to the discard pile. On your own turn, it can only be deposited into the bank.
 * @deprecated Use {@link ActionCardJustSayNo} directly instead.
 */
@Deprecated
public class JustSayNoCard extends ActionCardJustSayNo {



    public JustSayNoCard(int id) {
        super(id, "Just Say No", 4);
    }
}