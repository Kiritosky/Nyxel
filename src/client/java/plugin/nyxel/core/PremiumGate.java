package plugin.nyxel.core;

/**
 * Single choke point for premium gating. Quality-first: everything is unlocked
 * locally for now, but features ask through here so real validation (license key
 * / auth backend) can be wired in later without touching feature code.
 */
public final class PremiumGate {

    private PremiumGate() {
    }

    /** Whether the user has premium access at all. */
    public static boolean isPremium() {
        return true;
    }

    /** Whether a specific premium feature is unlocked. */
    public static boolean isUnlocked(String featureId) {
        return isPremium();
    }
}
