package moe.nmkmn.alcaris_items.converters;

import moe.nmkmn.alcaris_items.AlcarisItems;
import moe.nmkmn.alcaris_items.models.ItemModel;
import moe.nmkmn.alcaris_items.models.data.ToolDataModel;
import moe.nmkmn.alcaris_items.enums.Colors;
import moe.nmkmn.alcaris_items.enums.TextureIcons;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ToolItemConverter {
    private final AlcarisItems plugin;

    public ToolItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("all")
    public ItemStack createItem(ItemModel item, ToolDataModel tool, int amount) {
        ItemStack itemStack = new ItemStack(Material.WOODEN_PICKAXE, amount);
        Damageable itemMeta = (Damageable) itemStack.getItemMeta();

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
                                    Component.text("▸ ツール")
                                            .append(
                                                    Component.text(
                                                            "【" + TextureIcons.fromRarity(item.getRarity()).getUnicode() + "】"
                                                    )
                                            )
                            )
            );

            itemMeta.lore(lore);

            // アイテムのメタデータ
            itemMeta.setMaxDamage(tool.getMaxDamage());

            ToolComponent toolComponent = itemMeta.getTool();
            toolComponent.setRules(new ArrayList<>());
            toolComponent.setDefaultMiningSpeed(tool.getRules().getDefaultRule().getSpeed());
            toolComponent.setDamagePerBlock(tool.getRules().getDefaultRule().getDamage());

            for (ToolDataModel.Condition condition : tool.getRules().getConditions()) {
                condition.applyToToolRule(toolComponent);
            }

            // 識別タグ
            NamespacedKey keyId = new NamespacedKey(plugin, "item_id");
            NamespacedKey keyVersion = new NamespacedKey(plugin, "item_version");
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(keyId, PersistentDataType.STRING, item.getId());
            container.set(keyVersion, PersistentDataType.INTEGER, item.getVersion());

            itemMeta.setTool(toolComponent);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
