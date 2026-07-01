package plugin.nyxel.core;

/**
 * Capability interface for {@link Feature}s that parse action-bar (overlay) text.
 * The {@link FeatureManager} buckets features by the capability interfaces they
 * implement, so action-bar dispatch only touches features that opt in.
 */
public interface ActionBarListener {

    /** Action-bar (overlay) text update, only while the owning feature is enabled. */
    void onActionBar(String text);
}
