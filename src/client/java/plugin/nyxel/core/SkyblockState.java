package plugin.nyxel.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives high-level SkyBlock context from the sidebar scoreboard each tick:
 * whether the player is on SkyBlock, the current zone/island, the purse, and a
 * convenience lava-vs-water fishing context. Other features read this instead of
 * re-parsing the scoreboard themselves.
 */
public final class SkyblockState {

    /** Zones where fishing happens in lava (need lava-appropriate gear). */
    private static final Set<String> LAVA_ZONES = Set.of(
            "Crimson Isle", "Crystal Hollows", "Magma Fields", "Burning Desert",
            "Blazing Volcano", "Mucus Tunnel"
    );

    private static final Pattern PURSE_PATTERN =
            Pattern.compile("(?:Purse|Piggy):\\s*([\\d,]+)");
    private static final Pattern ZONE_PATTERN = Pattern.compile("⏣\\s*(.+)");

    private boolean onSkyblock = false;
    private String zone = "";
    private long purse = 0;
    private List<String> sidebarLines = Collections.emptyList();

    /** Read the sidebar once per tick. Cheap and defensive against null world. */
    public void update(MinecraftClient mc) {
        ClientWorld world = mc.world;
        if (world == null) {
            reset();
            return;
        }
        List<String> lines = readSidebar(world.getScoreboard());
        this.sidebarLines = lines;
        if (lines.isEmpty()) {
            reset();
            return;
        }

        // The sidebar title (objective display name) reads "SKYBLOCK" on Hypixel.
        boolean sky = false;
        for (String line : lines) {
            if (line.toUpperCase().contains("SKYBLOCK")) {
                sky = true;
                break;
            }
        }
        this.onSkyblock = sky;

        for (String line : lines) {
            Matcher zm = ZONE_PATTERN.matcher(line);
            if (zm.find()) {
                this.zone = zm.group(1).trim();
            }
            Matcher pm = PURSE_PATTERN.matcher(line);
            if (pm.find()) {
                try {
                    this.purse = Long.parseLong(pm.group(1).replace(",", ""));
                } catch (NumberFormatException ignored) {
                    // leave previous value
                }
            }
        }
    }

    public boolean onSkyblock() {
        return onSkyblock;
    }

    public String zone() {
        return zone;
    }

    public long purse() {
        return purse;
    }

    public List<String> sidebarLines() {
        return sidebarLines;
    }

    /** True when the current zone is one where fishing happens in lava. */
    public boolean isLavaFishingZone() {
        return LAVA_ZONES.contains(zone);
    }

    private void reset() {
        onSkyblock = false;
        zone = "";
        sidebarLines = Collections.emptyList();
    }

    /**
     * Read the sidebar slot into a list of formatting-stripped strings, top to
     * bottom. Wrapped defensively because scoreboard internals are mapping
     * sensitive across versions.
     */
    private static List<String> readSidebar(Scoreboard scoreboard) {
        List<String> result = new ArrayList<>();
        try {
            ScoreboardObjective objective =
                    scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            if (objective == null) {
                return result;
            }
            // Title line first.
            result.add(objective.getDisplayName().getString());
            List<ScoreboardEntry> entries = new ArrayList<>(
                    scoreboard.getScoreboardEntries(objective));
            // Highest score renders at the top; sort descending.
            entries.sort((a, b) -> Integer.compare(b.value(), a.value()));
            for (ScoreboardEntry entry : entries) {
                if (entry.hidden()) {
                    continue;
                }
                Team team = scoreboard.getScoreHolderTeam(entry.owner());
                Text decorated = Team.decorateName(team, entry.name());
                result.add(decorated.getString());
            }
        } catch (Throwable t) {
            // Never let a mapping surprise crash the client; degrade to no data.
            return Collections.emptyList();
        }
        return result;
    }
}
