package plugin.nyxel.feature.general.data;

/**
 * A minion fuel, deserialized from {@code minions.json}. {@code speedBoost} is the
 * fractional reduction applied to action time (e.g. 0.25 = 25% faster).
 */
public class Fuel {

    public String id;
    public String name;
    public double speedBoost;
}
