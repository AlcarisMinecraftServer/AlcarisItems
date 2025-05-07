package net.alcaris.plugin.items.converters;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.models.data.FoodDataModel;
import net.alcaris.plugin.items.models.ItemModel;
import net.alcaris.plugin.items.enums.TextureIcons;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;

import java.util.List;

public class FoodItemConverter extends  BaseItemConverter<FoodDataModel> {
    public FoodItemConverter(AlcarisItems plugin) {
        super(plugin);
    }

    @Override
    protected Material getMaterial(ItemModel item) {
        return Material.TROPICAL_FISH;
    }

    @Override
    protected String getCategoryName() {
        return "食料";
    }

    @Override
    protected void setLoreItemData(List<Component> lore, ItemModel item, FoodDataModel food) {
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

        // region 満腹度
        StringBuilder result = new StringBuilder();
        int nutrition = food.getNutrition();
        if (nutrition % 2 == 0) {
            result.append(String.valueOf(TextureIcons.HUNGER_10.getUnicode()).repeat(Math.max(0, nutrition / 2)));
        } else {
            result.append(TextureIcons.HUNGER_05.getUnicode());
            result.append(String.valueOf(TextureIcons.HUNGER_10.getUnicode()).repeat(Math.max(0, (nutrition - 1) / 2)));
        }

        lore.add(
                Component.text("▸  \uF803")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text("満腹度回復: ")
                                        .color(TextColor.fromHexString("E0E0E0"))
                                        .append(Component.text(result + "\uF802"))
                        )
        );
        // endregion

        // region エフェクト
        // TODO: あとで
        // endregion

        // region バフ
        // TODO: あとで
        // endregion
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void setAdditionalMeta(ItemMeta meta, FoodDataModel food) {
        FoodComponent foodComponent = meta.getFood();
        foodComponent.setNutrition(food.getNutrition());
        foodComponent.setSaturation(food.getSaturation());
        foodComponent.setCanAlwaysEat(food.isCanAlwaysEat());
        foodComponent.setEatSeconds(food.getEatSeconds());
        meta.setFood(foodComponent);
    }
}
