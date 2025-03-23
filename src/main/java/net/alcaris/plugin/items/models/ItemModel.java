package net.alcaris.plugin.items.models;

import java.util.List;

public class ItemModel {
    private String id;
    private int version;
    private String category;
    private String name;
    private List<String> lore;
    private int rarity;
    private int max_stack;
    private int custom_model_data;
    private Price price;
    private Object data;

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getRarity() {
        return rarity;
    }

    public int getMaxStack() {
        return max_stack;
    }

    public int getCustomModelData() {
        return custom_model_data;
    }

    public Object getData() {
        return data;
    }

    public static class Price {
        private int buy;
        private int sell;
        private boolean can_sell;

        public int getBuy() {
            return buy;
        }

        public int getSell() {
            return sell;
        }

        public boolean isCan_sell() {
            return can_sell;
        }
    }
}
