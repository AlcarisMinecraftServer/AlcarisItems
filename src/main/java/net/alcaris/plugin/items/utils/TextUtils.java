package net.alcaris.plugin.items.utils;

public class TextUtils {
    private TextUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String toRomanNumeral(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "Lv" + level;
        };
    }
}
