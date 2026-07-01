package plugin.nyxel.config;

/**
 * Declarative description of a single per-feature config option. Features return
 * these from {@code Feature.configOptions()}; a future GUI turns each into a
 * concrete control bound to the feature's namespaced option map ({@link FeatureOptions}).
 * Kept GUI-free so the config model doesn't depend on any rendering layer.
 */
public final class OptionSpec {

    public enum Kind {TOGGLE, SLIDER}

    public final Kind kind;
    public final String key;
    public final String label;
    public final String description;

    // Slider bounds / default; unused for toggles.
    public final int min;
    public final int max;
    public final int defInt;

    // Toggle default; unused for sliders.
    public final boolean defBool;

    private OptionSpec(Kind kind, String key, String label, String description,
                       int min, int max, int defInt, boolean defBool) {
        this.kind = kind;
        this.key = key;
        this.label = label;
        this.description = description;
        this.min = min;
        this.max = max;
        this.defInt = defInt;
        this.defBool = defBool;
    }

    public static OptionSpec toggle(String key, String label, String description,
                                    boolean defaultOn) {
        return new OptionSpec(Kind.TOGGLE, key, label, description, 0, 0, 0, defaultOn);
    }

    public static OptionSpec slider(String key, String label, String description,
                                    int min, int max, int defaultValue) {
        return new OptionSpec(Kind.SLIDER, key, label, description, min, max, defaultValue, false);
    }
}
