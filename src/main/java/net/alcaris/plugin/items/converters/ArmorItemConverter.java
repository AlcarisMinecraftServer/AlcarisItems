package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemArmorModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.utils.RarityUtils;
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
        String[] keyNames = {
            "item_id", "item_version", "armor_type", "required_level", "max_modification",
            "durability", "hp", "hp_perf", "hpr", "hpr_perf", "mp", "mp_perf", "mpr", "mpr_perf",
            "atk", "atk_perf", "def", "def_perf", "mat", "mat_perf", "mdf", "mdf_perf",
            "dex", "dex_perf", "speed", "speed_perf"
        };

        for (String name : keyNames) {
            keyMap.put(name, new NamespacedKey(plugin, name));
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

        return commonSetting(
            item,
            armor,
            amount,
            armor.getType(),
            armor.getRequirement(),
            maxModification,
            (int) armor.getDurability(), // durabilityもdoubleからintに変換
            0, // 初期性能値を0に設定
            0, 0, 0, 0, 0, 0, 0, 0, 0
        );
    }

    public ItemStack updateItem(ItemBaseModel item, ItemArmorModel armor, int amount, ItemStack oldItem) {
        ItemMeta oldMeta = oldItem.getItemMeta();
        if (oldMeta == null) return oldItem;

        PersistentDataContainer oldItemContainer = oldMeta.getPersistentDataContainer();
        int maxModification = oldItemContainer.getOrDefault(keys.get("max_modification"), PersistentDataType.INTEGER, 0);

        return commonSetting(
            item,
            armor,
            amount,
            armor.getType(),
            armor.getRequirement(),
            maxModification,
            0, // durability is not available in backward compatibility
            oldItemContainer.getOrDefault(keys.get("hp_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("hpr_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("mp_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("mpr_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("atk_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("def_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("mat_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("mdf_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("dex_perf"), PersistentDataType.INTEGER, 0),
            oldItemContainer.getOrDefault(keys.get("speed_perf"), PersistentDataType.INTEGER, 0)
        );
    }

    private ItemStack commonSetting(ItemBaseModel item, ItemArmorModel armor, int amount,
                                  String type, int requirement, int maxModification,
                                  int durability, int hpPerformance, int hprPerformance,
                                  int mpPerformance, int mprPerformance, int atkPerformance,
                                  int defPerformance, int matPerformance, int mdfPerformance,
                                  int dexPerformance, int speedPerformance) {
        ItemStack itemStack = new ItemStack(getArmorMaterial(item.getId()), amount);
        Damageable itemMeta = (Damageable) itemStack.getItemMeta();

        if (itemMeta == null) return itemStack;

        // Calculate final stats
        double finalHp = calculateFinalStat(armor.getHp(), hpPerformance);
        double finalHpr = calculateFinalStat(armor.getHpr(), hprPerformance);
        double finalMp = calculateFinalStat(armor.getMp(), mpPerformance);
        double finalMpr = calculateFinalStat(armor.getMpr(), mprPerformance);
        double finalAtk = calculateFinalStat(armor.getAtk(), atkPerformance);
        double finalDef = calculateFinalStat(armor.getDef(), defPerformance);
        double finalMat = calculateFinalStat(armor.getMat(), matPerformance);
        double finalMdf = calculateFinalStat(armor.getMdf(), mdfPerformance);
        double finalDex = calculateFinalStat(armor.getDex(), dexPerformance);
        double finalSpeed = calculateFinalStat(armor.getSpeed(), speedPerformance);

        // Set basic item properties
        setupBasicProperties(itemMeta, item);
        
        // Set durability if available
        if (itemMeta instanceof Damageable damageable && durability > 0) {
            damageable.setMaxDamage(durability);
        }
        
        // Set lore
        List<Component> lore = createLore(item, requirement, maxModification, armor,
            finalHp, finalHpr, finalMp, finalMpr, finalAtk, finalDef, finalMat, finalMdf, finalDex, finalSpeed,
            hpPerformance, hprPerformance, mpPerformance, mprPerformance, atkPerformance,
            defPerformance, matPerformance, mdfPerformance, dexPerformance, speedPerformance);
        itemMeta.lore(lore);

        // Set persistent data
        setupPersistentData(itemMeta, item, type != null ? type : "default", requirement, maxModification, durability,
            finalHp, hpPerformance, finalHpr, hprPerformance, finalMp, mpPerformance, finalMpr, mprPerformance,
            finalAtk, atkPerformance, finalDef, defPerformance, finalMat, matPerformance, finalMdf, mdfPerformance,
            finalDex, dexPerformance, finalSpeed, speedPerformance);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private double calculateFinalStat(double baseValue, int performance) {
        return Math.floor(baseValue * (0.5 * (1 + performance / 100.0)) * 100) / 100.0;
    }

    private void setupBasicProperties(ItemMeta itemMeta, ItemBaseModel item) {
        itemMeta.displayName(
            plugin.miniMessage.deserialize(item.getName())
                .color(TextColor.fromHexString(RarityUtils.getColor(item.getRarity()).getHexCode()))
                .decoration(TextDecoration.ITALIC, false)
        );
        itemMeta.setMaxStackSize(item.getMaxStack());

        if (item.getCustomModelData() != 0) {
            itemMeta.setCustomModelData(item.getCustomModelData());
        }
    }

    private List<Component> createLore(ItemBaseModel item, int requirement, int polishingCount,
                                     ItemArmorModel armor, double finalHp, double finalHpr, double finalMp,
                                     double finalMpr, double finalAtk, double finalDef, double finalMat,
                                     double finalMdf, double finalDex, double finalSpeed,
                                     int hpPerformance, int hprPerformance, int mpPerformance, int mprPerformance,
                                     int atkPerformance, int defPerformance, int matPerformance, int mdfPerformance,
                                     int dexPerformance, int speedPerformance) {
        List<Component> lore = new ArrayList<>();

        // Add rarity at the top
        lore.add(Component.text("【" + RarityUtils.getIcon(item.getRarity()).getUnicode() + "】 " + "レアリティ: " + item.getRarity())
            .color(TextColor.fromHexString(RarityUtils.getColor(item.getRarity()).getHexCode()))
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

        // Add armor stats
        if (armor.getHp() != 0)
            lore.add(buildStatLine("HP", String.valueOf(finalHp), String.valueOf(hpPerformance)));
        if (armor.getHpr() != 0)
            lore.add(buildStatLine("HPR", String.valueOf(finalHpr), String.valueOf(hprPerformance)));
        if (armor.getMp() != 0)
            lore.add(buildStatLine("MP", String.valueOf(finalMp), String.valueOf(mpPerformance)));
        if (armor.getMpr() != 0)
            lore.add(buildStatLine("MPR", String.valueOf(finalMpr), String.valueOf(mprPerformance)));
        if (armor.getAtk() != 0)
            lore.add(buildStatLine("ATK", String.valueOf(finalAtk), String.valueOf(atkPerformance)));
        if (armor.getDef() != 0)
            lore.add(buildStatLine("DEF", String.valueOf(finalDef), String.valueOf(defPerformance)));
        if (armor.getMat() != 0)
            lore.add(buildStatLine("MAT", String.valueOf(finalMat), String.valueOf(matPerformance)));
        if (armor.getMdf() != 0)
            lore.add(buildStatLine("MDF", String.valueOf(finalMdf), String.valueOf(mdfPerformance)));
        if (armor.getDex() != 0)
            lore.add(buildStatLine("DEX", String.valueOf(finalDex), String.valueOf(dexPerformance)));
        if (armor.getSpeed() != 0)
            lore.add(buildStatLine("SPEED", String.valueOf(finalSpeed), String.valueOf(speedPerformance)));

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
                                   double finalHp, int hpPerformance, double finalHpr, int hprPerformance,
                                   double finalMp, int mpPerformance, double finalMpr, int mprPerformance,
                                   double finalAtk, int atkPerformance, double finalDef, int defPerformance,
                                   double finalMat, int matPerformance, double finalMdf, int mdfPerformance,
                                   double finalDex, int dexPerformance, double finalSpeed, int speedPerformance) {
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        
        container.set(keys.get("item_id"), PersistentDataType.STRING, item.getId());
        container.set(keys.get("item_version"), PersistentDataType.LONG, item.getVersion());
        container.set(keys.get("armor_type"), PersistentDataType.STRING, armorType != null ? armorType : "default");
        container.set(keys.get("required_level"), PersistentDataType.INTEGER, requiredLevel);
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, maxModification);
        container.set(keys.get("durability"), PersistentDataType.INTEGER, durability);
        
        container.set(keys.get("hp"), PersistentDataType.DOUBLE, finalHp);
        container.set(keys.get("hp_perf"), PersistentDataType.INTEGER, hpPerformance);
        container.set(keys.get("hpr"), PersistentDataType.DOUBLE, finalHpr);
        container.set(keys.get("hpr_perf"), PersistentDataType.INTEGER, hprPerformance);
        container.set(keys.get("mp"), PersistentDataType.DOUBLE, finalMp);
        container.set(keys.get("mp_perf"), PersistentDataType.INTEGER, mpPerformance);
        container.set(keys.get("mpr"), PersistentDataType.DOUBLE, finalMpr);
        container.set(keys.get("mpr_perf"), PersistentDataType.INTEGER, mprPerformance);
        container.set(keys.get("atk"), PersistentDataType.DOUBLE, finalAtk);
        container.set(keys.get("atk_perf"), PersistentDataType.INTEGER, atkPerformance);
        container.set(keys.get("def"), PersistentDataType.DOUBLE, finalDef);
        container.set(keys.get("def_perf"), PersistentDataType.INTEGER, defPerformance);
        container.set(keys.get("mat"), PersistentDataType.DOUBLE, finalMat);
        container.set(keys.get("mat_perf"), PersistentDataType.INTEGER, matPerformance);
        container.set(keys.get("mdf"), PersistentDataType.DOUBLE, finalMdf);
        container.set(keys.get("mdf_perf"), PersistentDataType.INTEGER, mdfPerformance);
        container.set(keys.get("dex"), PersistentDataType.DOUBLE, finalDex);
        container.set(keys.get("dex_perf"), PersistentDataType.INTEGER, dexPerformance);
        container.set(keys.get("speed"), PersistentDataType.DOUBLE, finalSpeed);
        container.set(keys.get("speed_perf"), PersistentDataType.INTEGER, speedPerformance);
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

        // 現在の性能値を取得
        int hpPerf = container.getOrDefault(keys.get("hp_perf"), PersistentDataType.INTEGER, 0);
        int hprPerf = container.getOrDefault(keys.get("hpr_perf"), PersistentDataType.INTEGER, 0);
        int mpPerf = container.getOrDefault(keys.get("mp_perf"), PersistentDataType.INTEGER, 0);
        int mprPerf = container.getOrDefault(keys.get("mpr_perf"), PersistentDataType.INTEGER, 0);
        int atkPerf = container.getOrDefault(keys.get("atk_perf"), PersistentDataType.INTEGER, 0);
        int defPerf = container.getOrDefault(keys.get("def_perf"), PersistentDataType.INTEGER, 0);
        int matPerf = container.getOrDefault(keys.get("mat_perf"), PersistentDataType.INTEGER, 0);
        int mdfPerf = container.getOrDefault(keys.get("mdf_perf"), PersistentDataType.INTEGER, 0);
        int dexPerf = container.getOrDefault(keys.get("dex_perf"), PersistentDataType.INTEGER, 0);
        int speedPerf = container.getOrDefault(keys.get("speed_perf"), PersistentDataType.INTEGER, 0);

        // アップグレードタイプに応じて処理
        switch (type) {
            case ROLL_ALL:
                hpPerf = calculatePerformance();
                hprPerf = calculatePerformance();
                mpPerf = calculatePerformance();
                mprPerf = calculatePerformance();
                atkPerf = calculatePerformance();
                defPerf = calculatePerformance();
                matPerf = calculatePerformance();
                mdfPerf = calculatePerformance();
                dexPerf = calculatePerformance();
                speedPerf = calculatePerformance();
                break;

            case FIXED_INCREMENT:
                if (specificStat != null) {
                    switch (specificStat.toLowerCase()) {
                        case "hp" -> hpPerf = Math.min(100, hpPerf + PERFORMANCE_INCREMENT);
                        case "hpr" -> hprPerf = Math.min(100, hprPerf + PERFORMANCE_INCREMENT);
                        case "mp" -> mpPerf = Math.min(100, mpPerf + PERFORMANCE_INCREMENT);
                        case "mpr" -> mprPerf = Math.min(100, mprPerf + PERFORMANCE_INCREMENT);
                        case "atk" -> atkPerf = Math.min(100, atkPerf + PERFORMANCE_INCREMENT);
                        case "def" -> defPerf = Math.min(100, defPerf + PERFORMANCE_INCREMENT);
                        case "mat" -> matPerf = Math.min(100, matPerf + PERFORMANCE_INCREMENT);
                        case "mdf" -> mdfPerf = Math.min(100, mdfPerf + PERFORMANCE_INCREMENT);
                        case "dex" -> dexPerf = Math.min(100, dexPerf + PERFORMANCE_INCREMENT);
                        case "speed" -> speedPerf = Math.min(100, speedPerf + PERFORMANCE_INCREMENT);
                    }
                }
                break;

            case ROLL_SPECIFIC:
                if (specificStat != null) {
                    switch (specificStat.toLowerCase()) {
                        case "hp" -> hpPerf = calculatePerformance();
                        case "hpr" -> hprPerf = calculatePerformance();
                        case "mp" -> mpPerf = calculatePerformance();
                        case "mpr" -> mprPerf = calculatePerformance();
                        case "atk" -> atkPerf = calculatePerformance();
                        case "def" -> defPerf = calculatePerformance();
                        case "mat" -> matPerf = calculatePerformance();
                        case "mdf" -> mdfPerf = calculatePerformance();
                        case "dex" -> dexPerf = calculatePerformance();
                        case "speed" -> speedPerf = calculatePerformance();
                    }
                }
                break;

            case ROLL_AND_KEEP_HIGH:
                if (specificStat != null) {
                    int newRoll = calculatePerformance();
                    switch (specificStat.toLowerCase()) {
                        case "hp" -> hpPerf = Math.max(hpPerf, newRoll);
                        case "hpr" -> hprPerf = Math.max(hprPerf, newRoll);
                        case "mp" -> mpPerf = Math.max(mpPerf, newRoll);
                        case "mpr" -> mprPerf = Math.max(mprPerf, newRoll);
                        case "atk" -> atkPerf = Math.max(atkPerf, newRoll);
                        case "def" -> defPerf = Math.max(defPerf, newRoll);
                        case "mat" -> matPerf = Math.max(matPerf, newRoll);
                        case "mdf" -> mdfPerf = Math.max(mdfPerf, newRoll);
                        case "dex" -> dexPerf = Math.max(dexPerf, newRoll);
                        case "speed" -> speedPerf = Math.max(speedPerf, newRoll);
                    }
                }
                break;
        }

        // 性能値を更新
        container.set(keys.get("hp_perf"), PersistentDataType.INTEGER, hpPerf);
        container.set(keys.get("hpr_perf"), PersistentDataType.INTEGER, hprPerf);
        container.set(keys.get("mp_perf"), PersistentDataType.INTEGER, mpPerf);
        container.set(keys.get("mpr_perf"), PersistentDataType.INTEGER, mprPerf);
        container.set(keys.get("atk_perf"), PersistentDataType.INTEGER, atkPerf);
        container.set(keys.get("def_perf"), PersistentDataType.INTEGER, defPerf);
        container.set(keys.get("mat_perf"), PersistentDataType.INTEGER, matPerf);
        container.set(keys.get("mdf_perf"), PersistentDataType.INTEGER, mdfPerf);
        container.set(keys.get("dex_perf"), PersistentDataType.INTEGER, dexPerf);
        container.set(keys.get("speed_perf"), PersistentDataType.INTEGER, speedPerf);

        // maxModificationを減らす
        int newMaxModification = currentMaxModification - 1;
        container.set(keys.get("max_modification"), PersistentDataType.INTEGER, newMaxModification);

        // アイテムのステータスを再計算（基礎値はItemArmorModelから取得）
        double finalHp = calculateFinalStat(armor.getHp(), hpPerf);
        double finalHpr = calculateFinalStat(armor.getHpr(), hprPerf);
        double finalMp = calculateFinalStat(armor.getMp(), mpPerf);
        double finalMpr = calculateFinalStat(armor.getMpr(), mprPerf);
        double finalAtk = calculateFinalStat(armor.getAtk(), atkPerf);
        double finalDef = calculateFinalStat(armor.getDef(), defPerf);
        double finalMat = calculateFinalStat(armor.getMat(), matPerf);
        double finalMdf = calculateFinalStat(armor.getMdf(), mdfPerf);
        double finalDex = calculateFinalStat(armor.getDex(), dexPerf);
        double finalSpeed = calculateFinalStat(armor.getSpeed(), speedPerf);

        // 最終ステータスを更新
        container.set(keys.get("hp"), PersistentDataType.DOUBLE, finalHp);
        container.set(keys.get("hpr"), PersistentDataType.DOUBLE, finalHpr);
        container.set(keys.get("mp"), PersistentDataType.DOUBLE, finalMp);
        container.set(keys.get("mpr"), PersistentDataType.DOUBLE, finalMpr);
        container.set(keys.get("atk"), PersistentDataType.DOUBLE, finalAtk);
        container.set(keys.get("def"), PersistentDataType.DOUBLE, finalDef);
        container.set(keys.get("mat"), PersistentDataType.DOUBLE, finalMat);
        container.set(keys.get("mdf"), PersistentDataType.DOUBLE, finalMdf);
        container.set(keys.get("dex"), PersistentDataType.DOUBLE, finalDex);
        container.set(keys.get("speed"), PersistentDataType.DOUBLE, finalSpeed);

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
        lore.add(Component.text("【" + RarityUtils.getIcon(rarity).getUnicode() + "】 " + "レアリティ: " + rarity)
            .color(TextColor.fromHexString(RarityUtils.getColor(rarity).getHexCode()))
            .decoration(TextDecoration.ITALIC, false));

        // 区切り線
        lore.add(Component.text("                          ")
            .color(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.STRIKETHROUGH, true));

        // 必要レベルと改造回数の表示
        lore.add(buildInfoLine("必要レベル", String.valueOf(requiredLevel)));
        lore.add(buildInfoLine("残り改造回数", String.valueOf(newMaxModification)));

        // ステータスの表示
        if (armor.getHp() != 0)
            lore.add(buildStatLine("HP", String.valueOf(finalHp), String.valueOf(hpPerf)));
        if (armor.getHpr() != 0)
            lore.add(buildStatLine("HPR", String.valueOf(finalHpr), String.valueOf(hprPerf)));
        if (armor.getMp() != 0)
            lore.add(buildStatLine("MP", String.valueOf(finalMp), String.valueOf(mpPerf)));
        if (armor.getMpr() != 0)
            lore.add(buildStatLine("MPR", String.valueOf(finalMpr), String.valueOf(mprPerf)));
        if (armor.getAtk() != 0)
            lore.add(buildStatLine("ATK", String.valueOf(finalAtk), String.valueOf(atkPerf)));
        if (armor.getDef() != 0)
            lore.add(buildStatLine("DEF", String.valueOf(finalDef), String.valueOf(defPerf)));
        if (armor.getMat() != 0)
            lore.add(buildStatLine("MAT", String.valueOf(finalMat), String.valueOf(matPerf)));
        if (armor.getMdf() != 0)
            lore.add(buildStatLine("MDF", String.valueOf(finalMdf), String.valueOf(mdfPerf)));
        if (armor.getDex() != 0)
            lore.add(buildStatLine("DEX", String.valueOf(finalDex), String.valueOf(dexPerf)));
        if (armor.getSpeed() != 0)
            lore.add(buildStatLine("SPEED", String.valueOf(finalSpeed), String.valueOf(speedPerf)));

        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return true;
    }
} 