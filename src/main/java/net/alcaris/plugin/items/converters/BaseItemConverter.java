package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.enums.Colors;
import net.alcaris.plugin.items.enums.TextureIcons;
import net.alcaris.plugin.items.models.ItemModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseItemConverter<T> {

    protected final AlcarisItems plugin;

    public BaseItemConverter(AlcarisItems plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItem(ItemModel item, T data, int amount) {
        ItemStack stack = new ItemStack(getMaterial(item), amount);
        ItemMeta meta = stack.getItemMeta();

        if (meta != null) {
            setDisplayName(meta, item);
            setBasicMeta(meta, item);

            List<Component> lore = new ArrayList<>();
            setLoreDescription(lore, item);
            setLoreItemData(lore, item, data);
            setLoreMetaData(lore, item);

            meta.lore(lore);
            setAdditionalMeta(meta, data);
            setIdentifiers(meta, item);

            stack.setItemMeta(meta);
        }

        return stack;
    }

    protected abstract void setAdditionalMeta(ItemMeta meta, T data);

    protected abstract Material getMaterial(ItemModel item);

    protected abstract void setLoreItemData(List<Component> lore, ItemModel item, T data);

    protected void setDisplayName(ItemMeta meta, ItemModel item) {
        meta.displayName(
                plugin.miniMessage.deserialize(item.getName())
                        .color(TextColor.fromHexString(Colors.fromRarity(item.getRarity()).getHexCode()))
                        .decoration(TextDecoration.ITALIC, false)
        );
    }

    protected void setBasicMeta(ItemMeta meta, ItemModel item) {
        meta.setMaxStackSize(item.getMaxStack());
        if (item.getCustomModelData() != 0) {
            meta.setCustomModelData(item.getCustomModelData());
        }
    }

    protected void setLoreDescription(List<Component> lore, ItemModel item) {
        for (String text : item.getLore()) {
            lore.add(Component.text(text)
                    .color(TextColor.fromHexString("#D8D8D8"))
                    .decoration(TextDecoration.ITALIC, false));
        }
    }

    protected void setLoreMetaData(List<Component> lore, ItemModel item) {
        lore.add(Component.text("                                  ")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.STRIKETHROUGH, true));
        lore.add(Component.text("")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("▸ " + getCategoryName())
                        .append(Component.text("【" + TextureIcons.fromRarity(item.getRarity()).getUnicode() + "】"))));
    }

    protected void setIdentifiers(ItemMeta meta, ItemModel item) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin, "item_id"), PersistentDataType.STRING, item.getId());
        container.set(new NamespacedKey(plugin, "item_version"), PersistentDataType.LONG, item.getVersion());
    }

    protected abstract String getCategoryName();
}
