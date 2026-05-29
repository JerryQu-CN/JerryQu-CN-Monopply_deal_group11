/**
 * <h2>UI Animations (decoupled from card face classes)</h2>
 * <p>
 * Suggestion: Animations only manipulate {@link javafx.scene.Node} geometry and opacity,
 * do not modify {@link com.example.monopoly_deal_game.game.model.GameSession};
 * after the animation ends, the controller calls logic to submit the "animation completed" state
 * (if synchronization with rules is needed).
 * </p>
 *
 * <h3>Inheritance and division of labor</h3>
 * <pre>
 * {@link com.example.monopoly_deal_game.view.animation.UiMotion} — unified entry {@link #play(MotionContext, Runnable)}
 *     ↑
 * {@link com.example.monopoly_deal_game.view.animation.AbstractUiMotion} — template for duration and composing {@link javafx.animation.Animation}
 *     ├── {@link com.example.monopoly_deal_game.view.animation.ShuffleMotion}   — pile shuffle (disorder is visual only; actual order determined by logic)
 *     ├── {@link com.example.monopoly_deal_game.view.animation.DrawMotion}     — from pile to hand
 *     ├── {@link com.example.monopoly_deal_game.view.animation.PlaceMotion}    — land on tabletop/property area/bank bar
 *     └── {@link com.example.monopoly_deal_game.view.animation.MoveMotion}     — translate/reparent existing Node within parent (stealing, returning, etc.)
 * </pre>
 *
 * <p>
 * When combining multiple animations, use {@link javafx.animation.SequentialTransition} /
 * {@link javafx.animation.ParallelTransition} at the call site,
 * or add a small orchestrator class in this package (keeping each class independently testable).
 * </p>
 */
package com.example.monopoly_deal_game.view.animation;
