package net.alcaris.plugin.items.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public class ItemModel {
    private String id;
    private ItemCategory category;
    private long version;
    private String name;
    private List<String> lore;
    private int rarity;
    private int max_stack;
    private int custom_model_data;
    private Price price;
    private List<Tag> tags;
    private Object data;

    public String getId() {
        return id;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public long getVersion() {
        return version;
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

    public Price getPrice() {
        return price;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Object getData() {
        return data;
    }

    public static class Price {
        private int buy;
        private int sell;
        private boolean can_sell;

        public int getBuy() { return buy; }
        public int getSell() { return sell; }
        public boolean getCanSell() { return can_sell; }
    }

    public static class Tag {
        private String label;
        private String color;

        public String getLabel() { return label; }
        public String getColor() { return color; }
    }

    public enum ItemCategory {
        @SerializedName("food") FOOD,
        @SerializedName("tool") TOOL,
        @SerializedName("armor") ARMOR,
        @SerializedName("weapon") WEAPON,
        @SerializedName("material") MATERIAL;

        public static ItemCategory fromString(String s) {
            return ItemCategory.valueOf(s.toUpperCase());
        }
    }
}
