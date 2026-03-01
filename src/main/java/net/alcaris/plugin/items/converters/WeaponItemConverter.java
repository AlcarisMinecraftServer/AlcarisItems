package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.utils.RarityUtils;
import net.alcaris.plugin.items.utils.ItemDataComponents;
import net.alcaris.plugin.items.enums.WeaponStats;
import net.alcaris.plugin.items.enums.WeaponStatData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

public class WeaponItemConverter {
    private final AlcarisItems plugin;
    private final Map<String, NamespacedKey> keys;
    private final Gson gson = AlcarisItems.getGson();

    private static final int PERFORMANCE_INCREMENT = 5;

    public enum UpgradeType {
        ROLL_ALL,
        FIXED_INCREMENT,
        ROLL_SPECIFIC,
        ROLL_SPECIFIC_HIGH
    }

    public WeaponItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
        this.keys = initializeKeys();
    }

    private Map<String, NamespacedKey> initializeKeys() {
        Map<String, NamespacedKey> keyMap = new HashMap<>();

        String[] basicKeys = {
            "item_id", "item_version", "weapon_type", "required_level",
            "max_modification", "durability"
        };

        for (String name : basicKeys) {
            keyMap.put(name, new NamespacedKey(plugin, name));
        }

        for (WeaponStats stat : WeaponStats.values()) {
            keyMap.put(stat.getKey(), new NamespacedKey(plugin, stat.getKey()));
            keyMap.put(stat.getPerformanceKey(), new NamespacedKey(plugin, stat.getPerformanceKey()));
        }

        return keyMap;
    }

    private Material getToolMaterial(String itemId) {
        int idx = itemId.lastIndexOf('_');
        if (idx < 0) {
            return Material.WOODEN_SWORD;
        }
        String toolId = itemId.substring(idx);
        return switch (toolId) {
            case "_bow" -> Material.BOW;
            default -> Material.WOODEN_SWORD;
        };
    }

    private int calculatePerformance() {
        return ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
    }

    public ItemStack createNewItem(ItemBaseModel item, ItemWeaponModel weapon, int amount) {
        int maxModification = weapon.getMaxModification();

        WeaponStatData statData = new WeaponStatData();

        return commonSetting(
            item,
            weapon,
            amount,
            weapon.getType(),
            weapon.getRequirement(),
            maxModification,
            weapon.getDurability(),
            statData
        );
    }

    public ItemStack updateItem(ItemBaseModel item, ItemWeaponModel weapon, int amount, ItemStack oldItem) {
        ItemMeta oldMeta = oldItem.getItemMeta();
        if (oldMeta == null) return oldItem;

        PersistentDataContainer oldItemContainer = oldMeta.getPersistentDataContainer();
        int maxModification = oldItemContainer.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);
        int durability = oldItemContainer.getOrDefault(keys.get("durability"), PersistentDataType.INTEGER, 0);

        WeaponStatData statData = new WeaponStatData();
        for (WeaponStats stat : WeaponStats.values()) {
            int perf = oldItemContainer.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        return commonSetting(
            item,
            weapon,
            amount,
            weapon.getType(),
            weapon.getRequirement(),
            maxModification,
            durability > 0 ? durability : weapon.getDurability(),
            statData
        );
    }

    private ItemStack commonSetting(ItemBaseModel item, ItemWeaponModel weapon, int amount,
                                  String type, int requirement, int maxModification,
                                  int durability, WeaponStatData statData) {
        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) return itemStack;

        for (WeaponStats stat : WeaponStats.values()) {
            double baseValue = getWeaponStatValue(weapon, stat);
            int performance = statData.getPerformanceValue(stat);
            double finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        setupBasicProperties(itemMeta, item);

        if (durability > 0) {
            if (itemMeta instanceof Damageable damageable) {
                damageable.setMaxDamage(durability);
            }
        }

        applyAttributeModifiers(itemMeta, statData);

        List<Component> lore = createLore(item, requirement, maxModification, durability, statData);
        itemMeta.lore(lore);

        setupPersistentData(itemMeta, item, type != null ? type : "default", requirement, maxModification, durability, statData);

        itemStack.setItemMeta(itemMeta);
        ItemDataComponents.apply(itemStack, item);
        return itemStack;
    }

    private double calculateFinalStat(double baseValue, int performance) {
        if (baseValue < 0) {

            return Math.floor(baseValue * (1 - (performance / 200.0)) * 100) / 100.0;
        } else {

        return Math.floor(baseValue * (0.5 * (1 + performance / 100.0)) * 100) / 100.0;
        }
    }

    private double getWeaponStatValue(ItemWeaponModel weapon, WeaponStats stat) {

        return stat.getValue(weapon);
    }

    private void setupBasicProperties(ItemMeta itemMeta, ItemBaseModel item) {
        itemMeta.displayName(
            plugin.miniMessage.deserialize(item.getName())
                .color(TextColor.fromHexString(RarityUtils.getColor(item.getRarity()).getHexCode()))
                .decoration(TextDecoration.ITALIC, false)
        );
        itemMeta.setMaxStackSize(item.getMaxStack());
    }

    private List<Component> createLore(ItemBaseModel item, int requirement, int polishingCount, int durability, WeaponStatData statData) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.text("【" + RarityUtils.getIcon(item.getRarity()).getUnicode() + "】 " + "レアリティ: " + item.getRarity())
            .color(TextColor.fromHexString(RarityUtils.getColor(item.getRarity()).getHexCode()))
            .decoration(TextDecoration.ITALIC, false));

        for (String text : item.getLore()) {
            lore.add(Component.text(text)
                .color(TextColor.fromHexString("#D8D8D8"))
                .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.text("                          ")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.STRIKETHROUGH, true));

        lore.add(buildInfoLine("必要レベル", String.valueOf(requirement)));
        lore.add(buildInfoLine("残り改造回数", String.valueOf(polishingCount)));

        if (durability > 0) {
            lore.add(buildInfoLine("耐久値", String.valueOf(durability)));
        }

        for (WeaponStats stat : WeaponStats.values()) {
            double finalValue = statData.getFinalValue(stat);
            int performance = statData.getPerformanceValue(stat);
            if (finalValue != 0) {
                String displayValue = getDisplayValue(stat, finalValue);
                lore.add(buildStatLine(stat.getDisplayName(), displayValue, String.valueOf(performance)));
            }
        }

        return lore;
    }

    @NotNull
    private static String getDisplayValue(WeaponStats stat, double finalValue) {
        String displayValue;
        if (stat == WeaponStats.HPR || stat == WeaponStats.MP || stat == WeaponStats.MPR ||
            stat == WeaponStats.ATK || stat == WeaponStats.DEF || stat == WeaponStats.MDF ||
            stat == WeaponStats.CRT || stat == WeaponStats.CRD || stat == WeaponStats.SPD ||
            stat == WeaponStats.LUK) {
            displayValue = String.valueOf((int) finalValue);
        } else {
            displayValue = String.valueOf(finalValue);
        }
        return displayValue;
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
        int performance = Integer.parseInt(percent);
        TextColor percentColor = getPerformanceColor(performance);

        TextColor valueColor;
        try {
            double numValue = Double.parseDouble(value);
            valueColor = numValue < 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
        } catch (NumberFormatException e) {
            valueColor = NamedTextColor.GREEN;
        }

        return Component.text(label + " : ")
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(value)
                .color(valueColor)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text(" (")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text(percent)
                .color(percentColor)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text("%")
                .color(percentColor)
                .decoration(TextDecoration.ITALIC, false))
            .append(Component.text(")")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
    }

    private TextColor getPerformanceColor(int performance) {
        if (performance >= 100) {
            return TextColor.fromHexString("#00FFFF");
        } else if (performance >= 90) {
            return TextColor.fromHexString("#32CD32");
        } else if (performance >= 80) {
            return TextColor.fromHexString("#00FF00");
        } else if (performance >= 70) {
            return TextColor.fromHexString("#9ACD32");
        } else if (performance >= 60) {
            return TextColor.fromHexString("#ADFF2F");
        } else if (performance >= 50) {
            return TextColor.fromHexString("#FFFF00");
        } else if (performance >= 40) {
            return TextColor.fromHexString("#FFD700");
        } else if (performance >= 30) {
            return TextColor.fromHexString("#FFA500");
        } else if (performance >= 20) {
            return TextColor.fromHexString("#FF8C00");
        } else if (performance >= 10) {
            return TextColor.fromHexString("#FF4500");
        } else {
            return TextColor.fromHexString("#FF0000");
        }
    }

    private void setupPersistentData(ItemMeta itemMeta, ItemBaseModel item, String weaponType,
                                   int requiredLevel, int maxModification, int durability,
                                   WeaponStatData statData) {
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        container.set(keys.get("item_id"), PersistentDataType.STRING, item.getId());
        container.set(keys.get("item_version"), PersistentDataType.LONG, item.getVersion());
        container.set(keys.get("weapon_type"), PersistentDataType.STRING, weaponType != null ? weaponType : "default");
        container.set(keys.get("required_level"), PersistentDataType.INTEGER, requiredLevel);
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, maxModification);
        container.set(keys.get("durability"), PersistentDataType.INTEGER, durability);

        for (WeaponStats stat : WeaponStats.values()) {

            if (stat == WeaponStats.HPR || stat == WeaponStats.MP || stat == WeaponStats.MPR ||
                stat == WeaponStats.ATK || stat == WeaponStats.DEF || stat == WeaponStats.MDF ||
                stat == WeaponStats.CRT || stat == WeaponStats.CRD || stat == WeaponStats.SPD ||
                stat == WeaponStats.LUK) {
                container.set(keys.get(stat.getKey()), PersistentDataType.INTEGER, (int)statData.getFinalValue(stat));
            } else {
                container.set(keys.get(stat.getKey()), PersistentDataType.DOUBLE, statData.getFinalValue(stat));
            }

            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }
    }

    private void applyAttributeModifiers(ItemMeta itemMeta, WeaponStatData statData) {

        itemMeta.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
        itemMeta.removeAttributeModifier(Attribute.ATTACK_SPEED);
        itemMeta.removeAttributeModifier(Attribute.MOVEMENT_SPEED);
        itemMeta.removeAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE);

        double attackDamage = statData.getFinalValue(WeaponStats.DAMAGE);
        if (attackDamage != 0) {
            double adjustedDamage = attackDamage - 1.0;
            AttributeModifier damageModifier = new AttributeModifier(
                new NamespacedKey(plugin, "weapon_attack_damage"),
                adjustedDamage,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            );
            itemMeta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damageModifier);
        }

        double attackSpeed = statData.getFinalValue(WeaponStats.ATTACK_SPEED);
        if (attackSpeed != 0) {
            double adjustedSpeed = attackSpeed - 4.0;
            AttributeModifier speedModifier = new AttributeModifier(
                new NamespacedKey(plugin, "weapon_attack_speed"),
                adjustedSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            );
            itemMeta.addAttributeModifier(Attribute.ATTACK_SPEED, speedModifier);
        }

        double attackRange = statData.getFinalValue(WeaponStats.ATTACK_RANGE);
        if (attackRange != 0) {
            AttributeModifier rangeModifier = new AttributeModifier(
                new NamespacedKey(plugin, "weapon_attack_range"),
                attackRange,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            );
            itemMeta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, rangeModifier);
        }

        double movementSpeed = statData.getFinalValue(WeaponStats.WALK_SPEED);
        if (movementSpeed != 0) {
            AttributeModifier movementModifier = new AttributeModifier(
                new NamespacedKey(plugin, "weapon_movement_speed"),
                movementSpeed / 1000.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            );
            itemMeta.addAttributeModifier(Attribute.MOVEMENT_SPEED, movementModifier);
        }
    }

    public boolean upgradeWeaponPerformance(ItemStack itemStack, WeaponItemConverter.UpgradeType type, String specificStat) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        int currentMaxModification = container.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);

        if (currentMaxModification <= 0) {
            return false;
        }

        String itemId = container.getOrDefault(keys.get("item_id"), PersistentDataType.STRING, "");
        Optional<ItemBaseModel> optModel = ((AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore")).getItemRegistry().get(itemId);
        if (optModel.isEmpty()) {
            return false;
        }
        ItemBaseModel baseModel = optModel.get();

        ItemWeaponModel weapon = gson.fromJson(gson.toJson(baseModel.getData()), ItemWeaponModel.class);
        if (weapon == null) {
            return false;
        }

        WeaponStatData statData = new WeaponStatData();
        for (WeaponStats stat : WeaponStats.values()) {
            int perf = container.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        switch (type) {
            case ROLL_ALL:
                for (WeaponStats stat : WeaponStats.values()) {
                    statData.setPerformanceValue(stat, calculatePerformance());
                }
                break;

            case FIXED_INCREMENT:
                if (specificStat != null) {
                    WeaponStats targetStat = WeaponStats.fromKey(specificStat);
                    if (targetStat != null) {
                        int currentPerf = statData.getPerformanceValue(targetStat);
                        statData.setPerformanceValue(targetStat, Math.min(100, currentPerf + PERFORMANCE_INCREMENT));
                    }
                }
                break;

            case ROLL_SPECIFIC:
                if (specificStat != null) {
                    WeaponStats targetStat = WeaponStats.fromKey(specificStat);
                    if (targetStat != null) {
                        statData.setPerformanceValue(targetStat, calculatePerformance());
                    }
                }
                break;

            case ROLL_SPECIFIC_HIGH:
                if (specificStat != null) {
                    WeaponStats targetStat = WeaponStats.fromKey(specificStat);
                    if (targetStat != null) {
                    int newRoll = calculatePerformance();
                        int currentPerf = statData.getPerformanceValue(targetStat);
                        statData.setPerformanceValue(targetStat, Math.max(currentPerf, newRoll));
                    }
                }
                break;
        }

        for (WeaponStats stat : WeaponStats.values()) {
            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }

        int newMaxModification = currentMaxModification - 1;
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, newMaxModification);

        for (WeaponStats stat : WeaponStats.values()) {
            double baseValue = getWeaponStatValue(weapon, stat);
            int performance = statData.getPerformanceValue(stat);
            double finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        for (WeaponStats stat : WeaponStats.values()) {

            if (stat == WeaponStats.HPR || stat == WeaponStats.MP || stat == WeaponStats.MPR ||
                stat == WeaponStats.ATK || stat == WeaponStats.DEF || stat == WeaponStats.MDF ||
                stat == WeaponStats.CRT || stat == WeaponStats.CRD || stat == WeaponStats.SPD ||
                stat == WeaponStats.LUK) {
                container.set(keys.get(stat.getKey()), PersistentDataType.INTEGER, (int)statData.getFinalValue(stat));
            } else {
                container.set(keys.get(stat.getKey()), PersistentDataType.DOUBLE, statData.getFinalValue(stat));
            }
        }

        applyAttributeModifiers(itemMeta, statData);

        int requiredLevel = container.getOrDefault(keys.get("required_level"), PersistentDataType.INTEGER, 0);
        int durability = container.getOrDefault(keys.get("durability"), PersistentDataType.INTEGER, 0);

        List<Component> lore = createLore(baseModel, requiredLevel, newMaxModification, durability, statData);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return true;
    }

    public Map<String, Object> getWeaponStats(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        String itemId = container.getOrDefault(keys.get("item_id"), PersistentDataType.STRING, "");
        if (itemId.isEmpty()) {
            return null;
        }

        Optional<ItemBaseModel> optModel = ((AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore")).getItemRegistry().get(itemId);
        if (optModel.isEmpty()) {
            return null;
        }
        ItemBaseModel baseModel = optModel.get();

        ItemWeaponModel weapon = gson.fromJson(gson.toJson(baseModel.getData()), ItemWeaponModel.class);
        if (weapon == null) {
            return null;
        }

        WeaponStatData statData = new WeaponStatData();
        for (WeaponStats stat : WeaponStats.values()) {
            int perf = container.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        for (WeaponStats stat : WeaponStats.values()) {
            double baseValue = getWeaponStatValue(weapon, stat);
            if (baseValue != 0) {
                int performance = statData.getPerformanceValue(stat);
                double finalValue = calculateFinalStat(baseValue, performance);

                Map<String, Object> statInfo = new LinkedHashMap<>();
                statInfo.put("base", baseValue);
                statInfo.put("final", finalValue);
                statInfo.put("performance", performance);
                statInfo.put("displayName", stat.getDisplayName());

                stats.put(stat.getKey(), statInfo);
            }
        }

        return stats;
    }
}
