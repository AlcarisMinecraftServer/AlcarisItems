package net.alcaris.plugin.items.magic;

import java.util.List;

public class MagicSpellBuffConfig {
    public static class EffectSpec {
        public final String type;
        public final int amplifier;
        public final int durationTicks;

        public EffectSpec(String type, int amplifier, int durationTicks) {
            this.type = type;
            this.amplifier = amplifier;
            this.durationTicks = durationTicks;
        }
    }

    private final List<EffectSpec> effects;

    public MagicSpellBuffConfig(List<EffectSpec> effects) {
        this.effects = effects;
    }

    public List<EffectSpec> getEffects() {
        return effects;
    }
}
