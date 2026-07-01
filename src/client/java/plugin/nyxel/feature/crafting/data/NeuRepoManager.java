package plugin.nyxel.feature.crafting.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import plugin.nyxel.Nyxel;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads the community <b>NotEnoughUpdates-REPO</b> (the definitive source for
 * SkyBlock crafting recipes, which the official API does not expose) and distils
 * its per-item files into a single {@code neu-recipes.json} cached under the game
 * config dir. Subsequent launches load instantly from the cache; a fresh download
 * only happens when the upstream commit changes.
 *
 * <p>Each NEU {@code items/<ID>.json} carries a 3×3 {@code recipe} grid whose cells
 * are {@code "INTERNALNAME:count"}; cells that reference another item form the
 * crafting graph. All network work is best-effort — on any failure the caller keeps
 * whatever data it already had (the bundled seed).
 */
public final class NeuRepoManager {

    private static final String ZIP_URL =
            "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/master.zip";
    private static final String COMMIT_API =
            "https://api.github.com/repos/NotEnoughUpdates/NotEnoughUpdates-REPO/commits/master";

    private static final Gson GSON = new GsonBuilder().create();
    private static final NeuRepoManager INSTANCE = new NeuRepoManager();

    public static NeuRepoManager get() {
        return INSTANCE;
    }

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final Path cacheDir =
            FabricLoader.getInstance().getConfigDir().resolve("nyxel").resolve("neu");
    private final Path recipesCache = cacheDir.resolve("neu-recipes.json");
    private final Path shaFile = cacheDir.resolve("neu.sha");

    private NeuRepoManager() {
    }

    /**
     * Populate {@code repo} from NEU. Loads the local cache first (if present) so
     * the planner is usable immediately, then checks upstream and re-downloads only
     * when the commit changed. Intended to run on the shared background executor.
     */
    public void updateRecipes(RecipeRepository repo) {
        try {
            // Fast path: serve the cached consolidation immediately.
            if (Files.exists(recipesCache)) {
                repo.loadFromJson(Files.readString(recipesCache));
            }
            String remoteSha = fetchRemoteSha();
            String localSha = Files.exists(shaFile) ? Files.readString(shaFile).trim() : "";
            boolean upToDate = remoteSha != null && remoteSha.equals(localSha)
                    && Files.exists(recipesCache);
            if (upToDate) {
                return;
            }
            String json = downloadAndConsolidate();
            if (json == null) {
                return; // keep cache / bundled data
            }
            Files.createDirectories(cacheDir);
            Files.writeString(recipesCache, json);
            if (remoteSha != null) {
                Files.writeString(shaFile, remoteSha);
            }
            repo.loadFromJson(json);
            Nyxel.LOGGER.info("NEU recipes updated from upstream");
        } catch (Exception e) {
            Nyxel.LOGGER.warn("NEU recipe update failed, keeping existing data: {}",
                    e.getMessage());
        }
    }

    private String fetchRemoteSha() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(COMMIT_API))
                    .header("Accept", "application/vnd.github.sha")
                    .timeout(Duration.ofSeconds(15)).GET().build();
            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return resp.body().trim();
            }
        } catch (Exception e) {
            Nyxel.LOGGER.debug("NEU sha check failed: {}", e.getMessage());
        }
        return null;
    }

    /** Stream the repo zip, parse every {@code items/*.json}, return consolidated JSON. */
    private String downloadAndConsolidate() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(ZIP_URL))
                .timeout(Duration.ofSeconds(120)).GET().build();
        HttpResponse<InputStream> resp =
                http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() != 200) {
            Nyxel.LOGGER.warn("NEU zip download HTTP {}", resp.statusCode());
            return null;
        }

        List<OutRecipe> recipes = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(resp.body())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || !name.endsWith(".json")
                        || !name.contains("/items/")) {
                    continue;
                }
                byte[] bytes = zis.readAllBytes();
                OutRecipe r = toRecipe(bytes);
                if (r != null) {
                    recipes.add(r);
                }
            }
        }
        if (recipes.isEmpty()) {
            return null;
        }
        Root root = new Root();
        root.version = 1;
        root.source = "NotEnoughUpdates-REPO (downloaded)";
        root.recipes = recipes;
        return GSON.toJson(root);
    }

    /** Convert one NEU item file into a recipe, or null if it has no craft grid. */
    private OutRecipe toRecipe(byte[] bytes) {
        try {
            NeuItem item = GSON.fromJson(
                    new String(bytes, StandardCharsets.UTF_8), NeuItem.class);
            if (item == null || item.internalname == null || item.recipe == null) {
                return null;
            }
            Map<String, Integer> totals = new LinkedHashMap<>();
            for (String cell : item.recipe.values()) {
                if (cell == null || cell.isBlank()) {
                    continue;
                }
                String id = cell;
                int count = 1;
                int colon = cell.lastIndexOf(':');
                if (colon > 0 && colon < cell.length() - 1) {
                    String tail = cell.substring(colon + 1);
                    if (tail.chars().allMatch(Character::isDigit)) {
                        id = cell.substring(0, colon);
                        count = Integer.parseInt(tail);
                    }
                }
                totals.merge(id, count, Integer::sum);
            }
            if (totals.isEmpty()) {
                return null;
            }
            OutRecipe out = new OutRecipe();
            out.id = item.internalname;
            out.name = strip(item.displayname != null ? item.displayname : item.internalname);
            out.ingredients = new ArrayList<>();
            for (Map.Entry<String, Integer> e : totals.entrySet()) {
                OutIngredient ing = new OutIngredient();
                ing.item = e.getKey();
                ing.count = e.getValue();
                out.ingredients.add(ing);
            }
            return out;
        } catch (Exception e) {
            return null; // skip malformed item files
        }
    }

    private static String strip(String s) {
        return s.replaceAll("§.", "");
    }

    // --- NEU input shape (subset) ---
    private static final class NeuItem {
        String internalname;
        String displayname;
        Map<String, String> recipe;
    }

    // --- our consolidated output shape (matches recipes.json) ---
    private static final class Root {
        int version;
        String source;
        List<OutRecipe> recipes;
    }

    private static final class OutRecipe {
        String id;
        String name;
        List<OutIngredient> ingredients;
    }

    private static final class OutIngredient {
        String item;
        int count;
    }
}
