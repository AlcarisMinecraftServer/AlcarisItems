package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.enums.Colors;
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
import org.bukkit.inventory.EquipmentSlot;
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
import java.util.UUID;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class WeaponItemConverter {
    private final AlcarisItems plugin;
    private final Map<String, NamespacedKey> keys;
    private final Gson gson = new Gson();

    private static final int PERFORMANCE_INCREMENT = 5; // 固定値での上昇量

    public enum UpgradeType {
        ROLL_ALL,           // 全てをロール
        FIXED_INCREMENT,    // 1つを固定値で5%あげる
        ROLL_SPECIFIC,      // 1つを指定してロール
        ROLL_SPECIFIC_HIGH // 1つを前回のロールと比べて、高い方を採用
    }

    public WeaponItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
        this.keys = initializeKeys();
    }

    private Map<String, NamespacedKey> initializeKeys() {
        Map<String, NamespacedKey> keyMap = new HashMap<>();
        
        // 基本キー
        String[] basicKeys = {
            "item_id", "item_version", "weapon_type", "required_level", 
            "max_modification", "durability"
        };

        for (String name : basicKeys) {
            keyMap.put(name, new NamespacedKey(plugin, name));
        }
        
        // WeaponStatsから自動的にキーを生成
        for (WeaponStats stat : WeaponStats.values()) {
            keyMap.put(stat.getKey(), new NamespacedKey(plugin, stat.getKey()));
            keyMap.put(stat.getPerformanceKey(), new NamespacedKey(plugin, stat.getPerformanceKey()));
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
        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return itemStack;

        int maxModification = (int) weapon.getMaxModification();
        
        // 初期性能値を0に設定したWeaponStatDataを作成
        WeaponStatData statData = new WeaponStatData();

        return commonSetting(
            item,
            weapon,
            amount,
            weapon.getType(),
            weapon.getRequirement(),
            maxModification,
            (int) weapon.getDurability(),
            statData
        );
    }

    public ItemStack updateItem(ItemBaseModel item, ItemWeaponModel weapon, int amount, ItemStack oldItem) {
        ItemMeta oldMeta = oldItem.getItemMeta();
        if (oldMeta == null) return oldItem;

        PersistentDataContainer oldItemContainer = oldMeta.getPersistentDataContainer();
        int maxModification = oldItemContainer.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);
        int durability = oldItemContainer.getOrDefault(keys.get("durability"), PersistentDataType.INTEGER, 0);

        // 既存の性能値を読み込み
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
            durability > 0 ? durability : (int) weapon.getDurability(),
            statData
        );
    }

    private ItemStack commonSetting(ItemBaseModel item, ItemWeaponModel weapon, int amount,
                                  String type, int requirement, int maxModification,
                                  int durability, WeaponStatData statData) {
        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) return itemStack;

        // Calculate final stats using WeaponStatData
        for (WeaponStats stat : WeaponStats.values()) {
            double baseValue = getWeaponStatValue(weapon, stat);
            int performance = statData.getPerformanceValue(stat);
            double finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        // Set basic item properties
        setupBasicProperties(itemMeta, item);
        
        // Set durability if available
        if (durability > 0) {
            if (itemMeta instanceof Damageable damageable) {
                damageable.setMaxDamage(durability);
            }
        }
        
        // Apply attribute modifiers
        applyAttributeModifiers(itemMeta, statData);
        
        // Set lore
        List<Component> lore = createLore(item, requirement, maxModification, weapon, durability, statData);
        itemMeta.lore(lore);

        // Set persistent data
        setupPersistentData(itemMeta, item, type != null ? type : "default", requirement, maxModification, durability, statData);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private double calculateFinalStat(double baseValue, int performance) {
        if (baseValue < 0) {
            // ベース値がマイナスの場合: baseValue * (1 - (performance / 200))
            return Math.floor(baseValue * (1 - (performance / 200.0)) * 100) / 100.0;
        } else {
            // ベース値がプラスまたは0の場合: 既存の計算式
        return Math.floor(baseValue * (0.5 * (1 + performance / 100.0)) * 100) / 100.0;
        }
    }

    /**
     * WeaponStatsに対応する基礎値を取得するヘルパーメソッド
     * 
     * @param weapon ItemWeaponModelオブジェクト
     * @param stat 取得したいステータス
     * @return 対応する基礎値
     */
    private double getWeaponStatValue(ItemWeaponModel weapon, WeaponStats stat) {
        // WeaponStatsのgetValueメソッドを使用して動的に値を取得
        return stat.getValue(weapon);
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
                                     ItemWeaponModel weapon, int durability, WeaponStatData statData) {
        List<Component> lore = new ArrayList<>();

        // Add rarity at the top
        lore.add(Component.text("【" + net.alcaris.plugin.items.enums.TextureIcons.fromRarity(item.getRarity()).getUnicode() + "】 " + "レアリティ: " + item.getRarity())
            .color(TextColor.fromHexString(net.alcaris.plugin.items.enums.Colors.fromRarity(item.getRarity()).getHexCode()))
            .decoration(TextDecoration.ITALIC, false));
        
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
        lore.add(buildInfoLine("残り改造回数", String.valueOf(polishingCount)));

        // Add durability if present
        if (durability > 0) {
            lore.add(buildInfoLine("耐久値", String.valueOf(durability)));
        }

        // Add weapon stats using WeaponStatData
        for (WeaponStats stat : WeaponStats.values()) {
            double finalValue = statData.getFinalValue(stat);
            int performance = statData.getPerformanceValue(stat);
            if (finalValue != 0) { // 0以外の値（プラス・マイナス両方）を表示
                String displayValue;
                if (stat == WeaponStats.HPR || stat == WeaponStats.MP || stat == WeaponStats.MPR ||
                    stat == WeaponStats.ATK || stat == WeaponStats.DEF || stat == WeaponStats.MDF || 
                    stat == WeaponStats.CRT || stat == WeaponStats.CRD || stat == WeaponStats.SPD || 
                    stat == WeaponStats.LUK) {
                    displayValue = String.valueOf((int)finalValue);
                } else {
                    displayValue = String.valueOf(finalValue);
                }
                lore.add(buildStatLine(stat.getDisplayName(), displayValue, String.valueOf(performance)));
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
        
        // 値がマイナスの場合は赤色、プラスの場合は緑色で表示
        TextColor valueColor;
        try {
            double numValue = Double.parseDouble(value);
            valueColor = numValue < 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
        } catch (NumberFormatException e) {
            valueColor = NamedTextColor.GREEN; // デフォルト
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
            return TextColor.fromHexString("#00FFFF"); // aqua
        } else if (performance >= 90) {
            return TextColor.fromHexString("#32CD32"); // ライムグリーン
        } else if (performance >= 80) {
            return TextColor.fromHexString("#00FF00"); // 明るい緑
        } else if (performance >= 70) {
            return TextColor.fromHexString("#9ACD32"); // イエローグリーン
        } else if (performance >= 60) {
            return TextColor.fromHexString("#ADFF2F"); // グリーンイエロー
        } else if (performance >= 50) {
            return TextColor.fromHexString("#FFFF00"); // 黄色
        } else if (performance >= 40) {
            return TextColor.fromHexString("#FFD700"); // ゴールド
        } else if (performance >= 30) {
            return TextColor.fromHexString("#FFA500"); // オレンジ
        } else if (performance >= 20) {
            return TextColor.fromHexString("#FF8C00"); // ダークオレンジ
        } else if (performance >= 10) {
            return TextColor.fromHexString("#FF4500"); // オレンジレッド
        } else {
            return TextColor.fromHexString("#FF0000"); // 赤
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
        
        // WeaponStatsを使用してデータを設定
        for (WeaponStats stat : WeaponStats.values()) {
            // 最終値を保存
            if (stat == WeaponStats.HPR || stat == WeaponStats.MP || stat == WeaponStats.MPR ||
                stat == WeaponStats.ATK || stat == WeaponStats.DEF || stat == WeaponStats.MDF || 
                stat == WeaponStats.CRT || stat == WeaponStats.CRD || stat == WeaponStats.SPD || 
                stat == WeaponStats.LUK) {
                container.set(keys.get(stat.getKey()), PersistentDataType.INTEGER, (int)statData.getFinalValue(stat));
            } else {
                container.set(keys.get(stat.getKey()), PersistentDataType.DOUBLE, statData.getFinalValue(stat));
            }
            // 性能値を保存
            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }
    }

    /**
     * Apply attribute modifiers to weapon items using WeaponStatData
     */
    private void applyAttributeModifiers(ItemMeta itemMeta, WeaponStatData statData) {
        // Clear existing attribute modifiers
        itemMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        itemMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
        itemMeta.removeAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED);
        itemMeta.removeAttributeModifier(Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
        
        // Apply attack damage modifier (subtract 1 from base value)
        double attackDamage = statData.getFinalValue(WeaponStats.DAMAGE);
        if (attackDamage != 0) { // 0以外の値（プラス・マイナス両方）を適用
            double adjustedDamage = attackDamage - 1.0;
                AttributeModifier damageModifier = new AttributeModifier(
                    new NamespacedKey(plugin, "weapon_attack_damage"),
                    adjustedDamage,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND
                );
                itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
        }
        
        // Apply attack speed modifier (subtract 4 from base value)
        double attackSpeed = statData.getFinalValue(WeaponStats.ATTACK_SPEED);
        if (attackSpeed != 0) { // 0以外の値（プラス・マイナス両方）を適用
            double adjustedSpeed = attackSpeed - 4.0;
            AttributeModifier speedModifier = new AttributeModifier(
                new NamespacedKey(plugin, "weapon_attack_speed"),
                adjustedSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            );
            itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedModifier);
        }
        
        // Apply attack range modifier
        double attackRange = statData.getFinalValue(WeaponStats.ATTACK_RANGE);
        if (attackRange != 0) { // 0以外の値（プラス・マイナス両方）を適用
            AttributeModifier rangeModifier = new AttributeModifier(
                new NamespacedKey(plugin, "weapon_attack_range"),
                attackRange,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            );
            itemMeta.addAttributeModifier(Attribute.PLAYER_ENTITY_INTERACTION_RANGE, rangeModifier);
        }
        
        // Apply movement speed modifier
        double movementSpeed = statData.getFinalValue(WeaponStats.WALK_SPEED);
        if (movementSpeed != 0) { // 0以外の値（プラス・マイナス両方）を適用
            AttributeModifier movementModifier = new AttributeModifier(
                new NamespacedKey(plugin, "weapon_movement_speed"),
                movementSpeed / 1000.0, // Convert percentage to decimal
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            );
            itemMeta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, movementModifier);
        }
    }

    public boolean upgradeWeaponPerformance(ItemStack itemStack, UpgradeType type, String specificStat) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        
        // 現在のmaxModificationを取得
        int currentMaxModification = container.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);

        if (currentMaxModification <= 0) {
            return false;
        }

        // アイテムIDを取得してItemBaseModelを取得
        String itemId = container.getOrDefault(keys.get("item_id"), PersistentDataType.STRING, "");
        Optional<ItemBaseModel> optModel = ((AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore")).getItemRegistry().get(itemId);
        if (optModel.isEmpty()) {
            return false;
        }
        ItemBaseModel baseModel = optModel.get();
        
        // LinkedTreeMapをItemWeaponModelに変換
        ItemWeaponModel weapon = gson.fromJson(gson.toJson(baseModel.getData()), ItemWeaponModel.class);
        if (weapon == null) {
            return false;
        }

        // 現在の性能値を取得
        WeaponStatData statData = new WeaponStatData();
        for (WeaponStats stat : WeaponStats.values()) {
            int perf = container.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        // アップグレードタイプに応じて処理
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

        // 性能値を更新
        for (WeaponStats stat : WeaponStats.values()) {
            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }

        // maxModificationを減らす
        int newMaxModification = currentMaxModification - 1;
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, newMaxModification);

        // アイテムのステータスを再計算（基礎値はItemWeaponModelから取得）
        for (WeaponStats stat : WeaponStats.values()) {
            double baseValue = getWeaponStatValue(weapon, stat);
            int performance = statData.getPerformanceValue(stat);
            double finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        // 最終ステータスを更新
        for (WeaponStats stat : WeaponStats.values()) {
            // 最終値を保存
            if (stat == WeaponStats.HPR || stat == WeaponStats.MP || stat == WeaponStats.MPR || 
                stat == WeaponStats.ATK || stat == WeaponStats.DEF || stat == WeaponStats.MDF || 
                stat == WeaponStats.CRT || stat == WeaponStats.CRD || stat == WeaponStats.SPD || 
                stat == WeaponStats.LUK) {
                container.set(keys.get(stat.getKey()), PersistentDataType.INTEGER, (int)statData.getFinalValue(stat));
            } else {
                container.set(keys.get(stat.getKey()), PersistentDataType.DOUBLE, statData.getFinalValue(stat));
            }
        }

        // Apply attribute modifiers with updated stats
        applyAttributeModifiers(itemMeta, statData);

        // アイテムの説明文を更新
        long version = container.getOrDefault(keys.get("item_version"), PersistentDataType.LONG, 0L);
        String weaponType = container.getOrDefault(keys.get("weapon_type"), PersistentDataType.STRING, "");
        int requiredLevel = container.getOrDefault(keys.get("required_level"), PersistentDataType.INTEGER, 0);
        int durability = container.getOrDefault(keys.get("durability"), PersistentDataType.INTEGER, 0);

        // createLoreを使ってloreを再生成
        List<Component> lore = createLore(baseModel, requiredLevel, newMaxModification, weapon, durability, statData);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return true;
    }

    /**
     * 武器のステータス情報を取得する
     * @param itemStack 対象のItemStack
     * @return ステータス情報のMap（nullの場合は無効なアイテム）
     */
    public Map<String, Object> getWeaponStats(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        
        // アイテムIDを取得してItemBaseModelを取得
        String itemId = container.getOrDefault(keys.get("item_id"), PersistentDataType.STRING, "");
        if (itemId.isEmpty()) {
            return null;
        }

        Optional<ItemBaseModel> optModel = ((AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore")).getItemRegistry().get(itemId);
        if (optModel.isEmpty()) {
            return null;
        }
        ItemBaseModel baseModel = optModel.get();
        
        // LinkedTreeMapをItemWeaponModelに変換
        ItemWeaponModel weapon = gson.fromJson(gson.toJson(baseModel.getData()), ItemWeaponModel.class);
        if (weapon == null) {
            return null;
        }

        // 現在の性能値を取得
        WeaponStatData statData = new WeaponStatData();
        for (WeaponStats stat : WeaponStats.values()) {
            int perf = container.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        // 基礎値、最終値、性能値を計算
        Map<String, Object> stats = new LinkedHashMap<>();
        for (WeaponStats stat : WeaponStats.values()) {
            double baseValue = getWeaponStatValue(weapon, stat);
            if (baseValue != 0) { // 基礎値が0でないもののみ
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