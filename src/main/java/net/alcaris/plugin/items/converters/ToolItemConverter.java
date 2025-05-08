package net.alcaris.plugin.items.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ToolItemConverter(AlcarisItems plugin) {
        super(plugin);
    }

    @Override
    protected Material getMaterial(ItemModel item) {
        ToolDataModel toolData = gson.fromJson(gson.toJson(item.getData()), ToolDataModel.class);
        ToolDataModel.ToolType toolType = toolData.getToolType();

        return switch (toolType) {
            case SWORD -> Material.WOODEN_SWORD;
            case PICKAXE -> Material.WOODEN_PICKAXE;
            case AXE -> Material.WOODEN_AXE;
            case SHOVEL -> Material.WOODEN_SHOVEL;
            case HOE -> Material.WOODEN_HOE;
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
        }

        ToolComponent toolComponent = meta.getTool();
        toolComponent.setRules(new ArrayList<>());
        toolComponent.setDefaultMiningSpeed(tool.getRules().getDefaultRule().getSpeed());
        toolComponent.setDamagePerBlock(tool.getRules().getDefaultRule().getDamage());

        for (ToolDataModel.Condition condition : tool.getRules().getConditions()) {
            condition.applyToToolRule(toolComponent);
        }

        meta.setTool(toolComponent);
    }
}
