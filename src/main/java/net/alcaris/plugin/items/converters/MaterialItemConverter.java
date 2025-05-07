package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.enums.TextureIcons;
import net.alcaris.plugin.items.models.ItemModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MaterialItemConverter extends BaseItemConverter<Void> {

    public MaterialItemConverter(AlcarisItems plugin) {
        super(plugin);
    }

    @Override
    protected Material getMaterial(ItemModel item) {
        return Material.NAUTILUS_SHELL;
    }

    @Override
    protected String getCategoryName() {
        return "素材";
    }

    @Override
    protected void setLoreItemData(List<Component> lore, ItemModel item, Void data) {
        // TODO: ステータス追加予定
    }

    @Override
    protected void setAdditionalMeta(ItemMeta meta, Void data) {
        // TODO: カスタムメタ追加予定
    }
}
