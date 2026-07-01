package plugin.nyxel.core;

/**
 * Placeholder for planned-but-unimplemented features. Registered so the area
 * structure and config GUI are complete; does nothing until implemented. Always
 * off by default and labelled "(WIP)".
 */
public final class StubFeature implements Feature {

    private final String id;
    private final String displayName;
    private final Category category;

    public StubFeature(String id, String displayName, Category category) {
        this.id = id;
        this.displayName = displayName + " §8(WIP)";
        this.category = category;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public Category category() {
        return category;
    }

    @Override
    public String description() {
        return "Planned — not yet implemented";
    }

    @Override
    public boolean enabledByDefault() {
        return false;
    }
}
