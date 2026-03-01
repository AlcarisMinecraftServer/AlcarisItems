package net.alcaris.plugin.items.magic;

public class MagicSpellThrowConfig {
    private final String projectile;
    private final double radius;
    private final double damage;

    public MagicSpellThrowConfig(String projectile, double radius, double damage) {
        this.projectile = projectile;
        this.radius = radius;
        this.damage = damage;
    }

    public String getProjectile() {
        return projectile;
    }

    public double getRadius() {
        return radius;
    }

    public double getDamage() {
        return damage;
    }
}
