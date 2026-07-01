package plugin.nyxel.feature.economy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import plugin.nyxel.Nyxel;
import plugin.nyxel.config.ConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Fetches and caches Bazaar prices from Hypixel's <b>public</b> API endpoint
 * ({@code /v2/skyblock/bazaar}, no key required). Refreshes lazily on a
 * background thread when the cache is older than the configured TTL, so callers
 * (tooltips, fishing profit) never block on the network.
 */
public final class BazaarClient {

    private static final String URL = "https://api.hypixel.net/v2/skyblock/bazaar";
    private static final BazaarClient INSTANCE = new BazaarClient();

    public static BazaarClient get() {
        return INSTANCE;
    }

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, Price> prices = new ConcurrentHashMap<>();
    private final AtomicBoolean refreshing = new AtomicBoolean(false);
    private volatile long lastFetch = 0L;

    private BazaarClient() {
    }

    /** Instant buy price (what you pay) for a Bazaar product id, if known. */
    public Optional<Double> buyPrice(String productId) {
        maybeRefresh();
        Price p = prices.get(productId);
        return p == null ? Optional.empty() : Optional.of(p.buy);
    }

    /** Instant sell price (what you receive) for a Bazaar product id, if known. */
    public Optional<Double> sellPrice(String productId) {
        maybeRefresh();
        Price p = prices.get(productId);
        return p == null ? Optional.empty() : Optional.of(p.sell);
    }

    private void maybeRefresh() {
        long ttlMs = ConfigManager.get().economy.priceCacheSeconds * 1000L;
        if (System.currentTimeMillis() - lastFetch < ttlMs) {
            return;
        }
        if (!refreshing.compareAndSet(false, true)) {
            return; // a refresh is already in flight
        }
        Thread t = new Thread(this::fetch, "Nyxel-Bazaar");
        t.setDaemon(true);
        t.start();
    }

    private void fetch() {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(URL))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> resp =
                    http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                Nyxel.LOGGER.warn("Bazaar fetch HTTP {}", resp.statusCode());
                return;
            }
            JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
            JsonObject products = root.getAsJsonObject("products");
            for (String id : products.keySet()) {
                JsonObject status = products.getAsJsonObject(id)
                        .getAsJsonObject("quick_status");
                double buy = status.has("buyPrice") ? status.get("buyPrice").getAsDouble() : 0;
                double sell = status.has("sellPrice") ? status.get("sellPrice").getAsDouble() : 0;
                prices.put(id, new Price(buy, sell));
            }
            lastFetch = System.currentTimeMillis();
        } catch (Exception e) {
            Nyxel.LOGGER.error("Bazaar fetch failed", e);
        } finally {
            refreshing.set(false);
        }
    }

    private record Price(double buy, double sell) {
    }
}
