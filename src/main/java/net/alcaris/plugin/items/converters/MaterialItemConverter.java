package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.lib.MaterialScoreUtils;
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

import java.util.List;

public class MaterialItemConverter extends BaseItemConverter<Object> {

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
    protected void setLoreItemData(List<Component> lore, ItemBaseModel item, Object data) {
        lore.add(Component.text("魔法効果:")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));

        if (!MaterialScoreUtils.hasAnyScore(data)) {
            lore.add(Component.text("  なし")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            return;
        }

        for (String key : MaterialScoreUtils.SCORE_KEYS) {
            int value = MaterialScoreUtils.getScore(data, key);
            if (value == 0) {
                continue;
            }

            lore.add(Component.text("  " + key + ": ")
                    .color(getScoreColor(key))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(value)
                            .color(TextColor.fromHexString("#D8D8D8"))
                            .decoration(TextDecoration.ITALIC, false)));
        }
    }

    @Override
    protected void setAdditionalMeta(ItemStack stack, ItemMeta meta, Object data) {
        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (String key : MaterialScoreUtils.SCORE_KEYS) {
            container.set(new NamespacedKey(plugin, key), PersistentDataType.INTEGER, MaterialScoreUtils.getScore(data, key));
        }
    }

    private TextColor getScoreColor(String key) {
        return switch (key) {
            case "red" -> TextColor.fromHexString("#FF6B6B");
            case "orange" -> TextColor.fromHexString("#FFA94D");
            case "yellow" -> TextColor.fromHexString("#FFD43B");
            case "lime" -> TextColor.fromHexString("#A9E34B");
            case "green" -> TextColor.fromHexString("#51CF66");
            case "cyan" -> TextColor.fromHexString("#3BC9DB");
            case "aqua" -> TextColor.fromHexString("#66D9E8");
            case "blue" -> TextColor.fromHexString("#4DABF7");
            case "pink" -> TextColor.fromHexString("#F783AC");
            case "purple" -> TextColor.fromHexString("#B197FC");
            case "black" -> TextColor.fromHexString("#3C3C3C");
            case "gray" -> TextColor.fromHexString("#ADB5BD");
            case "white" -> TextColor.fromHexString("#F8F9FA");
            default -> NamedTextColor.WHITE;
        };
    }
}
