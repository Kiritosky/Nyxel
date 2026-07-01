package plugin.nyxel.data.neu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import plugin.nyxel.Nyxel;
import plugin.nyxel.core.NyxelExecutor;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Baseplate data source #1: the community <b>NotEnoughUpdates-REPO</b>, the
 * definitive source of static SkyBlock item data (recipes, NPC sell prices,
 * display names) that the official API does not expose.
 *
 * <p>On {@link #load()} it serves a local cache instantly, then — if the upstream
 * commit changed — streams the whole repo zip ({@link ZipInputStream}, no extra
 * dependency), distils every {@code items/*.json} into a compact
 * {@code neu-items.json} under the game config dir, and swaps it in. All network
 * work runs on the shared executor and is best-effort; a failed refresh keeps the
 * existing cache. Features query the loaded {@link NeuItem}s.
 */
public final class NeuRepoService {

    private static final String ZIP_URL =
            "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/master.zip";
    private static final String COMMIT_API =
            "https://api.github.com/repos/NotEnoughUpdates/NotEnoughUpdates-REPO/commits/master";

    private static final Gson GSON = new GsonBuilder().create();
    private static final Type ITEM_LIST = new TypeToken<List<NeuItem>>() {}.getType();
    private static final NeuRepoService INSTANCE = new NeuRepoService();

    public static NeuRepoService get() {
        return INSTANCE;
    }

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final Path cacheDir =
            FabricLoader.getInstance().getConfigDir().resolve("nyxel").resolve("neu");
    private final Path itemsCache = cacheDir.resolve("neu-items.json");
    private final Path shaFile = cacheDir.resolve("neu.sha");

    private volatile Map<String, NeuItem> items = new LinkedHashMap<>();

    private NeuRepoService() {
    }

    /** Load the cache immediately (if any) and kick off a background refresh. */
    public void load() {
        try {
            if (Files.exists(itemsCache)) {
                index(GSON.fromJson(Files.readString(itemsCache), ITEM_LIST));
            }
        } catch (Exception e) {
            Nyxel.LOGGER.warn("NEU cache read failed: {}", e.getMessage());
        }
        NyxelExecutor.run("neu-refresh", this::refresh);
    }

    // --- lookups ---

    public NeuItem byId(String internalName) {
        return internalName == null ? null : items.get(internalName);
    }

    public Collection<NeuItem> all() {
        return items.values();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // --- refresh pipeline ---

    private void refresh() {
        try {
            String remoteSha = fetchRemoteSha();
            String localSha = Files.exists(shaFile) ? Files.readString(shaFile).trim() : "";
            if (remoteSha != null && remoteSha.equals(localSha) && Files.exists(itemsCache)) {
                return; // up to date
            }
            List<NeuItem> parsed = downloadAndConsolidate();
            if (parsed == null || parsed.isEmpty()) {
                return; // keep existing cache
            }
            Files.createDirectories(cacheDir);
            Files.writeString(itemsCache, GSON.toJson(parsed, ITEM_LIST));
            if (remoteSha != null) {
                Files.writeString(shaFile, remoteSha);
            }
            index(parsed);
            Nyxel.LOGGER.info("NEU items updated from upstream ({} items)", parsed.size());
        } catch (Exception e) {
            Nyxel.LOGGER.warn("NEU refresh failed, keeping cache: {}", e.getMessage());
        }
    }

    private void index(List<NeuItem> list) {
        if (list == null) {
            return;
        }
        Map<String, NeuItem> map = new LinkedHashMap<>(list.size());
        for (NeuItem it : list) {
            if (it != null && it.internalName != null) {
                map.put(it.internalName, it);
            }
        }
        this.items = map;
    }

    private String fetchRemoteSha() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(COMMIT_API))
                    .header("Accept", "application/vnd.github.sha")
                    .timeout(Duration.ofSeconds(15)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return resp.body().trim();
            }
        } catch (Exception e) {
            Nyxel.LOGGER.debug("NEU sha check failed: {}", e.getMessage());
        }
        return null;
    }

    private List<NeuItem> downloadAndConsolidate() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(ZIP_URL))
                .timeout(Duration.ofSeconds(120)).GET().build();
        HttpResponse<InputStream> resp =
                http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() != 200) {
            Nyxel.LOGGER.warn("NEU zip download HTTP {}", resp.statusCode());
            return null;
        }
        List<NeuItem> out = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(resp.body())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || !name.endsWith(".json") || !name.contains("/items/")) {
                    continue;
                }
                NeuItem item = toItem(zis.readAllBytes());
                if (item != null) {
                    out.add(item);
                }
            }
        }
        return out;
    }

    /** Convert one NEU item file into our compact {@link NeuItem}. */
    private NeuItem toItem(byte[] bytes) {
        try {
            NeuRaw raw = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), NeuRaw.class);
            if (raw == null || raw.internalname == null) {
                return null;
            }
            NeuItem item = new NeuItem();
            item.internalName = raw.internalname;
            item.displayName = strip(raw.displayname != null ? raw.displayname : raw.internalname);
            item.npcSell = raw.npc_sell_price;
            if (raw.recipe != null) {
                for (Object cell : raw.recipe.values()) {
                    if (cell == null) {
                        continue;
                    }
                    String s = String.valueOf(cell);
                    if (s.isBlank()) {
                        continue;
                    }
                    String id = s;
                    int count = 1;
                    int colon = s.lastIndexOf(':');
                    if (colon > 0 && colon < s.length() - 1
                            && s.substring(colon + 1).chars().allMatch(Character::isDigit)) {
                        id = s.substring(0, colon);
                        count = Integer.parseInt(s.substring(colon + 1));
                    }
                    item.recipe.merge(id, count, Integer::sum);
                }
            }
            return item;
        } catch (Exception e) {
            return null; // skip malformed item files
        }
    }

    private static String strip(String s) {
        return s.replaceAll("§.", "");
    }

    /** Subset of a NEU item file we read. */
    private static final class NeuRaw {
        String internalname;
        String displayname;
        Long npc_sell_price;
        Map<String, Object> recipe;
    }
}
