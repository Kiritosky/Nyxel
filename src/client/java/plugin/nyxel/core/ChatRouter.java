package plugin.nyxel.core;

import plugin.nyxel.Nyxel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Single chat sink. Features register a regex + callback instead of each hooking
 * the chat event directly. Patterns are matched against the plain (formatting
 * stripped) text of every received game message.
 */
public final class ChatRouter {

    private static final ChatRouter INSTANCE = new ChatRouter();

    public static ChatRouter get() {
        return INSTANCE;
    }

    private final List<Subscription> subscriptions = new ArrayList<>();

    private ChatRouter() {
    }

    /**
     * Subscribe to messages matching {@code pattern}. The callback receives the
     * successful {@link Matcher} so capture groups can be read.
     */
    public void subscribe(Pattern pattern, Consumer<Matcher> callback) {
        subscriptions.add(new Subscription(pattern, callback));
    }

    /** Convenience overload that compiles the regex. */
    public void subscribe(String regex, Consumer<Matcher> callback) {
        subscribe(Pattern.compile(regex), callback);
    }

    /** Dispatch a raw chat line to all matching subscribers. */
    public void handle(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return;
        }
        for (Subscription sub : subscriptions) {
            Matcher m = sub.pattern.matcher(plainText);
            if (m.find()) {
                try {
                    sub.callback.accept(m);
                } catch (Exception e) {
                    Nyxel.LOGGER.error("chat handler error for {}", sub.pattern, e);
                }
            }
        }
    }

    private record Subscription(Pattern pattern, Consumer<Matcher> callback) {
    }
}
