package net.alcaris.plugin.items.lib;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MaterialScoreUtils {
    public static final List<String> SCORE_KEYS = List.of(
            "red",
            "orange",
            "yellow",
            "lime",
            "green",
            "cyan",
            "aqua",
            "blue",
            "pink",
            "purple",
            "black",
            "gray",
            "white"
    );

    private MaterialScoreUtils() {
    }

    public static int getScore(Object data, String key) {
        if (data == null) {
            return 0;
        }

        if (data instanceof Map<?, ?> map) {
            return numberToInt(map.get(key));
        }

        String getterName = "get" + key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);

        try {
            Method getter = data.getClass().getMethod(getterName);
            return numberToInt(getter.invoke(data));
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    public static Map<String, Integer> toOrderedMap(Object data) {
        Map<String, Integer> scores = new LinkedHashMap<>();

        for (String key : SCORE_KEYS) {
            scores.put(key, getScore(data, key));
        }

        return scores;
    }

    public static boolean hasAnyScore(Object data) {
        for (String key : SCORE_KEYS) {
            if (getScore(data, key) != 0) {
                return true;
            }
        }

        return false;
    }

    private static int numberToInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        return 0;
    }
}
