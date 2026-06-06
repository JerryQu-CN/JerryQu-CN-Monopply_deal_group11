package com.example.monopoly_deal_game.view;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

/**
 * Centralized design constants for all programmatic styling.
 * Keep in sync with game-style.css.
 */
public final class Styles {

    private Styles() {}

    // ── Color palette ──────────────────────────────────────────────
    // Background
    public static final String BG_START    = "#FDF6E3";
    public static final String BG_MID      = "#F5ECD7";
    public static final String BG_END      = "#E8DCC8";

    // Top bar
    public static final String TOPBAR_A    = "#4A2810";
    public static final String TOPBAR_B    = "#6B3A1F";

    // Panel
    public static final String PANEL_BG    = "rgba(255,252,245,0.94)";
    public static final String BORDER      = "rgba(139,90,43,0.22)";
    public static final String BORDER_HAND = "rgba(201,162,43,0.35)";

    // Text
    public static final String TEXT_PRIMARY    = "#3E2723";
    public static final String TEXT_SECONDARY  = "#5D4037";
    public static final String TEXT_ON_DARK    = "#FFF8E1";

    // Buttons
    public static final String BTN_GREEN_A = "#2E7D32";
    public static final String BTN_GREEN_B = "#1B5E20";

    // Accent
    public static final String GOLD        = "#C9A22B";
    public static final String DANGER      = "#C62828";
    public static final String CHROME_BG   = "#1A3A5C";
    public static final String PURPLE      = "#6A1B9A";
    public static final String BANK_GREEN  = "#2E7D32";

    // Selection
    public static final String SELECT_BLUE = "#1565C0";
    public static final String SELECT_ORANGE = "#E65100";

    // Card selection glow effects
    public static final DropShadow SELECT_GLOW =
            new DropShadow(14, Color.color(0.15, 0.35, 0.85, 0.55));
    public static final DropShadow STRONG_SELECT_GLOW =
            new DropShadow(28, Color.color(0.95, 0.45, 0.05, 0.82));

    // Card selection CSS borders
    public static final String SELECT_CSS =
            "-fx-border-color:" + SELECT_BLUE + ";-fx-border-width:3;-fx-border-radius:10;-fx-background-radius:10";
    public static final String STRONG_SELECT_CSS =
            "-fx-border-color:" + SELECT_ORANGE + ";-fx-border-width:5;"
            + "-fx-border-radius:11;-fx-background-radius:11;"
            + "-fx-background-color:rgba(255,243,224,0.45)";
    public static final String UNSELECT_CSS =
            "-fx-border-color:transparent;-fx-border-width:0";

    // ── Font sizes ─────────────────────────────────────────────────
    public static final String FS_10 = "-fx-font-size:10px";
    public static final String FS_11 = "-fx-font-size:11px";
    public static final String FS_12 = "-fx-font-size:12px";
    public static final String FS_13 = "-fx-font-size:13px";

    // ── CSS class names (keep in sync with game-style.css) ─────────
    public static final String CLASS_ROOT_BG          = "root-bg";
    public static final String CLASS_TOPBAR            = "topbar";
    public static final String CLASS_TOPBAR_TITLE      = "topbar-title";
    public static final String CLASS_SIDEBAR_PANEL     = "sidebar-panel";
    public static final String CLASS_SIDEBAR_LABEL     = "sidebar-label";
    public static final String CLASS_SIDEBAR_DECK_PANE = "sidebar-deck-pane";
    public static final String CLASS_HUD_MONEY         = "hud-money";
    public static final String CLASS_MOVES_LABEL       = "moves-label";
    public static final String CLASS_FEEDBACK_LABEL    = "feedback-label";
    public static final String CLASS_VERSION_LABEL     = "version-label";
    public static final String CLASS_TABLE_PANEL       = "table-panel";
    public static final String CLASS_HAND_PANEL        = "hand-panel";
    public static final String CLASS_LOG_PANEL         = "log-panel";
    public static final String CLASS_LOG_HEADER        = "log-header";
    public static final String CLASS_LOG_NORMAL        = "log-entry-normal";
    public static final String CLASS_LOG_IMPORTANT     = "log-entry-important";
    public static final String CLASS_LOG_SEPARATOR     = "log-entry-separator";
    public static final String CLASS_ACTION_DOCK       = "action-dock";
    public static final String CLASS_ACTION_DOCK_LABEL = "action-dock-label";
    public static final String CLASS_MENU_OVERLAY      = "menu-overlay";
    public static final String CLASS_MENU_DIALOG       = "menu-dialog";
    public static final String CLASS_MENU_TITLE        = "menu-dialog-title";
    public static final String CLASS_MENU_SUBTITLE     = "menu-dialog-subtitle";
    public static final String CLASS_BTN_PRIMARY       = "button-primary";
    public static final String CLASS_BTN_SECONDARY     = "button-secondary";
    public static final String CLASS_BTN_TOPBAR        = "button-topbar";
    public static final String CLASS_BTN_CHROME        = "button-chrome-action";
    public static final String CLASS_PLAYER_BOARD      = "player-board";
    public static final String CLASS_PLAYER_HEADER     = "player-board-header";
    public static final String CLASS_PLAYER_HEADER_OPP = "player-board-header-opponent";
    public static final String CLASS_PLAYER_HEADER_SELF = "player-board-header-self";
    public static final String CLASS_PROPERTY_COL      = "property-column";
    public static final String CLASS_PROPERTY_LABEL    = "property-column-label";
    public static final String CLASS_BANK_HEADER       = "bank-section-header";
    public static final String CLASS_BANK_TOTAL        = "bank-section-total";
    public static final String CLASS_SCROLL_TRANSPARENT = "scroll-pane-transparent";
}
