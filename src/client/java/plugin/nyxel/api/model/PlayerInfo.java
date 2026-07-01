package plugin.nyxel.api.model;

/**
 * Resolved snapshot of the player's active SkyBlock profile used across Ironman
 * features: whether it's an Ironman profile, the profile id/name, and the garden
 * data. {@link #available} is false until a successful API fetch.
 */
public final class PlayerInfo {

    public final boolean available;
    public final boolean ironman;
    public final String profileId;
    public final String cuteName;
    public final GardenData garden;

    public PlayerInfo(boolean available, boolean ironman, String profileId,
                      String cuteName, GardenData garden) {
        this.available = available;
        this.ironman = ironman;
        this.profileId = profileId;
        this.cuteName = cuteName;
        this.garden = garden;
    }

    public static PlayerInfo unavailable() {
        return new PlayerInfo(false, false, "", "", GardenData.empty());
    }
}
