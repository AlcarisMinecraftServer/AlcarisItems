package net.alcaris.plugin.items.enums;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Map;

public class EffectInfo {
    public static class Info {
        public final String nameJa;
        public final TextColor color;
        public final String icon;

        public Info(String nameJa, TextColor color, String icon) {
            this.nameJa = nameJa;
            this.color = color;
            this.icon = icon;
        }
    }

    private static final Map<String, Info> EFFECTS = Map.ofEntries(
            Map.entry("poison",         new Info("毒", TextColor.fromHexString("#FF4D4D"), "\uF118")),
            Map.entry("haste",          new Info("採掘速度上昇", NamedTextColor.BLUE, "\uF119")),
            Map.entry("hunger",         new Info("空腹", TextColor.fromHexString("#FF4D4D"), "\uF11A")),
            Map.entry("jump_boost",     new Info("跳躍力上昇", NamedTextColor.BLUE, "\uF11B")),
            Map.entry("blindness",      new Info("盲目", TextColor.fromHexString("#FF4D4D"), "\uF11C")),
            Map.entry("water_breathing",new Info("水中呼吸", NamedTextColor.BLUE, "\uF11D")),
            Map.entry("regeneration",   new Info("再生能力", NamedTextColor.BLUE, "\uF11E")),
            Map.entry("fire_resistance",new Info("火炎耐性", NamedTextColor.BLUE, "\uF120")),
            Map.entry("slow_falling",   new Info("落下速度低下", NamedTextColor.BLUE, "\uF121")),
            Map.entry("invisibility",   new Info("透明化", NamedTextColor.BLUE, "\uF122")),
            Map.entry("wither",         new Info("衰弱", TextColor.fromHexString("#FF4D4D"), "\uF123"))
    );

    public static Info get(String effectId) {
        return EFFECTS.getOrDefault(effectId.toLowerCase(), new Info(effectId, TextColor.color(0xAAAAAA), ""));
    }
}
