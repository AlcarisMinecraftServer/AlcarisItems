package net.alcaris.plugin.items.lib;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MaterialScoreUtils {
    public static final String MAGIC_MATERIALS_KEY = "magic_materials";
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

        Integer typedValue = getTypedScore(data, key);
        if (typedValue != null) {
            return typedValue;
        }

        Integer mappedValue = getMappedScore(data, key);
        if (mappedValue != null) {
            return mappedValue;
        }

        String getterName = "get" + key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);

        try {
            Method getter = data.getClass().getMethod(getterName);
            return numberToInt(getter.invoke(data));
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private static Integer getTypedScore(Object data, String key) {
        try {
            Method getValue = data.getClass().getMethod("getValue", String.class, String.class);
            return numberToInt(getValue.invoke(data, MAGIC_MATERIALS_KEY, key));
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method asMap = data.getClass().getMethod("asMap", String.class);
            return getScoreFromMapValue(asMap.invoke(data, MAGIC_MATERIALS_KEY), key);
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method getEntries = data.getClass().getMethod("getEntries", String.class);
            return getScoreFromEntries(getEntries.invoke(data, MAGIC_MATERIALS_KEY), key);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Integer getMappedScore(Object data, String key) {
        if (!(data instanceof Map<?, ?> map)) {
            return null;
        }

        Object directValue = map.get(key);
        if (directValue instanceof Number number) {
            return number.intValue();
        }

        if (!map.containsKey(MAGIC_MATERIALS_KEY)) {
            return null;
        }

        return getScoreFromEntries(map.get(MAGIC_MATERIALS_KEY), key);
    }

    private static Integer getScoreFromMapValue(Object value, String key) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }

        return numberToInt(map.get(key));
    }

    private static Integer getScoreFromEntries(Object entriesObject, String key) {
        if (!(entriesObject instanceof Iterable<?> entries)) {
            return null;
        }

        for (Object entryObject : entries) {
            ParsedEntry entry = parseEntry(entryObject);
            if (entry != null && entry.key().equals(key)) {
                return entry.value();
            }
        }

        return 0;
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

    private static ParsedEntry parseEntry(Object entryObject) {
        if (!(entryObject instanceof String entry)) {
            return null;
        }

        int separatorIndex = entry.indexOf(':');
        if (separatorIndex <= 0 || separatorIndex == entry.length() - 1) {
            return null;
        }

        String key = entry.substring(0, separatorIndex).trim();
        String rawValue = entry.substring(separatorIndex + 1).trim();
        if (key.isEmpty()) {
            return null;
        }

        try {
            return new ParsedEntry(key, Integer.parseInt(rawValue));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record ParsedEntry(String key, int value) {
    }
}
