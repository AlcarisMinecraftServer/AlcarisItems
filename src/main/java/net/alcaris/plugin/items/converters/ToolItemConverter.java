package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.models.ItemModel;
import net.alcaris.plugin.items.models.data.ToolDataModel;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.util.ArrayList;
import java.util.List;

public class ToolItemConverter extends BaseItemConverter<ToolDataModel> {
    public ToolItemConverter(AlcarisItems plugin) {
        super(plugin);
    }

    @Override
    protected Material getMaterial(ItemModel item) {
        // TODO: あとでツールデータから
        String toolType = "pickaxe";

        return switch (toolType) {
            case "sword" -> Material.WOODEN_SWORD;
            case "pickaxe" -> Material.WOODEN_PICKAXE;
            case "axe" -> Material.WOODEN_AXE;
            case "shovel" -> Material.WOODEN_SHOVEL;
            case "hoe" -> Material.WOODEN_HOE;
            default -> Material.STICK;
        };
    }

    @Override
    protected String getCategoryName() {
        return "ツール";
    }

    @Override
    protected void setLoreItemData(List<Component> lore, ItemModel item, ToolDataModel tool) {
        // TODO: ツールのステータスを実装（耐久値、採掘可能）
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void setAdditionalMeta(ItemMeta meta, ToolDataModel tool) {
        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(tool.getMaxDamage());
            ToolComponent toolComponent = damageable.getTool();
            toolComponent.setRules(new ArrayList<>());
            toolComponent.setDefaultMiningSpeed(tool.getRules().getDefaultRule().getSpeed());
            toolComponent.setDamagePerBlock(tool.getRules().getDefaultRule().getDamage());

            for (ToolDataModel.Condition condition : tool.getRules().getConditions()) {
                condition.applyToToolRule(toolComponent);
            }

            damageable.setTool(toolComponent);
        }
    }
}
