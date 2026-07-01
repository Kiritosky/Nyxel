package plugin.nyxel.gui;

/**
 * GUI palette for Nyxel's config screen. ARGB ints.
 *
 * <p>Ported from the MIT-licensed <b>SkyOS</b> project
 * (https://github.com/olb-freelocs/skyos, Copyright (c) 2025 Freelocs) — see
 * {@code licenses/skyos-MIT.txt}. Translated from Kotlin to Java.
 */
public final class NyxelColors {

    private NyxelColors() {
    }

    // Backgrounds
    public static final int BG_OVERLAY = 0xCC000000;
    public static final int BG_PANEL = 0xF212121F;
    public static final int BG_SIDEBAR = 0xF50D0D1A;
    public static final int BG_CARD = 0xFF16172A;
    public static final int BG_ELEVATED = 0xFF1C1D32;
    public static final int BG_HOVER = 0x0DFFFFFF;
    public static final int BG_ACTIVE = 0x15FFFFFF;

    // Borders
    public static final int BORDER = 0x14FFFFFF;
    public static final int BORDER_HI = 0x1AFFFFFF;

    // Foregrounds
    public static final int FG_PRIMARY = 0xFFF0F0FF;
    public static final int FG_SECONDARY = 0xA6F0F0FF;
    public static final int FG_MUTED = 0x66F0F0FF;
    public static final int FG_DISABLED = 0x59F0F0FF;

    // Brand gradient stops
    public static final int BRAND_GOLD = 0xFFFFB800;
    public static final int BRAND_ORANGE = 0xFFFF7A00;
    public static final int BRAND_PINK = 0xFFFF4ECD;
    public static final int BRAND_MAGENTA = 0xFFD83FFF;
    public static final int BRAND_PURPLE = 0xFF7B3FFF;
    public static final int BRAND_CYAN = 0xFF00D4FF;
    public static final int BRAND_SKY = 0xFF00AAFF;

    public static final int[] BRAND_GRADIENT = {
            BRAND_GOLD, BRAND_ORANGE, BRAND_PINK, BRAND_MAGENTA, BRAND_PURPLE, BRAND_CYAN, BRAND_SKY
    };

    // Semantic
    public static final int SUCCESS = 0xFF4ADE80;
    public static final int WARNING = 0xFFF5A623;
    public static final int ERROR = 0xFFFF6B6B;
    public static final int INFO = 0xFF4F8EF7;

    // Toggles
    public static final int TOGGLE_OFF = 0x26FFFFFF;
    public static final int TOGGLE_ON_L = 0xFF7B3FFF;
    public static final int TOGGLE_ON_R = 0xFF00D4FF;
    public static final int TOGGLE_KNOB = 0xFFFFFFFF;

    // Sidebar selection
    public static final int SIDEBAR_ACTIVE = 0x20FFFFFF;
    public static final int SIDEBAR_HOVER = 0x0FFFFFFF;

    /** A stable accent color per category, cycled from the brand gradient. */
    public static int accentFor(int categoryIndex) {
        return BRAND_GRADIENT[Math.floorMod(categoryIndex, BRAND_GRADIENT.length)];
    }
}
