package plugin.nyxel.feature.general.engine;

import plugin.nyxel.feature.general.data.Minion;

/**
 * Pure math for minion output: items/hour and time-to-full for a given tier and
 * fuel. A minion alternates generate/collect, so a full production cycle is two
 * actions; fuel reduces the per-action time. No Minecraft dependencies, so it is
 * unit-testable in isolation.
 */
public final class MinionCalculator {

    private MinionCalculator() {
    }

    public static Result compute(Minion minion, int tier, double fuelSpeedBoost) {
        int maxTier = minion.maxTier();
        if (maxTier == 0) {
            return new Result(0, 0, 0);
        }
        int t = Math.max(1, Math.min(tier, maxTier));
        double actionSeconds = minion.actionSeconds.get(t - 1);
        double boost = Math.max(0.0, Math.min(0.99, fuelSpeedBoost));
        double effectiveAction = actionSeconds * (1.0 - boost);
        double cycleSeconds = effectiveAction * 2.0; // generate + collect

        int itemsPerCycle = 0;
        for (Minion.Drop d : minion.drops) {
            itemsPerCycle += d.count;
        }

        double itemsPerHour = cycleSeconds > 0
                ? itemsPerCycle / cycleSeconds * 3600.0 : 0.0;

        int slots = t - 1 < minion.storageSlots.size()
                ? minion.storageSlots.get(t - 1)
                : (minion.storageSlots.isEmpty() ? 0
                        : minion.storageSlots.get(minion.storageSlots.size() - 1));
        long capacity = (long) slots * 64L;

        double hoursToFull = itemsPerHour > 0 ? capacity / itemsPerHour : 0.0;
        return new Result(itemsPerHour, capacity, hoursToFull);
    }

    /** Computed output for one minion configuration. */
    public record Result(double itemsPerHour, long capacityItems, double hoursToFull) {
    }
}
