package moe.nmkmn.alcaris_items.converters;

import moe.nmkmn.alcaris_items.AlcarisItems;
import moe.nmkmn.alcaris_items.enums.Colors;
import moe.nmkmn.alcaris_items.enums.TextureIcons;
import moe.nmkmn.alcaris_items.models.ItemModel;
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

public class MaterialItemConverter {
    private final AlcarisItems plugin;

    public MaterialItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("all")
    public ItemStack createItem(ItemModel item, int amount) {
        ItemStack itemStack = new ItemStack(Material.NAUTILUS_SHELL, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.displayName(
                    plugin.miniMessage.deserialize(item.getName())
                            .color(TextColor.fromHexString(Colors.fromRarity(item.getRarity()).getHexCode()))
                            .decoration(TextDecoration.ITALIC, false)
            );
            itemMeta.setMaxStackSize(item.getMaxStack());

            if (item.getCustomModelData() != 0) {
                itemMeta.setCustomModelData(item.getCustomModelData());
            }

            // 説明
            List<Component> lore = new ArrayList<>();
            for (String text: item.getLore()) {
                lore.add(
                        Component.text(text)
                                .color(TextColor.fromHexString("#D8D8D8"))
                                .decoration(TextDecoration.ITALIC, false)
                );
            }

            // レアリティー・カテゴリー
            lore.add(
                    Component.text("                          ")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.STRIKETHROUGH, true)
            );
            lore.add(
                    Component.text("")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(
                                    Component.text("▸ 素材")
                                            .append(
                                                    Component.text(
                                                            "【" + TextureIcons.fromRarity(item.getRarity()).getUnicode() + "】"
                                                    )
                                            )
                            )
            );

            itemMeta.lore(lore);

            // 識別タグ
            NamespacedKey keyId = new NamespacedKey(plugin, "item_id");
            NamespacedKey keyVersion = new NamespacedKey(plugin, "item_version");
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(keyId, PersistentDataType.STRING, item.getId());
            container.set(keyVersion, PersistentDataType.INTEGER, item.getVersion());

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
