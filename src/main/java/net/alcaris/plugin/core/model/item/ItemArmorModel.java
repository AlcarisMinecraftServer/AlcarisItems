package net.alcaris.plugin.core.model.item;

import java.util.List;

@SuppressWarnings("unused")
public class ItemArmorModel {
    private String armor_type;
    private int required_level;
    private int max_modification;
    private int durability;
    private Base base;

    public static class Base {
        private Attributes attributes;
        private List<Buff> buffs;
        private List<Effect> effects;

        public Attributes getAttributes() {
            return attributes;
        }

        public List<Buff> getBuffs() {
            return buffs;
        }

        public List<Effect> getEffects() {
            return effects;
        }
    }

    public static class Attributes {
        private int hp;
        private int hpr;
        private int mp;
        private int mpr;
        private int atk;
        private int def;
        private int mat;
        private int mdf;
        private int dex;
        private int speed;
        private int crt;  // クリティカル率
        private int crd;  // クリティカルダメージ
        private int luk;  // 幸運

        public int getHp() {
            return hp;
        }

        public int getHpr() {
            return hpr;
        }

        public int getMp() {
            return mp;
        }

        public int getMpr() {
            return mpr;
        }

        public int getAtk() {
            return atk;
        }

        public int getDef() {
            return def;
        }

        public int getMat() {
            return mat;
        }

        public int getMdf() {
            return mdf;
        }

        public int getDex() {
            return dex;
        }

        public int getSpeed() {
            return speed;
        }

        public int getCrt() {
            return crt;
        }

        public int getCrd() {
            return crd;
        }

        public int getLuk() {
            return luk;
        }
    }

    public static class Buff {
        // TODO: Implement buff structure
    }

    public static class Effect {
        // TODO: Implement effect structure
    }

    // Backward compatibility methods
    public String getType() {
        return armor_type;
    }

    public int getRequirement() {
        return required_level;
    }

    public int getPolishingCount() {
        return 0; // New structure doesn't use polishing count
    }

    public int getHp() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getHp() : 0;
    }

    public int getHpr() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getHpr() : 0;
    }

    public int getMp() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMp() : 0;
    }

    public int getMpr() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMpr() : 0;
    }

    public int getAtk() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getAtk() : 0;
    }

    public int getDef() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getDef() : 0;
    }

    public int getMat() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMat() : 0;
    }

    public int getMdf() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMdf() : 0;
    }

    public int getDex() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getDex() : 0;
    }

    public int getSpeed() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getSpeed() : 0;
    }

    public int getCrt() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getCrt() : 0;
    }

    public int getCrd() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getCrd() : 0;
    }

    public int getLuk() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getLuk() : 0;
    }

    // New getters
    public String getArmorType() {
        return armor_type;
    }

    public int getRequiredLevel() {
        return required_level;
    }

    public int getMaxModification() {
        return max_modification;
    }

    public int getDurability() {
        return durability;
    }

    public Base getBase() {
        return base;
    }
} 