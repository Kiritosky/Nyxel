package plugin.nyxel.config.gui;

/**
 * Central palette and metrics for the SkyHanni/MoulConfig-style GUI. ARGB ints.
 * Deep, low-chroma darks with a single purple brand accent; toggles use a neutral
 * off state and a green on state (no garish red), matching the restrained look of
 * the mods this is modelled on.
 */
public final class NyxelTheme {

    private NyxelTheme() {
    }

    // Window
    public static final int PANEL_BG = 0xF014141B;
    public static final int PANEL_BORDER = 0xFF3A3A46;
    public static final int SIDEBAR_BG = 0xFF121218;
    public static final int HEADER_BG = 0xFF1B1B23;
    public static final int HEADER_BG_TOP = 0xFF23232E;

    // Rows
    public static final int ROW_BG = 0x2A242430;
    public static final int ROW_HOVER = 0x38454560;
    public static final int SEARCH_BG = 0xFF0E0E13;

    // Accents
    public static final int ACCENT = 0xFFB47CFF;        // purple brand
    public static final int ACCENT_SOFT = 0x33B47CFF;   // selection background
    public static final int ACCENT_ON = 0xFF5BD97C;     // green (toggle on)
    public static final int ACCENT_OFF = 0xFF43434F;    // neutral grey (toggle off)
    public static final int TRACK = 0xFF2C2C38;         // slider / control track
    public static final int KNOB = 0xFFF2F2F5;

    // Text
    public static final int TEXT = 0xFFF2F2F6;
    public static final int TEXT_MUTED = 0xFF9C9CAB;
    public static final int TEXT_DIM = 0xFF66667A;

    // Shadow
    public static final int SHADOW = 0x66000000;

    // Metrics
    public static final int RADIUS = 6;
    public static final int ROW_HEIGHT = 34;
    public static final int PAD = 8;
    public static final int SIDEBAR_W = 104;
    public static final int HEADER_H = 30;
}
