package net.alcaris.plugin.items.models.data;

@SuppressWarnings("unused")
public class WeaponDataModel {
    private String type;
    private int requirement;
    private int polishingCount;
    private double damage;
    private double walkSpeed;
    private double attackRange;
    private double attackSpeed;
    private double xpBonus;
    private double lootBonus;


    public String getType() {
        return type;
    }

    public int getRequirement() {
        return requirement;
    }

    public int getPolishingCount() {
        return polishingCount;
    }

    public double getDamage() {
        return damage;
    }


    public double getWalkSpeed() {
        return walkSpeed;
    }


    public double getAttackRange() {
        return attackRange;
    }


    public double getAttackSpeed() {
        return attackSpeed;
    }

    public double getXpBonus() {
        return xpBonus;
    }

    public double getLootBonus() {
        return lootBonus;
    }
}
