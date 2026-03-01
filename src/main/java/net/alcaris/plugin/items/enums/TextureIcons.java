package net.alcaris.plugin.items.enums;

public enum TextureIcons {

    HP("\uE100"),
    HP_REGEN("\uE101"),
    MP("\uE102"),
    MP_REGEN("\uE103"),
    STR("\uE104"),
    INT("\uE105"),
    DEX("\uE106"),
    DEF("\uE108"),
    AGI("\uE109"),
    CUT("\uE10A"),
    LUC("\uE10B"),
    LVL("\uE100"),
    EXP("\uE100"),

    HUNGER_10("\uE10C"),
    HUNGER_05("\uE10D"),

    RARITY_1("\uE150"),
    RARITY_2("\uE151"),
    RARITY_3("\uE152"),
    RARITY_4("\uE153"),
    RARITY_5("\uE154"),

    NONE("");

    private final String unicode;

    TextureIcons(String unicode) {
        this.unicode = unicode;
    }

    public String getUnicode() {
        return unicode;
    }
}
