package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.api.ApiClient;
import plugin.nyxel.config.NyxelConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link ApiClient} routes proxy-first and assembles URLs/params
 * correctly, without touching the network.
 */
class ApiClientTest {

    private static Map<String, String> params() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("uuid", "abc123");
        return m;
    }

    @Test
    void proxyRouteEncodesPathAsParam() {
        NyxelConfig.Api api = new NyxelConfig.Api();
        api.proxyUrl = "https://nyxel-proxy.vercel.app/api/hypixel";
        String url = ApiClient.buildUrl(api, "skyblock/profiles", params());
        assertEquals(
                "https://nyxel-proxy.vercel.app/api/hypixel?path=skyblock%2Fprofiles&uuid=abc123",
                url);
    }

    @Test
    void proxyWinsOverKeyWhenBothSet() {
        NyxelConfig.Api api = new NyxelConfig.Api();
        api.proxyUrl = "https://p/api/hypixel";
        api.hypixelKey = "SECRET";
        String url = ApiClient.buildUrl(api, "player", Map.of());
        assertTrue(url.startsWith("https://p/api/hypixel?path=player"));
        assertTrue(!url.contains("api.hypixel.net"));
    }

    @Test
    void directRouteWhenOnlyKeySet() {
        NyxelConfig.Api api = new NyxelConfig.Api();
        api.hypixelKey = "SECRET";
        String url = ApiClient.buildUrl(api, "skyblock/profiles", params());
        assertEquals("https://api.hypixel.net/v2/skyblock/profiles?uuid=abc123", url);
    }

    @Test
    void trailingSlashOnProxyIsTrimmed() {
        NyxelConfig.Api api = new NyxelConfig.Api();
        api.proxyUrl = "https://p/api/hypixel/";
        String url = ApiClient.buildUrl(api, "player", Map.of());
        assertEquals("https://p/api/hypixel?path=player", url);
    }

    @Test
    void nothingConfiguredYieldsNull() {
        assertNull(ApiClient.buildUrl(new NyxelConfig.Api(), "player", Map.of()));
    }
}
