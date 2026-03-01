package net.alcaris.plugin.items.magic;

public class MagicSpellAoeConfig {
    private final double radius;
    private final double damage;
    private final int igniteSeconds;

    public MagicSpellAoeConfig(double radius, double damage, int igniteSeconds) {
        this.radius = radius;
        this.damage = damage;
        this.igniteSeconds = igniteSeconds;
    }

    public double getRadius() {
        return radius;
    }

    public double getDamage() {
        return damage;
    }

    public int getIgniteSeconds() {
        return igniteSeconds;
    }
}
