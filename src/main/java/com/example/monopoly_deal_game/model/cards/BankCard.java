package com.example.monopoly_deal_game.model.cards;

/**
 * Currency cards (M-denomination, etc.).
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Store the currency denomination.</li>
 *   <li>Can only be deposited into the bank area or used as payment; has no action effects.</li>
 * </ol>
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

}
