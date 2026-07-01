package plugin.nyxel.api;

import com.google.gson.JsonObject;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.NyxelConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Baseplate data source #2: live Hypixel data. The single entry point every
 * feature uses — callers pass a v2 path and parameters and never touch headers or
 * URLs:
 *
 * <pre>{@code ApiClient.get("skyblock/profiles", Map.of("uuid", uuid)); }</pre>
 *
 * Routing (in order):
 * <ol>
 *   <li>{@code api.proxyUrl} set → through the backend proxy (no key client-side),
 *       sending {@code X-Nyxel-Token}. The key lives only on the proxy.</li>
 *   <li>else {@code api.hypixelKey} set → directly to Hypixel with {@code API-Key}.</li>
 *   <li>else → unavailable (the mod runs keyless off scoreboard/chat).</li>
 * </ol>
 */
public final class ApiClient {

    private static final String BASE = "https://api.hypixel.net/v2";

    private ApiClient() {
    }

    /** True when a proxy URL or a personal key is configured. */
    public static boolean isConfigured() {
        NyxelConfig.Api api = ConfigManager.get().api;
        return notBlank(api.proxyUrl) || notBlank(api.hypixelKey);
    }

    /**
     * GET a Hypixel v2 endpoint by path + params, routed per config.
     *
     * @throws IllegalStateException if no API source is configured
     * @throws Exception on transport / non-200 responses
     */
    public static JsonObject get(String path, Map<String, String> params) throws Exception {
        NyxelConfig.Api api = ConfigManager.get().api;
        String url = buildUrl(api, path, params);
        if (url == null) {
            throw new IllegalStateException("Nyxel: no API source configured (proxyUrl/hypixelKey)");
        }
        if (notBlank(api.proxyUrl)) {
            return HttpJson.get(url, "X-Nyxel-Token", api.modToken);
        }
        return HttpJson.get(url, "API-Key", api.hypixelKey);
    }

    /**
     * Assemble the request URL for the configured source (proxy-first), or null if
     * nothing is configured. Exposed so the routing can be unit-tested without
     * touching the network.
     */
    public static String buildUrl(NyxelConfig.Api api, String path, Map<String, String> params) {
        String query = encodeParams(params);
        if (notBlank(api.proxyUrl)) {
            return trimTrailingSlash(api.proxyUrl) + "?path=" + enc(path)
                    + (query.isEmpty() ? "" : "&" + query);
        }
        if (notBlank(api.hypixelKey)) {
            return BASE + "/" + path + (query.isEmpty() ? "" : "?" + query);
        }
        return null;
    }

    private static String encodeParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        return sb.toString();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    private static String trimTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
