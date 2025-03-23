package net.alcaris.plugin.items.models.data;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.util.ArrayList;
import java.util.List;

public class ToolDataModel {
    private int max_damage;
    private Rules rules;
    private List<Object> upgrades;

    public int getMaxDamage() {
        return max_damage;
    }

    public Rules getRules() {
        return rules;
    }

    public List<Object> getUpgrades() {
        return upgrades;
    }

    public static class Rules {
        @SerializedName("default")
        private DefaultRule defaultRule;
        private List<Condition> conditions;

        public DefaultRule getDefaultRule() {
            return defaultRule;
        }

        public List<Condition> getConditions() {
            return conditions;
        }
    }

    public static class DefaultRule {
        private float speed;
        private int damage;

        public float getSpeed() {
            return speed;
        }

        public int getDamage() {
            return damage;
        }
    }


    public static class Condition {
        private Object blocks;
        private Float speed;
        private Boolean correct_for_drops;

        public Object getBlocks() {
            return blocks;
        }

        public Float getSpeed() {
            return speed;
        }

        public Boolean isCorrectForDrops() {
            return correct_for_drops;
        }

        @SuppressWarnings("all")
        public void applyToToolRule(ToolComponent toolComponent) {
            if (blocks instanceof String blockString) {
                if (blockString.startsWith("#")) {
                    Tag<Material> tag = convertToTag(blockString);
                    if (tag != null) {
                        toolComponent.addRule(tag, speed, correct_for_drops);
                    }
                } else {
                    Material material = convertToMaterial(blockString);
                    if (material != null) {
                        toolComponent.addRule(material, speed, correct_for_drops);
                    }
                }
            } else if (blocks instanceof List<?> blockList) {
                List<Material> materials = new ArrayList<>();
                for (Object obj : blockList) {
                    if (obj instanceof String block) {
                        Material material = convertToMaterial(block);
                        if (material != null) {
                            materials.add(material);
                        }
                    }
                }
                if (!materials.isEmpty()) {
                    toolComponent.addRule(materials, speed, correct_for_drops);
                }
            }
        }

        private Tag<Material> convertToTag(String tagName) {
            return switch (tagName) {
                case "#mineable/pickaxe" -> Tag.MINEABLE_PICKAXE;
                case "#mineable/axe" -> Tag.MINEABLE_AXE;
                case "#mineable/shovel" -> Tag.MINEABLE_SHOVEL;
                case "#mineable/hoe" -> Tag.MINEABLE_HOE;
                default -> null;
            };
        }

        private Material convertToMaterial(String blockName) {
            try {
                return Material.valueOf(blockName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
