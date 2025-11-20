package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MaterialItemConverter extends BaseItemConverter<Void> {

    public MaterialItemConverter(AlcarisItems plugin) {
        super(plugin);
    }

    @Override
    protected Material getMaterial(ItemBaseModel item) {
        return Material.NAUTILUS_SHELL;
    }

    @Override
    protected String getCategoryName() {
        return "素材";
    }

    @Override
    protected void setLoreItemData(List<Component> lore, ItemBaseModel item, Void data) {
        // TODO: ステータス追加予定
    }

    @Override
    protected void setAdditionalMeta(ItemStack stack, Void data) {
        // TODO: カスタムメタ追加予定
    }
}
