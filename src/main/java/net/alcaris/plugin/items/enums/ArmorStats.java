package net.alcaris.plugin.items.enums;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.alcaris.plugin.core.model.item.ItemArmorModel;

public enum ArmorStats {
    HP("hp", "HP", ItemArmorModel::getHp),
    HPR("hpr", "HPR", ItemArmorModel::getHpr),
    MP("mp", "MP", ItemArmorModel::getMp),
    MPR("mpr", "MPR", ItemArmorModel::getMpr),
    ATK("atk", "ATK", ItemArmorModel::getAtk),
    DEF("def", "DEF", ItemArmorModel::getDef),
    MAT("mat", "MAT", ItemArmorModel::getMat),
    MDF("mdf", "MDF", ItemArmorModel::getMdf),
    DEX("dex", "DEX", ItemArmorModel::getDex),
    SPEED("speed", "SPEED", ItemArmorModel::getSpeed),
    CRT("crt", "CRT", ItemArmorModel::getCrt),
    CRD("crd", "CRD", ItemArmorModel::getCrd),
    LUK("luk", "LUK", ItemArmorModel::getLuk);

    private final String key;
    private final String displayName;
    private final Function<ItemArmorModel, Float> getter;

    ArmorStats(String key, String displayName, Function<ItemArmorModel, Float> getter) {
        this.key = key;
        this.displayName = displayName;
        this.getter = getter;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }


    public String getPerformanceKey() {
        return key + "_perf";
    }

    /**
     * ItemArmorModelから対応するステータス値を取得
     */
    public double getValue(ItemArmorModel armor) {
        return getter.apply(armor);
    }

    /**
     * キー名からArmorStatsを取得
     */
    public static ArmorStats fromKey(String key) {
        return Arrays.stream(values())
                .filter(stat -> stat.getKey().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    /**
     * すべてのキー名を取得
     */
    public static String[] getAllKeys() {
        return Arrays.stream(values())
                .map(ArmorStats::getKey)
                .toArray(String[]::new);
    }

    /**
     * すべてのキー名をStreamで取得
     */
    public static Stream<String> getAllKeysStream() {
        return Arrays.stream(values())
                .map(ArmorStats::getKey);
    }

    /**
     * すべてのバリューキーを取得（PersistentDataContainer用）
     */
    public static String[] getAllValueKeys() {
        return Arrays.stream(values())
                .map(ArmorStats::getKey)
                .toArray(String[]::new);
    }

    /**
     * すべてのパフォーマンスキーを取得（PersistentDataContainer用）
     */
    public static String[] getAllPerformanceKeys() {
        return Arrays.stream(values())
                .map(ArmorStats::getPerformanceKey)
                .toArray(String[]::new);
    }

    /**
     * PersistentDataContainer用のすべてのキーを取得
     */
    public static String[] getAllPersistentKeys() {
        List<String> keys = Arrays.stream(values())
                .flatMap(stat -> Stream.of(stat.getKey(), stat.getPerformanceKey()))
                .toList();
        return keys.toArray(new String[0]);
    }
} 