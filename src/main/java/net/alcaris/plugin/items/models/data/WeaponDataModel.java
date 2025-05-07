package net.alcaris.plugin.items.models.data;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.util.ArrayList;
import java.util.List;


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
