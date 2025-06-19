package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.AlcarisCore;
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
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

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
        ROLL_AND_KEEP_HIGH  // 1つを前回のロールと比べて、高い方を採用
    }

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
        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return itemStack;

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        int maxModification = (int) weapon.getMaxModification(); // doubleからintに変換

        return commonSetting(
            item,
            weapon,
            amount,
            weapon.getType(),
            weapon.getRequirement(),
            maxModification,
            (int) weapon.getDurability(), // durabilityもdoubleからintに変換
            0, // 初期性能値を0に設定
            0,
            0,
            0,
            0,
            0
        );
    }

    public ItemStack updateItem(ItemBaseModel item, ItemWeaponModel weapon, int amount, ItemStack oldItem) {
        ItemMeta oldMeta = oldItem.getItemMeta();
        if (oldMeta == null) return oldItem;

        PersistentDataContainer oldItemContainer = oldMeta.getPersistentDataContainer();
        int maxModification = oldItemContainer.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);

        return commonSetting(
            item,
            weapon,
            amount,
            weapon.getType(),
            weapon.getRequirement(),
            maxModification,
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
                                  String type, int requirement, int maxModification,
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
        List<Component> lore = createLore(item, requirement, maxModification, weapon,
            finalDamage, finalAttackRange, finalAttackSpeed,
            finalWalkSpeed, finalXpBonus, finalLootBonus,
            damagePerformance, walkSpeedPerformance, attackRangePerformance,
            attackSpeedPerformance, xpBonusPerformance, lootBonusPerformance);
        itemMeta.lore(lore);

        // Set persistent data
        setupPersistentData(itemMeta, item, type != null ? type : "default", requirement, maxModification, durability,
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
            durability = (int) ItemWeaponModel.class.getMethod("getDurability").invoke(weapon);
        } catch (Exception e) {
            // fallback for backward compatibility
        }
        if (durability > 0) {
            lore.add(buildInfoLine("耐久値", String.valueOf(durability)));
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
        container.set(keys.get("weapon_type"), PersistentDataType.STRING, weaponType != null ? weaponType : "default");
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
        int damagePerf = container.getOrDefault(keys.get("attack_damage_perf"), PersistentDataType.INTEGER, 0);
        int walkSpeedPerf = container.getOrDefault(keys.get("movement_speed_perf"), PersistentDataType.INTEGER, 0);
        int attackRangePerf = container.getOrDefault(keys.get("attack_range_perf"), PersistentDataType.INTEGER, 0);
        int attackSpeedPerf = container.getOrDefault(keys.get("attack_speed_perf"), PersistentDataType.INTEGER, 0);
        int xpBonusPerf = container.getOrDefault(keys.get("experience_bonus_perf"), PersistentDataType.INTEGER, 0);
        int lootBonusPerf = container.getOrDefault(keys.get("drop_rate_bonus_perf"), PersistentDataType.INTEGER, 0);

        // アップグレードタイプに応じて処理
        switch (type) {
            case ROLL_ALL:
                damagePerf = calculatePerformance();
                walkSpeedPerf = calculatePerformance();
                attackRangePerf = calculatePerformance();
                attackSpeedPerf = calculatePerformance();
                xpBonusPerf = calculatePerformance();
                lootBonusPerf = calculatePerformance();
                break;

            case FIXED_INCREMENT:
                if (specificStat != null) {
                    switch (specificStat.toLowerCase()) {
                        case "damage" -> damagePerf = Math.min(100, damagePerf + PERFORMANCE_INCREMENT);
                        case "walkspeed" -> walkSpeedPerf = Math.min(100, walkSpeedPerf + PERFORMANCE_INCREMENT);
                        case "attackrange" -> attackRangePerf = Math.min(100, attackRangePerf + PERFORMANCE_INCREMENT);
                        case "attackspeed" -> attackSpeedPerf = Math.min(100, attackSpeedPerf + PERFORMANCE_INCREMENT);
                        case "xpbonus" -> xpBonusPerf = Math.min(100, xpBonusPerf + PERFORMANCE_INCREMENT);
                        case "lootbonus" -> lootBonusPerf = Math.min(100, lootBonusPerf + PERFORMANCE_INCREMENT);
                    }
                }
                break;

            case ROLL_SPECIFIC:
                if (specificStat != null) {
                    switch (specificStat.toLowerCase()) {
                        case "damage" -> damagePerf = calculatePerformance();
                        case "walkspeed" -> walkSpeedPerf = calculatePerformance();
                        case "attackrange" -> attackRangePerf = calculatePerformance();
                        case "attackspeed" -> attackSpeedPerf = calculatePerformance();
                        case "xpbonus" -> xpBonusPerf = calculatePerformance();
                        case "lootbonus" -> lootBonusPerf = calculatePerformance();
                    }
                }
                break;

            case ROLL_AND_KEEP_HIGH:
                if (specificStat != null) {
                    int newRoll = calculatePerformance();
                    switch (specificStat.toLowerCase()) {
                        case "damage" -> damagePerf = Math.max(damagePerf, newRoll);
                        case "walkspeed" -> walkSpeedPerf = Math.max(walkSpeedPerf, newRoll);
                        case "attackrange" -> attackRangePerf = Math.max(attackRangePerf, newRoll);
                        case "attackspeed" -> attackSpeedPerf = Math.max(attackSpeedPerf, newRoll);
                        case "xpbonus" -> xpBonusPerf = Math.max(xpBonusPerf, newRoll);
                        case "lootbonus" -> lootBonusPerf = Math.max(lootBonusPerf, newRoll);
                    }
                }
                break;
        }

        // 性能値を更新
        container.set(keys.get("attack_damage_perf"), PersistentDataType.INTEGER, damagePerf);
        container.set(keys.get("movement_speed_perf"), PersistentDataType.INTEGER, walkSpeedPerf);
        container.set(keys.get("attack_range_perf"), PersistentDataType.INTEGER, attackRangePerf);
        container.set(keys.get("attack_speed_perf"), PersistentDataType.INTEGER, attackSpeedPerf);
        container.set(keys.get("experience_bonus_perf"), PersistentDataType.INTEGER, xpBonusPerf);
        container.set(keys.get("drop_rate_bonus_perf"), PersistentDataType.INTEGER, lootBonusPerf);

        // maxModificationを減らす
        int newMaxModification = currentMaxModification - 1;
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, newMaxModification);

        // アイテムのステータスを再計算（基礎値はItemWeaponModelから取得）
        double finalDamage = calculateFinalStat(weapon.getDamage(), damagePerf);
        double finalAttackRange = calculateFinalStat(weapon.getAttackRange(), attackRangePerf);
        double finalAttackSpeed = calculateFinalStat(weapon.getAttackSpeed(), attackSpeedPerf);
        int finalWalkSpeed = (int) calculateFinalStat(weapon.getWalkSpeed(), walkSpeedPerf);
        int finalXpBonus = (int) calculateFinalStat(weapon.getXpBonus(), xpBonusPerf);
        int finalLootBonus = (int) calculateFinalStat(weapon.getLootBonus(), lootBonusPerf);

        // 最終ステータスを更新
        container.set(keys.get("attack_damage"), PersistentDataType.DOUBLE, finalDamage);
        container.set(keys.get("attack_range"), PersistentDataType.DOUBLE, finalAttackRange);
        container.set(keys.get("attack_speed"), PersistentDataType.DOUBLE, finalAttackSpeed);
        container.set(keys.get("movement_speed"), PersistentDataType.INTEGER, finalWalkSpeed);
        container.set(keys.get("experience_bonus"), PersistentDataType.INTEGER, finalXpBonus);
        container.set(keys.get("drop_rate_bonus"), PersistentDataType.INTEGER, finalLootBonus);

        // アイテムの説明文を更新
        long version = container.getOrDefault(keys.get("item_version"), PersistentDataType.LONG, 0L);
        String weaponType = container.getOrDefault(keys.get("weapon_type"), PersistentDataType.STRING, "");
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

        // ステータスの表示
        if (weapon.getDamage() != 0)
            lore.add(buildStatLine("攻撃力", String.valueOf(finalDamage), String.valueOf(damagePerf)));
        if (weapon.getAttackRange() != 0)
            lore.add(buildStatLine("攻撃距離", String.valueOf(finalAttackRange), String.valueOf(attackRangePerf)));
        if (weapon.getAttackSpeed() != 0)
            lore.add(buildStatLine("攻撃速度", String.valueOf(finalAttackSpeed), String.valueOf(attackSpeedPerf)));
        if (weapon.getWalkSpeed() != 0)
            lore.add(buildStatLine("移動速度", String.valueOf(finalWalkSpeed), String.valueOf(walkSpeedPerf)));
        if (weapon.getXpBonus() != 0)
            lore.add(buildStatLine("経験値ボーナス", String.valueOf(finalXpBonus), String.valueOf(xpBonusPerf)));
        if (weapon.getLootBonus() != 0)
            lore.add(buildStatLine("ドロップ率ボーナス", String.valueOf(finalLootBonus), String.valueOf(lootBonusPerf)));

        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return true;
    }
}