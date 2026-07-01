package plugin.nyxel.feature.common.data;

import com.google.gson.Gson;
import plugin.nyxel.Nyxel;
import plugin.nyxel.api.HttpJson;
import plugin.nyxel.core.NyxelExecutor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Base for repositories backed by a bundled JSON table under
 * {@code /assets/nyxel/data/}. Handles resource IO and {@code byId}/{@code byName}
 * caching; subclasses only describe their schema:
 *
 * <ul>
 *   <li>{@link #resourcePath()} — classpath location of the bundled file;</li>
 *   <li>{@link #parse(String)} — Gson-parse a JSON string into the item list (the
 *       subclass owns its {@code Root} wrapper type);</li>
 *   <li>{@link #idOf}/{@link #nameOf} — the lookup keys.</li>
 * </ul>
 *
 * The bundled file is the baseline; {@link #tryLoadRemote(String)} allows a remote
 * override so game updates need not ship a mod update, with bundled as fallback.
 * This generalizes the pattern originally written for the garden mutation dataset.
 *
 * @param <T> the record type held by this repository
 */
public abstract class DataRepository<T> {

    protected static final Gson GSON = new Gson();

    private final Map<String, T> byId = new LinkedHashMap<>();
    private final Map<String, T> byName = new HashMap<>();

    /** Classpath path of the bundled dataset, e.g. {@code /assets/nyxel/data/x.json}. */
    protected abstract String resourcePath();

    /** Parse a JSON document into the flat list of items (subclass owns the schema). */
    protected abstract List<T> parse(String json);

    /** Stable id used as the primary lookup key. */
    protected abstract String idOf(T item);

    /** Display name used for the case-insensitive secondary lookup. */
    protected abstract String nameOf(T item);

    /** Human label for log lines (e.g. "recipes"). */
    protected String label() {
        return getClass().getSimpleName();
    }

    /**
     * Optional live source (a Hypixel resources endpoint or a community dataset).
     * When present, {@link #refresh()} loads it asynchronously over the bundled
     * fallback. {@code null} means bundled-only.
     */
    protected String remoteUrl() {
        return null;
    }

    /**
     * Parse the {@link #remoteUrl()} document. Defaults to {@link #parse(String)}
     * (same schema as the bundled file); override when the live source uses a
     * different shape, e.g. the Hypixel resources API.
     */
    protected List<T> parseRemote(String json) {
        return parse(json);
    }

    /**
     * Load the bundled fallback immediately, then — if a {@link #remoteUrl()} is
     * configured — refresh from the live source on the shared background executor
     * so the caller never blocks on the network.
     */
    public final void refresh() {
        loadBundled();
        String url = remoteUrl();
        if (url != null && !url.isBlank()) {
            NyxelExecutor.run(label() + "-remote", () -> tryLoadRemote(url));
        }
    }

    /** Load the bundled dataset from the classpath. */
    public final void loadBundled() {
        try (InputStream in = getClass().getResourceAsStream(resourcePath())) {
            if (in == null) {
                Nyxel.LOGGER.error("{} not found on classpath", resourcePath());
                return;
            }
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            index(parse(json));
            Nyxel.LOGGER.info("Loaded {} {} (bundled)", byId.size(), label());
        } catch (Exception e) {
            Nyxel.LOGGER.error("Failed to load bundled {}", resourcePath(), e);
        }
    }

    /** Load directly from a JSON string (used by tests and remote loading). */
    public final void loadFromJson(String json) {
        index(parse(json));
    }

    /** Best-effort remote override; keeps existing data if it fails or is empty. */
    public final void tryLoadRemote(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            String json = HttpJson.get(url, null).toString();
            List<T> items = parseRemote(json);
            if (items != null && !items.isEmpty()) {
                byId.clear();
                byName.clear();
                index(items);
                Nyxel.LOGGER.info("Loaded {} {} (remote)", byId.size(), label());
            }
        } catch (Exception e) {
            Nyxel.LOGGER.warn("Remote {} dataset failed, keeping bundled: {}",
                    label(), e.getMessage());
        }
    }

    private void index(List<T> items) {
        if (items == null) {
            return;
        }
        for (T item : items) {
            if (item == null) {
                continue;
            }
            String id = idOf(item);
            String name = nameOf(item);
            if (id == null || name == null) {
                continue;
            }
            byId.put(id, item);
            byName.put(name.toLowerCase(Locale.ROOT), item);
        }
    }

    public final Collection<T> all() {
        return byId.values();
    }

    public final T byId(String id) {
        return id == null ? null : byId.get(id);
    }

    public final T byName(String name) {
        return name == null ? null : byName.get(name.toLowerCase(Locale.ROOT));
    }

    public final boolean isEmpty() {
        return byId.isEmpty();
    }
}
