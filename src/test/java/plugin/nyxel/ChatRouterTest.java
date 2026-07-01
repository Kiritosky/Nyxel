package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.core.ChatRouter;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatRouterTest {

    @Test
    void dispatchesCaptureGroupToSubscriber() {
        AtomicReference<String> captured = new AtomicReference<>();
        ChatRouter.get().subscribe("\\+(\\d+) coins", m -> captured.set(m.group(1)));

        ChatRouter.get().handle("You earned +250 coins!");
        assertEquals("250", captured.get());
    }

    @Test
    void ignoresNonMatchingLines() {
        AtomicReference<String> captured = new AtomicReference<>();
        ChatRouter.get().subscribe("UNIQUE_TEST_TOKEN (\\w+)", m -> captured.set(m.group(1)));

        ChatRouter.get().handle("nothing relevant here");
        assertNull(captured.get());
    }
}
