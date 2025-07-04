package net.alcaris.plugin.items.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 防具ステータスデータを管理するヘルパークラス
 */
public class ArmorStatData {
    private final Map<ArmorStats, Integer> finalValues = new HashMap<>();
    private final Map<ArmorStats, Integer> performanceValues = new HashMap<>();

    public ArmorStatData() {
        // 初期化
        for (ArmorStats stat : ArmorStats.values()) {
            finalValues.put(stat, 0);
            performanceValues.put(stat, 0);
        }
    }

    public void setFinalValue(ArmorStats stat, int value) {
        finalValues.put(stat, value);
    }

    public void setPerformanceValue(ArmorStats stat, int value) {
        performanceValues.put(stat, value);
    }

    public int getFinalValue(ArmorStats stat) {
        return finalValues.getOrDefault(stat, 0);
    }

    public int getPerformanceValue(ArmorStats stat) {
        return performanceValues.getOrDefault(stat, 0);
    }

    public Map<ArmorStats, Integer> getFinalValues() {
        return new HashMap<>(finalValues);
    }

    public Map<ArmorStats, Integer> getPerformanceValues() {
        return new HashMap<>(performanceValues);
    }

    /**
     * 配列から値を設定
     */
    public void setFromArrays(int[] finalValues, int[] performanceValues) {
        ArmorStats[] stats = ArmorStats.values();
        for (int i = 0; i < stats.length && i < finalValues.length && i < performanceValues.length; i++) {
            this.finalValues.put(stats[i], finalValues[i]);
            this.performanceValues.put(stats[i], performanceValues[i]);
        }
    }

    /**
     * 配列として取得
     */
    public int[] getFinalValuesAsArray() {
        ArmorStats[] stats = ArmorStats.values();
        int[] result = new int[stats.length];
        for (int i = 0; i < stats.length; i++) {
            result[i] = finalValues.getOrDefault(stats[i], 0);
        }
        return result;
    }

    public int[] getPerformanceValuesAsArray() {
        ArmorStats[] stats = ArmorStats.values();
        int[] result = new int[stats.length];
        for (int i = 0; i < stats.length; i++) {
            result[i] = performanceValues.getOrDefault(stats[i], 0);
        }
        return result;
    }
} 