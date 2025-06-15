package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.enums.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WeaponItemConverter {
    private final AlcarisItems plugin;
    private final Map<String, NamespacedKey> keys;

    public WeaponItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
        this.keys = initializeKeys();
    }

    private Map<String, NamespacedKey> initializeKeys() {
        Map<String, NamespacedKey> keyMap = new HashMap<>();
        String[] keyNames = {
            "item_id", "item_version", "weapon_type", "required_level", "max_modification",
            "durability", "attack_damage", "attack_damage_perf", "movement_speed", "movement_speed_perf",
            "attack_range", "attack_range_perf", "attack_speed", "attack_speed_perf",
            "experience_bonus", "experience_bonus_perf", "drop_rate_bonus", "drop_rate_bonus_perf"
        };

        for (String name : keyNames) {
            keyMap.put(name, new NamespacedKey(plugin, name));
        }
        return keyMap;
    }

    private Material getToolMaterial(String itemId) {
        String toolId = itemId.substring(itemId.lastIndexOf("_"));
        return switch (toolId) {
            case "_bow" -> Material.BOW;
            default -> Material.WOODEN_SWORD;
        };
    }

    private int calculatePerformance() {
        return ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
    }

    public ItemStack createNewItem(ItemBaseModel item, ItemWeaponModel weapon, int amount) {
        return commonSetting(
            item,
            weapon,
            amount,
            weapon.getType(),
            weapon.getRequirement(),
            weapon.getPolishingCount(),
            0, // durability is not available in backward compatibility
            calculatePerformance(),
            calculatePerformance(),
            calculatePerformance(),
            calculatePerformance(),
            calculatePerformance(),
            calculatePerformance()
        );
    }

    public ItemStack updateItem(ItemBaseModel item, ItemWeaponModel weapon, int amount, ItemStack oldItem) {
        ItemMeta oldMeta = oldItem.getItemMeta();
        PersistentDataContainer oldItemContainer = oldMeta.getPersistentDataContainer();

        return commonSetting(
            item,
            weapon,
            amount,
            weapon.getType(),
            weapon.getRequirement(),
            weapon.getPolishingCount(),
            0, // durability is not available in backward compatibility
            oldItemContainer.getOrDefault(keys.get("attack_damage_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("movement_speed_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("attack_range_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("attack_speed_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("experience_bonus_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("drop_rate_bonus_perf"), PersistentDataType.INTEGER, 0)
        );
    }

    private ItemStack commonSetting(ItemBaseModel item, ItemWeaponModel weapon, int amount,
                                  String type, int requirement, int polishingCount,
                                  int durability, int damagePerformance, int walkSpeedPerformance,
                                  int attackRangePerformance, int attackSpeedPerformance,
                                  int xpBonusPerformance, int lootBonusPerformance) {
        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
        Damageable itemMeta = (Damageable) itemStack.getItemMeta();

        if (itemMeta == null) return itemStack;

        // Calculate final stats
        double finalDamage = calculateFinalStat(weapon.getDamage(), damagePerformance);
        double finalAttackRange = calculateFinalStat(weapon.getAttackRange(), attackRangePerformance);
        double finalAttackSpeed = calculateFinalStat(weapon.getAttackSpeed(), attackSpeedPerformance);
        int finalWalkSpeed = (int) calculateFinalStat(weapon.getWalkSpeed(), walkSpeedPerformance);
        int finalXpBonus = (int) calculateFinalStat(weapon.getXpBonus(), xpBonusPerformance);
        int finalLootBonus = (int) calculateFinalStat(weapon.getLootBonus(), lootBonusPerformance);

        // Set basic item properties
        setupBasicProperties(itemMeta, item);
        
        // Set durability if available
        if (itemMeta instanceof Damageable damageable && durability > 0) {
            damageable.setMaxDamage(durability);
        }
        
        // Set lore
        List<Component> lore = createLore(item, requirement, polishingCount, weapon,
            finalDamage, finalAttackRange, finalAttackSpeed,
            finalWalkSpeed, finalXpBonus, finalLootBonus,
            damagePerformance, walkSpeedPerformance, attackRangePerformance,
            attackSpeedPerformance, xpBonusPerformance, lootBonusPerformance);
        itemMeta.lore(lore);

        // Set persistent data
        setupPersistentData(itemMeta, item, type, requirement, polishingCount, durability,
            finalDamage, damagePerformance, finalWalkSpeed, walkSpeedPerformance,
            finalAttackRange, attackRangePerformance, finalAttackSpeed, attackSpeedPerformance,
            finalXpBonus, xpBonusPerformance, finalLootBonus, lootBonusPerformance);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private double calculateFinalStat(double baseValue, int performance) {
        return Math.floor(baseValue * (0.5 * (1 + performance / 100.0)) * 100) / 100.0;
    }

    private void setupBasicProperties(ItemMeta itemMeta, ItemBaseModel item) {
        itemMeta.displayName(
            plugin.miniMessage.deserialize(item.getName())
                .color(TextColor.fromHexString(Colors.fromRarity(item.getRarity()).getHexCode()))
                .decoration(TextDecoration.ITALIC, false)
        );
        itemMeta.setMaxStackSize(item.getMaxStack());

        if (item.getCustomModelData() != 0) {
            itemMeta.setCustomModelData(item.getCustomModelData());
        }
    }

    private List<Component> createLore(ItemBaseModel item, int requirement, int polishingCount,
                                     ItemWeaponModel weapon, double finalDamage, double finalAttackRange,
                                     double finalAttackSpeed, int finalWalkSpeed, int finalXpBonus,
                                     int finalLootBonus, int damagePerformance, int walkSpeedPerformance,
                                     int attackRangePerformance, int attackSpeedPerformance,
                                     int xpBonusPerformance, int lootBonusPerformance) {
        List<Component> lore = new ArrayList<>();
        
        // Add base lore
        for (String text : item.getLore()) {
            lore.add(Component.text(text)
                .color(TextColor.fromHexString("#D8D8D8"))
                .decoration(TextDecoration.ITALIC, false));
        }

        // Add separator
        lore.add(Component.text("                          ")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.STRIKETHROUGH, true));

        // Add requirement and modification info
        lore.add(buildInfoLine("必要レベル", String.valueOf(requirement)));
        if (polishingCount > 0) {
            lore.add(buildInfoLine("研磨回数", String.valueOf(polishingCount)));
        }

        // Add weapon stats
        if (weapon.getDamage() != 0)
            lore.add(buildStatLine("攻撃力", String.valueOf(finalDamage), String.valueOf(damagePerformance)));
        if (weapon.getAttackRange() != 0)
            lore.add(buildStatLine("攻撃距離", String.valueOf(finalAttackRange), String.valueOf(attackRangePerformance)));
        if (weapon.getAttackSpeed() != 0)
            lore.add(buildStatLine("攻撃速度", String.valueOf(finalAttackSpeed), String.valueOf(attackSpeedPerformance)));
        if (weapon.getWalkSpeed() != 0)
            lore.add(buildStatLine("移動速度", String.valueOf(finalWalkSpeed), String.valueOf(walkSpeedPerformance)));
        if (weapon.getXpBonus() != 0)
            lore.add(buildStatLine("経験値ボーナス", String.valueOf(finalXpBonus), String.valueOf(xpBonusPerformance)));
        if (weapon.getLootBonus() != 0)
            lore.add(buildStatLine("ドロップ率ボーナス", String.valueOf(finalLootBonus), String.valueOf(lootBonusPerformance)));

        return lore;
    }

    private Component buildInfoLine(String label, String value) {
        return Component.text(label + " : ")
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(value)
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
    }

    private Component buildStatLine(String label, String value, String percent) {
        return Component.text(label + " : ")
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(value)
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text(" (")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text(percent)
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text("%")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text(")")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
    }

    private void setupPersistentData(ItemMeta itemMeta, ItemBaseModel item, String weaponType,
                                   int requiredLevel, int maxModification, int durability,
                                   double finalDamage, int damagePerformance, int finalWalkSpeed,
                                   int walkSpeedPerformance, double finalAttackRange,
                                   int attackRangePerformance, double finalAttackSpeed,
                                   int attackSpeedPerformance, int finalXpBonus,
                                   int xpBonusPerformance, int finalLootBonus,
                                   int lootBonusPerformance) {
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        
        container.set(keys.get("item_id"), PersistentDataType.STRING, item.getId());
        container.set(keys.get("item_version"), PersistentDataType.LONG, item.getVersion());
        container.set(keys.get("weapon_type"), PersistentDataType.STRING, weaponType);
        container.set(keys.get("required_level"), PersistentDataType.INTEGER, requiredLevel);
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, maxModification);
        container.set(keys.get("durability"), PersistentDataType.INTEGER, durability);
        
        container.set(keys.get("attack_damage"), PersistentDataType.DOUBLE, finalDamage);
        container.set(keys.get("attack_damage_perf"), PersistentDataType.INTEGER, damagePerformance);
        container.set(keys.get("movement_speed"), PersistentDataType.INTEGER, finalWalkSpeed);
        container.set(keys.get("movement_speed_perf"), PersistentDataType.INTEGER, walkSpeedPerformance);
        container.set(keys.get("attack_range"), PersistentDataType.DOUBLE, finalAttackRange);
        container.set(keys.get("attack_range_perf"), PersistentDataType.INTEGER, attackRangePerformance);
        container.set(keys.get("attack_speed"), PersistentDataType.DOUBLE, finalAttackSpeed);
        container.set(keys.get("attack_speed_perf"), PersistentDataType.INTEGER, attackSpeedPerformance);
        container.set(keys.get("experience_bonus"), PersistentDataType.INTEGER, finalXpBonus);
        container.set(keys.get("experience_bonus_perf"), PersistentDataType.INTEGER, xpBonusPerformance);
        container.set(keys.get("drop_rate_bonus"), PersistentDataType.INTEGER, finalLootBonus);
        container.set(keys.get("drop_rate_bonus_perf"), PersistentDataType.INTEGER, lootBonusPerformance);
    }
}