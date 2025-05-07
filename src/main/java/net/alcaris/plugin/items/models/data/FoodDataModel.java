package net.alcaris.plugin.items.models.data;

import java.util.List;

public class FoodDataModel {
    private int nutrition;
    private float saturation;
    private boolean can_always_eat;
    private float eat_seconds;
    private List<Effect> effects;
    private List<Attribute> attributes;
    private List<Buff> buffs;

    public int getNutrition() {
        return nutrition;
    }

    public float getSaturation() {
        return saturation;
    }

    public boolean isCanAlwaysEat() {
        return can_always_eat;
    }

    public float getEatSeconds() {
        return eat_seconds;
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Buff> getBuff() {
        return buffs;
    }

    public static class Effect {
        private String effect;
        private int duration;
        private int amplifier;
        private double chance;

        public String getEffect() {
            return effect;
        }

        public int getDuration() {
            return duration;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public double getChance() {
            return chance;
        }
    }

    public static class Attribute {
        private String attribute;
        private String operation;
        private int value;
        private int duration;

        public String getAttribute() {
            return attribute;
        }

        public String getOperation() {
            return operation;
        }

        public int getValue() {
            return value;
        }

        public int getDuration() {
            return duration;
        }
    }

    public static class Buff {
        private String kind;
        private int duration;
        private float amount;

        public String getKind() {
            return kind;
        }

        public int getDuration() {
            return duration;
        }

        public float getAmount() {
            return amount;
        }
    }
}
