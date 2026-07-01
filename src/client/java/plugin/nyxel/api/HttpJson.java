package plugin.nyxel.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal JSON-over-HTTP GET helper shared by the API layer. Blocking; callers
 * run it off the render/tick thread.
 */
public final class HttpJson {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    private HttpJson() {
    }

    /** GET the URL as JSON, sending the Hypixel {@code API-Key} header if present. */
    public static JsonObject get(String url, String apiKey) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10)).GET();
        if (apiKey != null && !apiKey.isBlank()) {
            b.header("API-Key", apiKey);
        }
        HttpResponse<String> resp = CLIENT.send(b.build(),
                HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new ApiException(resp.statusCode(), resp.body());
        }
        return JsonParser.parseString(resp.body()).getAsJsonObject();
    }

    /** Thrown on non-200 responses so callers can react to 403/429 etc. */
    public static final class ApiException extends Exception {
        public final int status;

        public ApiException(int status, String body) {
            super("HTTP " + status + ": " + abbreviate(body));
            this.status = status;
        }

        private static String abbreviate(String s) {
            if (s == null) {
                return "";
            }
            return s.length() > 200 ? s.substring(0, 200) : s;
        }
    }
}
