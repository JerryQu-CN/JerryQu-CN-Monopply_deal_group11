package com.example.monopoly_deal_game.view.scene;

import com.example.monopoly_deal_game.view.GameplayUiBundle;
import javafx.scene.layout.Pane;

import java.util.Objects;

/**
 * Maps {@link UiSceneLayer} to containers within {@link GameplayUiBundle} for mounting animations and popups.
 */
public final class ScenePaneResolver {

    private final GameplayUiBundle bundle;

    public ScenePaneResolver(GameplayUiBundle bundle) {
        this.bundle = Objects.requireNonNull(bundle);
    }

    public Pane paneFor(UiSceneLayer layer) {
        return switch (layer) {
            case TABLE -> bundle.gameRoot();
            case ACTION_MODAL -> bundle.actionLayer();
            case MENU_OVERLAY -> bundle.menuOverlay();
        };
    }
}
