package net.alcaris.plugin.items.enums;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;

public enum WeaponStats {
    DAMAGE("damage", "攻撃力", ItemWeaponModel::getDamage),
    WALK_SPEED("walkspeed", "移動速度", ItemWeaponModel::getWalkSpeed),
    ATTACK_RANGE("attackrange", "攻撃距離", ItemWeaponModel::getAttackRange),
    ATTACK_SPEED("attackspeed", "攻撃速度", ItemWeaponModel::getAttackSpeed),
    HPR("hpr", "HPR", ItemWeaponModel::getHpr),
    MP("mp", "MP", ItemWeaponModel::getMp),
    MPR("mpr", "MPR", ItemWeaponModel::getMpr),
    ATK("atk", "ATK", ItemWeaponModel::getAtk),
    DEF("def", "DEF", ItemWeaponModel::getDef),
    MDF("mdf", "MDF", ItemWeaponModel::getMdf),
    CRT("crt", "CRT", ItemWeaponModel::getCrt),
    CRD("crd", "CRD", ItemWeaponModel::getCrd),
    SPD("spd", "SPD", ItemWeaponModel::getSpd),
    LUK("luk", "LUK", ItemWeaponModel::getLuk);

    private final String key;
    private final String displayName;
    private final Function<ItemWeaponModel, Float> getter;

    WeaponStats(String key, String displayName, Function<ItemWeaponModel, Float> getter) {
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

    public double getValue(ItemWeaponModel weapon) {
        return getter.apply(weapon);
    }

    public static WeaponStats fromKey(String key) {
        return Arrays.stream(values())
                .filter(stat -> stat.getKey().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }

    public static String[] getAllKeys() {
        return Arrays.stream(values())
                .map(WeaponStats::getKey)
                .toArray(String[]::new);
    }

    public static Stream<String> getAllKeysStream() {
        return Arrays.stream(values())
                .map(WeaponStats::getKey);
    }

    public static String[] getAllValueKeys() {
        return Arrays.stream(values())
                .map(WeaponStats::getKey)
                .toArray(String[]::new);
    }

    public static String[] getAllPerformanceKeys() {
        return Arrays.stream(values())
                .map(WeaponStats::getPerformanceKey)
                .toArray(String[]::new);
    }

    public static String[] getAllPersistentKeys() {
        List<String> keys = Arrays.stream(values())
                .flatMap(stat -> Stream.of(stat.getKey(), stat.getPerformanceKey()))
                .toList();
        return keys.toArray(new String[0]);
    }
}
