package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.feature.crafting.data.RecipeRepository;
import plugin.nyxel.feature.economy.data.NpcPriceRepository;
import plugin.nyxel.feature.garden.data.MutationRepository;
import plugin.nyxel.feature.general.data.MinionRepository;
import plugin.nyxel.feature.hud.data.CollectionRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Loads every bundled dataset from its shipped file and fails the build if one is
 * malformed or empty. This guards the hand-authored JSON tables — a typo that
 * breaks parsing surfaces here instead of silently at runtime.
 */
class DataSchemaTest {

    private static final Path DATA_DIR =
            Path.of("src/client/resources/assets/nyxel/data");

    private static String read(String file) throws IOException {
        return Files.readString(DATA_DIR.resolve(file));
    }

    @Test
    void mutationsParse() throws IOException {
        MutationRepository repo = new MutationRepository();
        repo.loadFromJson(read("mutations.json"));
        assertFalse(repo.isEmpty(), "mutations.json produced no entries");
    }

    @Test
    void recipesParse() throws IOException {
        RecipeRepository repo = new RecipeRepository();
        repo.loadFromJson(read("recipes.json"));
        assertFalse(repo.isEmpty(), "recipes.json produced no entries");
    }

    @Test
    void npcPricesParse() throws IOException {
        NpcPriceRepository repo = new NpcPriceRepository();
        repo.loadFromJson(read("npc_prices.json"));
        assertFalse(repo.isEmpty(), "npc_prices.json produced no entries");
    }

    @Test
    void collectionsParse() throws IOException {
        CollectionRepository repo = new CollectionRepository();
        repo.loadFromJson(read("collections.json"));
        assertFalse(repo.isEmpty(), "collections.json produced no entries");
    }

    @Test
    void minionsParseWithFuels() throws IOException {
        MinionRepository repo = new MinionRepository();
        repo.loadFromJson(read("minions.json"));
        assertFalse(repo.isEmpty(), "minions.json produced no minions");
        assertFalse(repo.fuels().isEmpty(), "minions.json produced no fuels");
    }
}
