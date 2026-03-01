package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemArmorModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.utils.RarityUtils;
import net.alcaris.plugin.items.utils.ItemDataComponents;
import net.alcaris.plugin.items.enums.ArmorStats;
import net.alcaris.plugin.items.enums.ArmorStatData;
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

public class ArmorItemConverter {
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

    public ArmorItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
        this.keys = initializeKeys();
    }

    private Map<String, NamespacedKey> initializeKeys() {
        Map<String, NamespacedKey> keyMap = new HashMap<>();

        String[] basicKeys = {"item_id", "item_version", "armor_type", "required_level", "max_modification", "durability"};
        for (String name : basicKeys) {
            keyMap.put(name, new NamespacedKey(plugin, name));
        }

        for (ArmorStats stat : ArmorStats.values()) {
            keyMap.put(stat.getKey(), new NamespacedKey(plugin, stat.getKey()));
            keyMap.put(stat.getPerformanceKey(), new NamespacedKey(plugin, stat.getPerformanceKey()));
        }

        return keyMap;
    }

    private Material getArmorMaterial(String itemId) {
        int idx = itemId.lastIndexOf('_');
        if (idx < 0) {
            return Material.LEATHER_CHESTPLATE;
        }
        String armorId = itemId.substring(idx);
        return switch (armorId) {
            case "_helmet" -> Material.LEATHER_HELMET;
            case "_chestplate" -> Material.LEATHER_CHESTPLATE;
            case "_leggings" -> Material.LEATHER_LEGGINGS;
            case "_boots" -> Material.LEATHER_BOOTS;
            default -> Material.LEATHER_CHESTPLATE;
        };
    }

    private int calculatePerformance() {
        return ((int) (Math.random() * 100) + (int) (Math.random() * 100)) / 2 + 1;
    }

    public ItemStack createNewItem(ItemBaseModel item, ItemArmorModel armor, int amount) {
        int maxModification = armor.getMaxModification();

        ArmorStatData statData = new ArmorStatData();

        return commonSetting(
            item,
            armor,
            amount,
            armor.getType(),
            armor.getRequirement(),
            maxModification,
            armor.getDurability(),
            statData
        );
    }

    public ItemStack updateItem(ItemBaseModel item, ItemArmorModel armor, int amount, ItemStack oldItem) {
        ItemMeta oldMeta = oldItem.getItemMeta();
        if (oldMeta == null) return oldItem;

        PersistentDataContainer oldItemContainer = oldMeta.getPersistentDataContainer();
        int maxModification = oldItemContainer.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);

        ArmorStatData statData = new ArmorStatData();
        for (ArmorStats stat : ArmorStats.values()) {
            int performance = oldItemContainer.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, performance);
        }

        return commonSetting(
            item,
            armor,
            amount,
            armor.getType(),
            armor.getRequirement(),
            maxModification,
            0,
            statData
        );
    }

    private ItemStack commonSetting(ItemBaseModel item, ItemArmorModel armor, int amount,
                                  String type, int requirement, int maxModification,
                                  int durability, ArmorStatData statData) {
        ItemStack itemStack = new ItemStack(getArmorMaterial(item.getId()), amount);
        Damageable itemMeta = (Damageable) itemStack.getItemMeta();

        if (itemMeta == null) return itemStack;

        for (ArmorStats stat : ArmorStats.values()) {
            double baseValue = getArmorStatValue(armor, stat);
            int performance = statData.getPerformanceValue(stat);
            int finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        setupBasicProperties(itemMeta, item);

        if (durability > 0) {
            itemMeta.setMaxDamage(durability);
        }

        applyAttributeModifiers(itemMeta, statData);

        List<Component> lore = createLore(item, requirement, maxModification, armor, statData);
        itemMeta.lore(lore);

        setupPersistentData(itemMeta, item, type != null ? type : "default", requirement, maxModification, durability, statData);

        itemStack.setItemMeta(itemMeta);
        ItemDataComponents.apply(itemStack, item);
        return itemStack;
    }

    private int calculateFinalStat(double baseValue, int performance) {
        if (baseValue < 0) {
            return (int) Math.floor(baseValue * (1 - (performance / 200.0)));
        } else {
            return (int) Math.floor(baseValue * (0.5 * (1 + performance / 100.0)));
        }
    }

    private void setupBasicProperties(ItemMeta itemMeta, ItemBaseModel item) {
        itemMeta.displayName(
            plugin.miniMessage.deserialize(item.getName())
                .color(TextColor.fromHexString(RarityUtils.getColor(item.getRarity()).getHexCode()))
                .decoration(TextDecoration.ITALIC, false)
        );
        itemMeta.setMaxStackSize(item.getMaxStack());
    }

    private List<Component> createLore(ItemBaseModel item, int requirement, int polishingCount,
                                     ItemArmorModel armor, ArmorStatData statData) {
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

        int durability = 0;
        try {
            durability = (int) ItemArmorModel.class.getMethod("getDurability").invoke(armor);
        } catch (Exception ignored) {}
        if (durability > 0) {
            lore.add(buildInfoLine("耐久値", String.valueOf(durability)));
        }

        for (ArmorStats stat : ArmorStats.values()) {
                int finalValue = statData.getFinalValue(stat);
                int performanceValue = statData.getPerformanceValue(stat);
            if (finalValue != 0) {
                lore.add(buildStatLine(stat.getDisplayName(),
                        String.valueOf(finalValue),
                        String.valueOf(performanceValue)));
            }
        }

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
        int performance = Integer.parseInt(percent);
        TextColor percentColor = getPerformanceColor(performance);

        TextColor valueColor;
        try {
            int numValue = Integer.parseInt(value);
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

    private void applyAttributeModifiers(ItemMeta itemMeta, ArmorStatData statData) {
        itemMeta.removeAttributeModifier(Attribute.ARMOR);
        itemMeta.removeAttributeModifier(Attribute.ARMOR_TOUGHNESS);
        itemMeta.removeAttributeModifier(Attribute.KNOCKBACK_RESISTANCE);
        itemMeta.removeAttributeModifier(Attribute.MOVEMENT_SPEED);

        int speedValue = statData.getFinalValue(ArmorStats.MOVEMENT_SPEED);
        if (speedValue != 0) {
            AttributeModifier movementModifier = new AttributeModifier(
                new NamespacedKey(plugin, "armor_movement_speed"),
                speedValue / 1000.0,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ARMOR
            );
            itemMeta.addAttributeModifier(Attribute.MOVEMENT_SPEED, movementModifier);
        }
    }

    private void setupPersistentData(ItemMeta itemMeta, ItemBaseModel item, String armorType,
                                   int requiredLevel, int maxModification, int durability,
                                   ArmorStatData statData) {
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        container.set(keys.get("item_id"), PersistentDataType.STRING, item.getId());
        container.set(keys.get("item_version"), PersistentDataType.LONG, item.getVersion());
        container.set(keys.get("armor_type"), PersistentDataType.STRING, armorType != null ? armorType : "default");
        container.set(keys.get("required_level"), PersistentDataType.INTEGER, requiredLevel);
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, maxModification);
        container.set(keys.get("durability"), PersistentDataType.INTEGER, durability);

        for (ArmorStats stat : ArmorStats.values()) {
            container.set(keys.get(stat.getKey()), PersistentDataType.INTEGER, statData.getFinalValue(stat));
            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }
    }

    public boolean upgradeArmorPerformance(ItemStack itemStack, UpgradeType type, String specificStat) {
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

        ItemArmorModel armor = gson.fromJson(gson.toJson(baseModel.getData()), ItemArmorModel.class);
        if (armor == null) {
            return false;
        }

        ArmorStatData statData = new ArmorStatData();
        for (ArmorStats stat : ArmorStats.values()) {
            int perf = container.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        switch (type) {
            case ROLL_ALL:
                for (ArmorStats stat : ArmorStats.values()) {
                    statData.setPerformanceValue(stat, calculatePerformance());
                }
                break;

            case FIXED_INCREMENT:
                if (specificStat != null) {
                    ArmorStats targetStat = ArmorStats.fromKey(specificStat);
                    if (targetStat != null) {
                        int currentPerf = statData.getPerformanceValue(targetStat);
                        statData.setPerformanceValue(targetStat, Math.min(100, currentPerf + PERFORMANCE_INCREMENT));
                    }
                }
                break;

            case ROLL_SPECIFIC:
                if (specificStat != null) {
                    ArmorStats targetStat = ArmorStats.fromKey(specificStat);
                    if (targetStat != null) {
                        statData.setPerformanceValue(targetStat, calculatePerformance());
                    }
                }
                break;

            case ROLL_SPECIFIC_HIGH:
                if (specificStat != null) {
                    ArmorStats targetStat = ArmorStats.fromKey(specificStat);
                    if (targetStat != null) {
                        int newRoll = calculatePerformance();
                        int currentPerf = statData.getPerformanceValue(targetStat);
                        statData.setPerformanceValue(targetStat, Math.max(currentPerf, newRoll));
                    }
                }
                break;
        }

        for (ArmorStats stat : ArmorStats.values()) {
            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }

        int newMaxModification = currentMaxModification - 1;
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, newMaxModification);

        for (ArmorStats stat : ArmorStats.values()) {
            double baseValue = getArmorStatValue(armor, stat);
            int performance = statData.getPerformanceValue(stat);
            int finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        for (ArmorStats stat : ArmorStats.values()) {
            container.set(keys.get(stat.getKey()), PersistentDataType.INTEGER, statData.getFinalValue(stat));
        }

        int requiredLevel = container.getOrDefault(keys.get("required_level"), PersistentDataType.INTEGER, 0);

        List<Component> lore = createLore(baseModel, requiredLevel, newMaxModification, armor, statData);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return true;
    }

    private double getArmorStatValue(ItemArmorModel armor, ArmorStats stat) {
        return stat.getValue(armor);
    }

    public Map<String, Object> getArmorStats(ItemStack itemStack) {
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

        ItemArmorModel armor = gson.fromJson(gson.toJson(baseModel.getData()), ItemArmorModel.class);
        if (armor == null) {
            return null;
        }

        ArmorStatData statData = new ArmorStatData();
        for (ArmorStats stat : ArmorStats.values()) {
            int perf = container.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        for (ArmorStats stat : ArmorStats.values()) {
            double baseValue = getArmorStatValue(armor, stat);
            if (baseValue != 0) {
                int performance = statData.getPerformanceValue(stat);
                int finalValue = calculateFinalStat(baseValue, performance);

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
