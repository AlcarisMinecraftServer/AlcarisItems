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
        private double hp;
        private double hpr;
        private double mp;
        private double mpr;
        private double atk;
        private double def;
        private double mat;
        private double mdf;
        private double dex;
        private double speed;
        private double crt;  // クリティカル率
        private double crd;  // クリティカルダメージ
        private double luk;  // 幸運

        public double getHp() {
            return hp;
        }

        public double getHpr() {
            return hpr;
        }

        public double getMp() {
            return mp;
        }

        public double getMpr() {
            return mpr;
        }

        public double getAtk() {
            return atk;
        }

        public double getDef() {
            return def;
        }

        public double getMat() {
            return mat;
        }

        public double getMdf() {
            return mdf;
        }

        public double getDex() {
            return dex;
        }

        public double getSpeed() {
            return speed;
        }

        public double getCrt() {
            return crt;
        }

        public double getCrd() {
            return crd;
        }

        public double getLuk() {
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

    public double getHp() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getHp() : 0;
    }

    public double getHpr() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getHpr() : 0;
    }

    public double getMp() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMp() : 0;
    }

    public double getMpr() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMpr() : 0;
    }

    public double getAtk() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getAtk() : 0;
    }

    public double getDef() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getDef() : 0;
    }

    public double getMat() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMat() : 0;
    }

    public double getMdf() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getMdf() : 0;
    }

    public double getDex() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getDex() : 0;
    }

    public double getSpeed() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getSpeed() : 0;
    }

    public double getCrt() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getCrt() : 0;
    }

    public double getCrd() {
        return base != null && base.getAttributes() != null ? base.getAttributes().getCrd() : 0;
    }

    public double getLuk() {
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