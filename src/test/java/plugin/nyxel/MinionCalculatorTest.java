package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.feature.general.data.Minion;
import plugin.nyxel.feature.general.engine.MinionCalculator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinionCalculatorTest {

    /** 10s action, 1 item/collect, 1 storage slot (capacity 64). */
    private static Minion sample() {
        Minion m = new Minion();
        m.id = "test";
        m.name = "Test Minion";
        m.actionSeconds = List.of(10.0);
        Minion.Drop d = new Minion.Drop();
        d.item = "Thing";
        d.count = 1;
        m.drops = List.of(d);
        m.storageSlots = List.of(1);
        return m;
    }

    @Test
    void computesItemsPerHourAndCapacity() {
        // cycle = 10s action * 2 = 20s; 1 item/cycle -> 180 items/hour; capacity 64.
        MinionCalculator.Result r = MinionCalculator.compute(sample(), 1, 0.0);
        assertEquals(180.0, r.itemsPerHour(), 0.001);
        assertEquals(64, r.capacityItems());
        assertEquals(64.0 / 180.0, r.hoursToFull(), 0.001);
    }

    @Test
    void fuelBoostReducesActionTime() {
        // 50% boost halves action time -> doubles output to 360/hour.
        MinionCalculator.Result r = MinionCalculator.compute(sample(), 1, 0.5);
        assertEquals(360.0, r.itemsPerHour(), 0.001);
    }

    @Test
    void tierIsClampedToAvailableData() {
        // Only tier I exists; asking for tier 5 must not throw and uses tier I.
        MinionCalculator.Result r = MinionCalculator.compute(sample(), 5, 0.0);
        assertEquals(180.0, r.itemsPerHour(), 0.001);
    }

    @Test
    void emptyMinionIsSafe() {
        MinionCalculator.Result r = MinionCalculator.compute(new Minion(), 1, 0.0);
        assertEquals(0.0, r.itemsPerHour(), 0.001);
        assertTrue(r.hoursToFull() >= 0.0);
    }
}
