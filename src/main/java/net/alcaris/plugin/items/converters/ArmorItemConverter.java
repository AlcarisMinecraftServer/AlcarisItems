package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemArmorModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.enums.Colors;
import net.alcaris.plugin.items.enums.ArmorStats;
import net.alcaris.plugin.items.enums.ArmorStatData;
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
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ArmorItemConverter {
    private final AlcarisItems plugin;
    private final Map<String, NamespacedKey> keys;
    private final Gson gson = new Gson();

    private static final int PERFORMANCE_INCREMENT = 5; // 固定値での上昇量

    public enum UpgradeType {
        ROLL_ALL,           // 全てをロール
        FIXED_INCREMENT,    // 1つを固定値で5%あげる
        ROLL_SPECIFIC,      // 1つを指定してロール
        ROLL_AND_KEEP_HIGH  // 1つを前回のロールと比べて、高い方を採用
    }

    public ArmorItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
        this.keys = initializeKeys();
    }

    private Map<String, NamespacedKey> initializeKeys() {
        Map<String, NamespacedKey> keyMap = new HashMap<>();
        
        // 基本キー
        String[] basicKeys = {"item_id", "item_version", "armor_type", "required_level", "max_modification", "durability"};
        for (String name : basicKeys) {
            keyMap.put(name, new NamespacedKey(plugin, name));
        }
        
        // ArmorStatsから自動的にキーを生成
        for (ArmorStats stat : ArmorStats.values()) {
            keyMap.put(stat.getValueKey(), new NamespacedKey(plugin, stat.getValueKey()));
            keyMap.put(stat.getPerformanceKey(), new NamespacedKey(plugin, stat.getPerformanceKey()));
        }
        
        return keyMap;
    }

    private Material getArmorMaterial(String itemId) {
        String armorId = itemId.substring(itemId.lastIndexOf("_"));
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
        ItemStack itemStack = new ItemStack(getArmorMaterial(item.getId()), amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return itemStack;

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        int maxModification = (int) armor.getMaxModification(); // doubleからintに変換

        // 初期性能値を0に設定したArmorStatDataを作成
        ArmorStatData statData = new ArmorStatData();
        // デフォルトで全て0で初期化されているので追加設定不要

        return commonSetting(
            item,
            armor,
            amount,
            armor.getType(),
            armor.getRequirement(),
            maxModification,
            (int) armor.getDurability(), // durabilityもdoubleからintに変換
            statData
        );
    }

    public ItemStack updateItem(ItemBaseModel item, ItemArmorModel armor, int amount, ItemStack oldItem) {
        ItemMeta oldMeta = oldItem.getItemMeta();
        if (oldMeta == null) return oldItem;

        PersistentDataContainer oldItemContainer = oldMeta.getPersistentDataContainer();
        int maxModification = oldItemContainer.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);

        // ArmorStatsを使用して性能値を取得してArmorStatDataに設定
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
            0, // durability is not available in backward compatibility
            statData
        );
    }

    private ItemStack commonSetting(ItemBaseModel item, ItemArmorModel armor, int amount,
                                  String type, int requirement, int maxModification,
                                  int durability, ArmorStatData statData) {
        ItemStack itemStack = new ItemStack(getArmorMaterial(item.getId()), amount);
        Damageable itemMeta = (Damageable) itemStack.getItemMeta();

        if (itemMeta == null) return itemStack;

        // Calculate final stats using ArmorStatData
        for (ArmorStats stat : ArmorStats.values()) {
            double baseValue = getArmorStatValue(armor, stat);
            int performance = statData.getPerformanceValue(stat);
            double finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        // Set basic item properties
        setupBasicProperties(itemMeta, item);
        
        // Set durability if available
        if (itemMeta instanceof Damageable damageable && durability > 0) {
            damageable.setMaxDamage(durability);
        }
        
        // Set lore
        List<Component> lore = createLore(item, requirement, maxModification, armor, statData);
        itemMeta.lore(lore);

        // Set persistent data
        setupPersistentData(itemMeta, item, type != null ? type : "default", requirement, maxModification, durability, statData);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private double calculateFinalStat(double baseValue, int performance) {
        return Math.floor(baseValue * (0.5 * (1 + performance / 100.0)));
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
                                     ItemArmorModel armor, ArmorStatData statData) {
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
        int durability = 0;
        try {
            durability = (int) ItemArmorModel.class.getMethod("getDurability").invoke(armor);
        } catch (Exception e) {
            // fallback for backward compatibility
        }
        if (durability > 0) {
            lore.add(buildInfoLine("耐久値", String.valueOf(durability)));
        }

        // Add armor stats using ArmorStats enum
        for (ArmorStats stat : ArmorStats.values()) {
            double baseValue = getArmorStatValue(armor, stat);
            if (baseValue != 0) {
                double finalValue = statData.getFinalValue(stat);
                int performanceValue = statData.getPerformanceValue(stat);
                lore.add(buildStatLine(stat.getDisplayName(), 
                        String.valueOf((int)finalValue), 
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
        
        // ArmorStatsを使用してデータを設定
        for (ArmorStats stat : ArmorStats.values()) {
            container.set(keys.get(stat.getValueKey()), PersistentDataType.DOUBLE, statData.getFinalValue(stat));
            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }
    }

    public boolean upgradeArmorPerformance(ItemStack itemStack, UpgradeType type, String specificStat) {
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
        
        // LinkedTreeMapをItemArmorModelに変換
        ItemArmorModel armor = gson.fromJson(gson.toJson(baseModel.getData()), ItemArmorModel.class);
        if (armor == null) {
            return false;
        }

        // 現在の性能値を取得してArmorStatDataに設定
        ArmorStatData statData = new ArmorStatData();
        for (ArmorStats stat : ArmorStats.values()) {
            int perf = container.getOrDefault(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, 0);
            statData.setPerformanceValue(stat, perf);
        }

        // アップグレードタイプに応じて処理
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

            case ROLL_AND_KEEP_HIGH:
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

        // 性能値を更新
        for (ArmorStats stat : ArmorStats.values()) {
            container.set(keys.get(stat.getPerformanceKey()), PersistentDataType.INTEGER, statData.getPerformanceValue(stat));
        }

        // maxModificationを減らす
        int newMaxModification = currentMaxModification - 1;
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, newMaxModification);

        // アイテムのステータスを再計算（基礎値はItemArmorModelから取得）
        for (ArmorStats stat : ArmorStats.values()) {
            double baseValue = getArmorStatValue(armor, stat);
            int performance = statData.getPerformanceValue(stat);
            double finalValue = calculateFinalStat(baseValue, performance);
            statData.setFinalValue(stat, finalValue);
        }

        // 最終ステータスを更新
        for (ArmorStats stat : ArmorStats.values()) {
            container.set(keys.get(stat.getValueKey()), PersistentDataType.DOUBLE, statData.getFinalValue(stat));
        }

        // アイテムの説明文を更新
        long version = container.getOrDefault(keys.get("item_version"), PersistentDataType.LONG, 0L);
        String armorType = container.getOrDefault(keys.get("armor_type"), PersistentDataType.STRING, "");
        int requiredLevel = container.getOrDefault(keys.get("required_level"), PersistentDataType.INTEGER, 0);

        // 説明文の作成
        List<Component> lore = new ArrayList<>();
        
        // レアリティの取得（既存のアイテムから）
        int rarity = 1; // デフォルト値を1に設定
        if (itemMeta.hasLore() && itemMeta.lore() != null && !itemMeta.lore().isEmpty()) {
            Component firstLine = itemMeta.lore().get(0);
            String firstLineText = firstLine.toString();
            if (firstLineText.contains("レアリティ: ")) {
                try {
                    String rarityStr = firstLineText.split("レアリティ: ")[1].trim();
                    rarity = Integer.parseInt(rarityStr);
                } catch (NumberFormatException e) {
                    // デフォルト値を使用
                }
            }
        }

        // 基本情報の追加
        lore.add(Component.text("【" + net.alcaris.plugin.items.enums.TextureIcons.fromRarity(rarity).getUnicode() + "】 " + "レアリティ: " + rarity)
            .color(TextColor.fromHexString(net.alcaris.plugin.items.enums.Colors.fromRarity(rarity).getHexCode()))
            .decoration(TextDecoration.ITALIC, false));

        // 区切り線
        lore.add(Component.text("                          ")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.STRIKETHROUGH, true));

        // 必要レベルと改造回数の表示
        lore.add(buildInfoLine("必要レベル", String.valueOf(requiredLevel)));
        lore.add(buildInfoLine("残り改造回数", String.valueOf(newMaxModification)));

        // ステータスの表示（ArmorStatsを使用）
        for (ArmorStats stat : ArmorStats.values()) {
            addStatToLore(lore, armor, stat, statData);
        }

        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return true;
    }

    /**
     * ステータスをLoreに追加するヘルパーメソッド
     */
    private void addStatToLore(List<Component> lore, ItemArmorModel armor, ArmorStats stat, 
                              ArmorStatData statData) {
        double baseValue = getArmorStatValue(armor, stat);
        if (baseValue != 0) {
            lore.add(buildStatLine(stat.getDisplayName(), 
                    String.valueOf((int)statData.getFinalValue(stat)), 
                    String.valueOf(statData.getPerformanceValue(stat))));
        }
    }

    /**
     * ArmorStatsに対応する基礎値を取得するヘルパーメソッド
     * 
     * @param armor ItemArmorModelオブジェクト
     * @param stat 取得したいステータス
     * @return 対応する基礎値
     */
    private double getArmorStatValue(ItemArmorModel armor, ArmorStats stat) {
        // ArmorStatsのgetValueメソッドを使用して動的に値を取得
        return stat.getValue(armor);
    }
} 