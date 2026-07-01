package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.feature.fishing.SeaCreatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SeaCreaturesTest {

    @Test
    void matchesKnownWaterCreature() {
        assertEquals("Squid", SeaCreatures.match("A Squid appears."));
    }

    @Test
    void matchesKnownLavaCreature() {
        assertEquals("Lord Jawbus",
                SeaCreatures.match("You feel the heat... Lord Jawbus surfaces!"));
    }

    @Test
    void returnsNullForUnrelatedLine() {
        assertNull(SeaCreatures.match("You are now in the Hub."));
    }
}
