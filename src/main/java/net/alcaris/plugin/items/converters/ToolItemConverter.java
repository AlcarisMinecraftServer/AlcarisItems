package net.alcaris.plugin.items.converters;

import com.google.gson.Gson;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemToolModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;

import java.util.ArrayList;
import java.util.List;

public class ToolItemConverter extends BaseItemConverter<ItemToolModel> {
    private final Gson gson = AlcarisItems.getGson();

    public ToolItemConverter(AlcarisItems plugin) {
        super(plugin);
    }

    @Override
    protected Material getMaterial(ItemBaseModel item) {
        ItemToolModel toolData = gson.fromJson(gson.toJson(item.getData()), ItemToolModel.class);
        ItemToolModel.ToolType toolType = toolData.getToolType();

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
    protected void setLoreItemData(List<Component> lore, ItemBaseModel item, ItemToolModel tool) {

    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void setAdditionalMeta(ItemStack stack, ItemMeta meta, ItemToolModel tool) {
        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(tool.getMaxDamage());
        }

        ToolComponent toolComponent = meta.getTool();
        toolComponent.setRules(new ArrayList<>());
        toolComponent.setDefaultMiningSpeed(tool.getRules().getDefaultRule().getSpeed());
        toolComponent.setDamagePerBlock(tool.getRules().getDefaultRule().getDamage());

        for (ItemToolModel.Condition condition : tool.getRules().getConditions()) {
            condition.applyToToolRule(toolComponent);
        }

        meta.setTool(toolComponent);
    }
}
