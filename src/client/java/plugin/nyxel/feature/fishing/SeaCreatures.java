package plugin.nyxel.feature.fishing;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data table mapping a distinctive substring of each sea-creature spawn message
 * to the creature's display name. Detection matches by {@code contains} so minor
 * punctuation differences are tolerated.
 *
 * <p>NOTE: these strings are game data and should be verified/extended against
 * the current Hypixel SkyBlock build. Adding an entry here is all that is needed
 * to support a new creature — no code changes required.
 */
public final class SeaCreatures {

    private SeaCreatures() {
    }

    /** Insertion-ordered so more specific phrases can precede generic ones. */
    public static final Map<String, String> SPAWN_MESSAGES = new LinkedHashMap<>();

    static {
        // Water
        SPAWN_MESSAGES.put("A Squid appears", "Squid");
        SPAWN_MESSAGES.put("You stumbled upon a Sea Walker", "Sea Walker");
        SPAWN_MESSAGES.put("Sea Archer", "Sea Archer");
        SPAWN_MESSAGES.put("Monster of the Deep", "Monster of the Deep");
        SPAWN_MESSAGES.put("catfish", "Catfish");
        SPAWN_MESSAGES.put("Carrot King", "Carrot King");
        SPAWN_MESSAGES.put("Sea Leech", "Sea Leech");
        SPAWN_MESSAGES.put("Guardian Defender", "Guardian Defender");
        SPAWN_MESSAGES.put("Deep Sea Protector", "Deep Sea Protector");
        SPAWN_MESSAGES.put("Hydra", "Water Hydra");
        SPAWN_MESSAGES.put("Sea Emperor", "Sea Emperor");
        SPAWN_MESSAGES.put("Night Squid", "Night Squid");
        // Lava (Crimson Isle)
        SPAWN_MESSAGES.put("Magma Slug", "Magma Slug");
        SPAWN_MESSAGES.put("Moogma", "Moogma");
        SPAWN_MESSAGES.put("Lava Blaze", "Lava Blaze");
        SPAWN_MESSAGES.put("Lava Pigman", "Lava Pigman");
        SPAWN_MESSAGES.put("Flaming Worm", "Flaming Worm");
        SPAWN_MESSAGES.put("Fire Eel", "Fire Eel");
        SPAWN_MESSAGES.put("Taurus", "Taurus");
        SPAWN_MESSAGES.put("Plhlegblast", "Plhlegblast");
        SPAWN_MESSAGES.put("Lord Jawbus", "Lord Jawbus");
        SPAWN_MESSAGES.put("Thunder", "Thunder");
    }

    /** Return the creature name if {@code line} matches a known spawn message. */
    public static String match(String line) {
        for (Map.Entry<String, String> e : SPAWN_MESSAGES.entrySet()) {
            if (line.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }
}
