package com.example.monopoly_deal_game.model.cards;

import com.example.monopoly_deal_game.game.state.GameSession;
import com.example.monopoly_deal_game.logic.CardPlayOptions;
import com.example.monopoly_deal_game.model.Player;

/**
 * Currency cards providing money denominations for payment and banking.
 */
public class BankCard extends Card {

    /**
     * @param id    Unique card identifier
     * @param name  Name (e.g. "1M", "5M")
     * @param value Denomination (the amount used in payment calculations)
     */
    public BankCard(int id, String name, int value) {
        super(id, name, value, "Currency used for paying debts and rent.");
    }

    @Override
    public CardType getCardType() {
        return CardType.CURRENCY;
    }

    @Override
    public String getImageFileName() { return value + "M.png"; }

    @Override
    public void executePlay(Player player, GameSession session, CardPlayOptions opt) {
        player.getBank().addCard(this);
    }

}
