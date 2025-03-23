package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.models.data.FoodDataModel;
import net.alcaris.plugin.items.models.ItemModel;
import net.alcaris.plugin.items.enums.Colors;
import net.alcaris.plugin.items.enums.TextureIcons;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class FoodItemConverter {
    private final AlcarisItems plugin;

    public FoodItemConverter(final AlcarisItems plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("all")
    public ItemStack createItem(ItemModel item, FoodDataModel food, int amount) {
        ItemStack itemStack = new ItemStack(Material.TROPICAL_FISH, amount);
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

            // ステータス
            lore.add(
                    Component.text("")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                            .decoration(TextDecoration.STRIKETHROUGH, true)
                            .append(
                                    Component.text("         ")
                                            .append(
                                                    Component.text("\uF822消費時\uF822")
                                                            .color(TextColor.fromHexString("#777777"))
                                                            .decoration(TextDecoration.STRIKETHROUGH, false)
                                                            .append(
                                                                    Component.text("         ")
                                                                            .decoration(TextDecoration.STRIKETHROUGH, true)
                                                            )
                                            )
                            )
            );

            StringBuilder result = new StringBuilder();

            // 満腹度計算
            int nutrition = food.getNutrition();
            if (nutrition % 2 == 0) {
                int pairs = nutrition / 2;
                for (int i = 0; i < pairs; i++) {
                    result.append(TextureIcons.HUNGER_10.getUnicode());
                }
            } else {
                result.append(TextureIcons.HUNGER_05.getUnicode());
                int pairs = (nutrition - 1) / 2;
                for (int i = 0; i < pairs; i++) {
                    result.append(TextureIcons.HUNGER_10.getUnicode());
                }
            }

//            for (Component effect_lore : effect.lore) {
//                lore.add(effect_lore);
//            }

            lore.add(
                    Component.text("▸  \uF803")
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(
                                    Component.text("満腹度回復: ")
                                            .color(TextColor.fromHexString("E0E0E0"))
                                            .append(
                                                    Component.text(result + "\uF802")
                                            )
                            )
            );

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
                                    Component.text("▸ 食料")
                                            .append(
                                                    Component.text(
                                                            "【" + TextureIcons.fromRarity(item.getRarity()).getUnicode() + "】"
                                                    )
                                            )
                            )
            );

            itemMeta.lore(lore);

            // アイテムのメタデータ
            FoodComponent foodComponent = itemMeta.getFood();
            foodComponent.setNutrition(food.getNutrition());
            foodComponent.setSaturation(food.getSaturation());
            foodComponent.setCanAlwaysEat(food.isCanAlwaysEat());
            foodComponent.setEatSeconds(food.getEatSeconds());

            // 識別タグ
            NamespacedKey keyId = new NamespacedKey(plugin, "item_id");
            NamespacedKey keyVersion = new NamespacedKey(plugin, "item_version");
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(keyId, PersistentDataType.STRING, item.getId());
            container.set(keyVersion, PersistentDataType.INTEGER, item.getVersion());

            itemMeta.setFood(foodComponent);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
