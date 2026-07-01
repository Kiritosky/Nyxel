package plugin.nyxel;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (main source set) entrypoint. Holds shared constants and the logger so
 * both the main and client source sets can reference them.
 */
public class Nyxel implements ModInitializer {

    public static final String MOD_ID = "nyxel";
    public static final String MOD_NAME = "Nyxel";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        // Nyxel is a client-only mod; all behaviour is wired up in NyxelClient.
        LOGGER.info("[{}] common init", MOD_NAME);
    }
}
