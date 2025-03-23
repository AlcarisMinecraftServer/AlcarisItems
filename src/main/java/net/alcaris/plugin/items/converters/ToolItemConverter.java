package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.models.ItemModel;
import net.alcaris.plugin.items.models.data.ToolDataModel;
import net.alcaris.plugin.items.enums.Colors;
import net.alcaris.plugin.items.enums.TextureIcons;
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

    private Material getToolMaterial(String itemId) {
        String toolId = itemId.substring(itemId.lastIndexOf("_"));

        switch (toolId) {
            case "_pickaxe" -> {
                return Material.WOODEN_PICKAXE;
            }
            case "_axe" -> {
                return Material.WOODEN_AXE;
            }
            case "_shovel" -> {
                return Material.WOODEN_SHOVEL;
            }
            case "_hoe" -> {
                return Material.WOODEN_HOE;
            }
        }

        return Material.STONE;
    }

    @SuppressWarnings("all")
    public ItemStack createItem(ItemModel item, ToolDataModel tool, int amount) {
        ItemStack itemStack = new ItemStack(getToolMaterial(item.getId()), amount);
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
