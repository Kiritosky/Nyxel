package plugin.nyxel.api.model;

/**
 * Resolved snapshot of the player's active SkyBlock profile: whether it's an
 * Ironman profile and the profile id/name. {@link #available} is false until a
 * successful API fetch. Feature-specific profile data (garden, collections, …)
 * is added back by the features that need it.
 */
public final class PlayerInfo {

    public final boolean available;
    public final boolean ironman;
    public final String profileId;
    public final String cuteName;

    public PlayerInfo(boolean available, boolean ironman, String profileId, String cuteName) {
        this.available = available;
        this.ironman = ironman;
        this.profileId = profileId;
        this.cuteName = cuteName;
    }

    public static PlayerInfo unavailable() {
        return new PlayerInfo(false, false, "", "");
    }
}
