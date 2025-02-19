package moe.nmkmn.alcaris_items.enums;

public enum TextureIcons {
    // ステータス用
    HP("\uE100"), // HP
    HP_REGEN("\uE101"), // HP自然回復
    MP("\uE102"), // MP
    MP_REGEN("\uE103"), // MP自然回復
    STR("\uE104"), // 物理
    INT("\uE105"), // 魔法・賢さ
    DEX("\uE106"), // 器用
    DEF("\uE108"), // 防御
    AGI("\uE109"), // 素早さ
    CUT("\uE10A"), // 会心
    LUC("\uE10B"), // 幸運
    LVL("\uE100"), // レベル
    EXP("\uE100"), // 経験値

    // アイコン用
    HUNGER_10("\uE10C"), // 満腹度 1.0
    HUNGER_05("\uE10D"), // 満腹度 0.5

    // レアリティ用
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

    public static TextureIcons fromRarity(int rarity) {
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
