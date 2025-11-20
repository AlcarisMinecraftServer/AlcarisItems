package net.alcaris.plugin.items.converters;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.keys.SoundEventKeys;
import net.alcaris.plugin.core.model.item.Common;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemFoodModel;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.enums.EffectInfo;
import net.alcaris.plugin.items.enums.TextureIcons;
import net.alcaris.plugin.items.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;

import java.util.List;
import java.util.Locale;

public class FoodItemConverter extends BaseItemConverter<ItemFoodModel> {
    public FoodItemConverter(AlcarisItems plugin) {
        super(plugin);
    }

    @Override
    protected Material getMaterial(ItemBaseModel item) {
        return Material.TROPICAL_FISH;
    }

    @Override
    protected String getCategoryName() {
        return "食料";
    }

    @Override
    protected void setLoreItemData(List<Component> lore, ItemBaseModel item, ItemFoodModel food) {
        lore.add(
                Component.text("")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.STRIKETHROUGH, true)
                        .append(
                                Component.text("             ")
                                        .append(
                                                Component.text("\uF822消費時\uF822")
                                                        .color(TextColor.fromHexString("#777777"))
                                                        .decoration(TextDecoration.STRIKETHROUGH, false)
                                                        .append(
                                                                Component.text("             ")
                                                                        .decoration(TextDecoration.STRIKETHROUGH, true)
                                                        )
                                        )
                        )
        );

        // region 満腹度
        int nutrition = food.getNutrition();
        int full = nutrition / 2;
        boolean hasHalf = nutrition % 2 != 0;

        StringBuilder result = new StringBuilder();
        result.append(String.valueOf(TextureIcons.HUNGER_10.getUnicode()).repeat(full));
        if (hasHalf) result.append(TextureIcons.HUNGER_05.getUnicode());

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
        if (food.getEffects() != null && !food.getEffects().isEmpty()) {
            for (Common.Effect effect : food.getEffects()) {
                String id = effect.getEffect();
                EffectInfo.Info info = EffectInfo.get(id);

                int seconds = effect.getDuration();
                int amplifier = effect.getAmplifier() + 1;
                String timeString = String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);

                lore.add(
                        Component.text("   \uF803").color(NamedTextColor.GRAY).append(
                                Component.text(info.nameJa + " ").color(info.color)
                                        .decoration(TextDecoration.ITALIC, false)
                                        .append(Component.text(TextUtils.toRomanNumeral(amplifier)))
                                        .append(Component.text("（" + timeString + "）"))
                        )
                );
            }
        }
        // endregion

        // region バフ
        // TODO: あとで
        // endregion
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void setAdditionalMeta(ItemStack stack, ItemFoodModel food) {
        ItemMeta meta = stack.getItemMeta();
        FoodComponent foodComponent = meta.getFood();
        foodComponent.setNutrition(food.getNutrition());
        foodComponent.setSaturation(food.getSaturation());
        foodComponent.setCanAlwaysEat(food.isCanAlwaysEat());
        meta.setFood(foodComponent);

        Consumable.Builder consumable = Consumable.consumable()
                .consumeSeconds(food.getEatSeconds())
                .animation(ItemUseAnimation.EAT)
                .sound(SoundEventKeys.ENTITY_GENERIC_EAT);
        stack.setData(DataComponentTypes.CONSUMABLE, consumable);
    }
}
