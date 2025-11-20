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
}