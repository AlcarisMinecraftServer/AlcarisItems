package net.alcaris.plugin.items.enums;

public enum Colors {
    RARITY_1("#CCCCCC"),
    RARITY_2("#79EC6E"),
    RARITY_3("#789AF7"),
    RARITY_4("#DB63EB"),
    RARITY_5("#EFE33E");

    private final String hexCode;

    Colors(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getHexCode() {
        return hexCode;
    }

    @SuppressWarnings("Duplicates")
    public static Colors fromRarity(int rarity) {
        return switch (rarity) {
            case 1 -> RARITY_1;
            case 2 -> RARITY_2;
            case 3 -> RARITY_3;
            case 4 -> RARITY_4;
            case 5 -> RARITY_5;
            default -> throw new IllegalArgumentException("Invalid rarity: " + rarity);
        };
    }
}