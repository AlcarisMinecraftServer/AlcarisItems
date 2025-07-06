package net.alcaris.plugin.items.enums;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;

public enum WeaponStats {
    DAMAGE("damage", "攻撃力", "attack_damage", "attack_damage_perf", ItemWeaponModel::getDamage),
    WALK_SPEED("walkspeed", "移動速度", "movement_speed", "movement_speed_perf", ItemWeaponModel::getWalkSpeed),
    ATTACK_RANGE("attackrange", "攻撃距離", "attack_range", "attack_range_perf", ItemWeaponModel::getAttackRange),
    ATTACK_SPEED("attackspeed", "攻撃速度", "attack_speed", "attack_speed_perf", ItemWeaponModel::getAttackSpeed),
    HPR("hpr", "HPR", "hpr", "hpr_perf", ItemWeaponModel::getHpr),
    MP("mp", "MP", "mp", "mp_perf", ItemWeaponModel::getMp),
    MPR("mpr", "MPR", "mpr", "mpr_perf", ItemWeaponModel::getMpr),
    ATK("atk", "ATK", "atk", "atk_perf", ItemWeaponModel::getAtk),
    DEF("def", "DEF", "def", "def_perf", ItemWeaponModel::getDef),
    MDF("mdf", "MDF", "mdf", "mdf_perf", ItemWeaponModel::getMdf),
    CRT("crt", "CRT", "crt", "crt_perf", ItemWeaponModel::getCrt),
    CRD("crd", "CRD", "crd", "crd_perf", ItemWeaponModel::getCrd),
    SPD("spd", "SPD", "spd", "spd_perf", ItemWeaponModel::getSpd),
    LUK("luk", "LUK", "luk", "luk_perf", ItemWeaponModel::getLuk);

    private final String key;
    private final String displayName;
    private final String valueKey;
    private final String performanceKey;
    private final Function<ItemWeaponModel, Double> getter;

    WeaponStats(String key, String displayName, String valueKey, String performanceKey, 
                Function<ItemWeaponModel, Double> getter) {
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
     * ItemWeaponModelから対応するステータス値を取得
     */
    public double getValue(ItemWeaponModel weapon) {
        return getter.apply(weapon);
    }

    /**
     * キー名からWeaponStatsを取得
     */
    public static WeaponStats fromKey(String key) {
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
                .map(WeaponStats::getKey)
                .toArray(String[]::new);
    }

    /**
     * すべてのキー名をStreamで取得
     */
    public static Stream<String> getAllKeysStream() {
        return Arrays.stream(values())
                .map(WeaponStats::getKey);
    }

    /**
     * すべてのバリューキーを取得（PersistentDataContainer用）
     */
    public static String[] getAllValueKeys() {
        return Arrays.stream(values())
                .map(WeaponStats::getValueKey)
                .toArray(String[]::new);
    }

    /**
     * すべてのパフォーマンスキーを取得（PersistentDataContainer用）
     */
    public static String[] getAllPerformanceKeys() {
        return Arrays.stream(values())
                .map(WeaponStats::getPerformanceKey)
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