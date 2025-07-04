package net.alcaris.plugin.items.enums;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.alcaris.plugin.core.model.item.ItemArmorModel;

public enum ArmorStats {
    HP("hp", "HP", "hp", "hp_perf", ItemArmorModel::getHp),
    HPR("hpr", "HPR", "hpr", "hpr_perf", ItemArmorModel::getHpr),
    MP("mp", "MP", "mp", "mp_perf", ItemArmorModel::getMp),
    MPR("mpr", "MPR", "mpr", "mpr_perf", ItemArmorModel::getMpr),
    ATK("atk", "ATK", "atk", "atk_perf", ItemArmorModel::getAtk),
    DEF("def", "DEF", "def", "def_perf", ItemArmorModel::getDef),
    MAT("mat", "MAT", "mat", "mat_perf", ItemArmorModel::getMat),
    MDF("mdf", "MDF", "mdf", "mdf_perf", ItemArmorModel::getMdf),
    DEX("dex", "DEX", "dex", "dex_perf", ItemArmorModel::getDex),
    SPEED("speed", "SPEED", "speed", "speed_perf", ItemArmorModel::getSpeed),
    CRT("crt", "CRT", "crt", "crt_perf", ItemArmorModel::getCrt),
    CRD("crd", "CRD", "crd", "crd_perf", ItemArmorModel::getCrd),
    LUK("luk", "LUK", "luk", "luk_perf", ItemArmorModel::getLuk);

    private final String key;
    private final String displayName;
    private final String valueKey;
    private final String performanceKey;
    private final Function<ItemArmorModel, Double> getter;

    ArmorStats(String key, String displayName, String valueKey, String performanceKey, 
               Function<ItemArmorModel, Double> getter) {
        this.key = key;
        this.displayName = displayName;
        this.valueKey = valueKey;
        this.performanceKey = performanceKey;
        this.getter = getter;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getValueKey() {
        return valueKey;
    }

    public String getPerformanceKey() {
        return performanceKey;
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
                .map(ArmorStats::getValueKey)
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
                .flatMap(stat -> Stream.of(stat.getValueKey(), stat.getPerformanceKey()))
                .toList();
        return keys.toArray(new String[0]);
    }
} 