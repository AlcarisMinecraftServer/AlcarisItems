package net.alcaris.plugin.items.utils;

import net.alcaris.plugin.items.enums.Colors;
import net.alcaris.plugin.items.enums.TextureIcons;

public class RarityUtils {
    private RarityUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final int MIN_RARITY = 1;
    private static final int MAX_RARITY = 5;

    public static void validate(int rarity) {
        if (rarity < MIN_RARITY || rarity > MAX_RARITY) {
            throw new IllegalArgumentException("Invalid rarity: " + rarity + " (must be between " + MIN_RARITY + " and " + MAX_RARITY + ")");
        }
    }

    public static Colors getColor(int rarity) {
        validate(rarity);
        return Colors.values()[rarity - 1];
    }

    public static TextureIcons getIcon(int rarity) {
        validate(rarity);
        return switch (rarity) {
            case 1 -> TextureIcons.RARITY_1;
            case 2 -> TextureIcons.RARITY_2;
            case 3 -> TextureIcons.RARITY_3;
            case 4 -> TextureIcons.RARITY_4;
            case 5 -> TextureIcons.RARITY_5;
            default -> throw new IllegalArgumentException("Invalid rarity: " + rarity);
        };
    }
}
