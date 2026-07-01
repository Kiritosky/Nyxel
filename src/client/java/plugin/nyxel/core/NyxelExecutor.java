package plugin.nyxel.core;

import plugin.nyxel.Nyxel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One shared daemon thread pool for all of Nyxel's off-thread work (API fetches,
 * remote dataset loads). Features and clients submit here instead of spinning up
 * their own {@code new Thread(...)}, so the number of background threads stays
 * bounded no matter how many data-backed features are added.
 *
 * <p>Network politeness (avoiding Hypixel rate limits) is handled by callers via
 * cache TTLs and in-flight guards; this class only owns thread lifecycle.
 */
public final class NyxelExecutor {

    private static final ExecutorService POOL =
            Executors.newCachedThreadPool(new DaemonFactory());

    private NyxelExecutor() {
    }

    /** Submit a background task; exceptions are logged, never propagated. */
    public static void run(String name, Runnable task) {
        POOL.submit(() -> {
            Thread current = Thread.currentThread();
            String previous = current.getName();
            current.setName("Nyxel-Worker/" + name);
            try {
                task.run();
            } catch (Throwable t) {
                Nyxel.LOGGER.error("Background task '{}' failed", name, t);
            } finally {
                current.setName(previous);
            }
        });
    }

    private static final class DaemonFactory implements ThreadFactory {
        private final AtomicInteger n = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Nyxel-Worker-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
