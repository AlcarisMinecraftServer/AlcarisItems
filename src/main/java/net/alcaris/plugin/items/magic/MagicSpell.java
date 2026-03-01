package net.alcaris.plugin.items.magic;

public class MagicSpell {
    private final String id;
    private final String displayName;
    private final int mpCost;
    private final int cooldownTicks;
    private final MagicType type;

    private final MagicSpellBuffConfig buffConfig;
    private final MagicSpellAoeConfig aoeConfig;
    private final MagicSpellThrowConfig throwConfig;

    public MagicSpell(
            String id,
            String displayName,
            int mpCost,
            int cooldownTicks,
            MagicType type,
            MagicSpellBuffConfig buffConfig,
            MagicSpellAoeConfig aoeConfig,
            MagicSpellThrowConfig throwConfig
    ) {
        this.id = id;
        this.displayName = displayName;
        this.mpCost = mpCost;
        this.cooldownTicks = cooldownTicks;
        this.type = type;
        this.buffConfig = buffConfig;
        this.aoeConfig = aoeConfig;
        this.throwConfig = throwConfig;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMpCost() {
        return mpCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public MagicType getType() {
        return type;
    }

    public MagicSpellBuffConfig getBuffConfig() {
        return buffConfig;
    }

    public MagicSpellAoeConfig getAoeConfig() {
        return aoeConfig;
    }

    public MagicSpellThrowConfig getThrowConfig() {
        return throwConfig;
    }
}
