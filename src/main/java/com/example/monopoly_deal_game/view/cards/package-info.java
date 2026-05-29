/**
 * <h2>Card GUI (parallel to the {@link com.example.monopoly_deal_game.model.cards.Card} family)</h2>
 * <p>
 * Suggestion: <b>what the card face looks like, Tooltip, click feedback</b> belong in this package;
 * <b>trajectory animations like flying from pile to hand</b> belong in
 * {@link com.example.monopoly_deal_game.view.animation}, driven by animation classes
 * through {@link javafx.scene.Node#setTranslateX(double)} etc.,
 * avoiding writing JavaFX in the Card model.
 * </p>
 *
 * <h3>Inheritance (implementation order reference)</h3>
 * <pre>
 * {@link com.example.monopoly_deal_game.model.cards.Card} (model)
 *     ├── PropertyCard  →  {@link com.example.monopoly_deal_game.view.cards.PropertyCardNode}
 *     ├── ActionCard    →  {@link com.example.monopoly_deal_game.view.cards.ActionCardNode}
 *     └── BankCard      →  {@link com.example.monopoly_deal_game.view.cards.BankCardNode}
 *
 * {@link com.example.monopoly_deal_game.view.cards.AbstractCardNode} (JavaFX Region, common card face traits)
 *     ├── PropertyCardNode
 *     ├── ActionCardNode
 *     └── BankCardNode
 * </pre>
 *
 * <p>
 * Factory: {@link com.example.monopoly_deal_game.view.cards.CardNodeFactory#from(com.example.monopoly_deal_game.model.cards.Card)},
 * Controller or {@link com.example.monopoly_deal_game.view.GameplayViewCoordinator} depends only on
 * {@link com.example.monopoly_deal_game.model.cards.Card},
 * not directly instantiating specific Node subclasses.
 * </p>
 */
package com.example.monopoly_deal_game.view.cards;
