package net.alcaris.plugin.items.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 武器ステータスデータを管理するヘルパークラス
 */
public class WeaponStatData {
    private final Map<WeaponStats, Double> finalValues = new HashMap<>();
    private final Map<WeaponStats, Integer> performanceValues = new HashMap<>();

    public WeaponStatData() {
        // 初期化
        for (WeaponStats stat : WeaponStats.values()) {
            finalValues.put(stat, 0.0);
            performanceValues.put(stat, 0);
        }
    }

    public void setFinalValue(WeaponStats stat, double value) {
        finalValues.put(stat, value);
    }

    public void setPerformanceValue(WeaponStats stat, int value) {
        performanceValues.put(stat, value);
    }

    public double getFinalValue(WeaponStats stat) {
        return finalValues.getOrDefault(stat, 0.0);
    }

    public int getPerformanceValue(WeaponStats stat) {
        return performanceValues.getOrDefault(stat, 0);
    }

    public Map<WeaponStats, Double> getFinalValues() {
        return new HashMap<>(finalValues);
    }

    public Map<WeaponStats, Integer> getPerformanceValues() {
        return new HashMap<>(performanceValues);
    }

    /**
     * 配列から値を設定
     */
    public void setFromArrays(double[] finalValues, int[] performanceValues) {
        WeaponStats[] stats = WeaponStats.values();
        for (int i = 0; i < stats.length && i < finalValues.length && i < performanceValues.length; i++) {
            this.finalValues.put(stats[i], finalValues[i]);
            this.performanceValues.put(stats[i], performanceValues[i]);
        }
    }

    /**
     * 配列として取得
     */
    public double[] getFinalValuesAsArray() {
        WeaponStats[] stats = WeaponStats.values();
        double[] result = new double[stats.length];
        for (int i = 0; i < stats.length; i++) {
            result[i] = finalValues.getOrDefault(stats[i], 0.0);
        }
        return result;
    }

    public int[] getPerformanceValuesAsArray() {
        WeaponStats[] stats = WeaponStats.values();
        int[] result = new int[stats.length];
        for (int i = 0; i < stats.length; i++) {
            result[i] = performanceValues.getOrDefault(stats[i], 0);
        }
        return result;
    }
} 