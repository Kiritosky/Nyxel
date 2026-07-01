package plugin.nyxel.config.gui;

/**
 * Central palette and metrics for the SkyHanni/MoulConfig-style GUI. ARGB ints.
 */
public final class NyxelTheme {

    private NyxelTheme() {
    }

    // Window
    public static final int PANEL_BG = 0xEE1A1A20;
    public static final int PANEL_BORDER = 0xFF2E2E38;
    public static final int SIDEBAR_BG = 0xFF15151A;
    public static final int HEADER_BG = 0xFF202028;

    // Rows
    public static final int ROW_BG = 0x22202028; // subtle translucent
    public static final int ROW_HOVER = 0x44303048;
    public static final int SEARCH_BG = 0xFF101014;

    // Accents
    public static final int ACCENT = 0xFFB14BFF;       // purple
    public static final int ACCENT_ON = 0xFF55E07A;    // green (toggle on)
    public static final int ACCENT_OFF = 0xFFC04646;   // red (toggle off)
    public static final int KNOB = 0xFFEDEDED;

    // Text
    public static final int TEXT = 0xFFF0F0F5;
    public static final int TEXT_MUTED = 0xFF9A9AA8;
    public static final int TEXT_DIM = 0xFF6A6A78;

    // Metrics
    public static final int RADIUS = 4;
    public static final int ROW_HEIGHT = 34;
    public static final int PAD = 8;
    public static final int SIDEBAR_W = 104;
    public static final int HEADER_H = 30;
}
