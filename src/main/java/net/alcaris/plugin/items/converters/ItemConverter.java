package net.alcaris.plugin.items.converters;

import com.google.gson.Gson;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemFoodModel;
import net.alcaris.plugin.core.model.item.ItemToolModel;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;
import net.alcaris.plugin.core.model.item.ItemArmorModel;
import net.alcaris.plugin.items.AlcarisItems;
import org.bukkit.inventory.ItemStack;

public class ItemConverter {
    private final FoodItemConverter foodItemConverter;
    private final ToolItemConverter toolItemConverter;
    private final MaterialItemConverter materialItemConverter;
    private final WeaponItemConverter weaponItemConverter;
    private final ArmorItemConverter armorItemConverter;

    private final Gson gson = AlcarisItems.getGson();

    public ItemConverter(
            FoodItemConverter foodItemConverter,
            ToolItemConverter toolItemConverter,
            MaterialItemConverter materialItemConverter,
            WeaponItemConverter weaponItemConverter,
            ArmorItemConverter armorItemConverter
    ) {
        this.foodItemConverter = foodItemConverter;
        this.toolItemConverter = toolItemConverter;
        this.materialItemConverter = materialItemConverter;
        this.weaponItemConverter = weaponItemConverter;
        this.armorItemConverter = armorItemConverter;
    }

    @SuppressWarnings("unused")
    private <T> T convertData(Object data, Class<T> clazz) {
        return gson.fromJson(gson.toJson(data), clazz);
    }

    public ItemStack convert(ItemBaseModel item, int amount) {
        return convert(item, amount, null);
    }

    public ItemStack convert(ItemBaseModel item, int amount, ItemStack oldStack) {
        ItemStack result = null;

        switch (item.getCategory()) {
            case FOOD -> {
                ItemFoodModel foodData = gson.fromJson(gson.toJson(item.getData()), ItemFoodModel.class);
                result = foodItemConverter.createItem(item, foodData, amount);
            }
            case TOOL -> {
                ItemToolModel toolData = gson.fromJson(gson.toJson(item.getData()), ItemToolModel.class);
                result = toolItemConverter.createItem(item, toolData, amount);
            }
            case WEAPON -> {
                ItemWeaponModel weaponData = gson.fromJson(gson.toJson(item.getData()), ItemWeaponModel.class);
                result = oldStack != null
                        ? weaponItemConverter.updateItem(item, weaponData, amount, oldStack)
                        : weaponItemConverter.createNewItem(item, weaponData, amount);
            }
            case ARMOR -> {
                ItemArmorModel armorData = gson.fromJson(gson.toJson(item.getData()), ItemArmorModel.class);
                result = oldStack != null
                        ? armorItemConverter.updateItem(item, armorData, amount, oldStack)
                        : armorItemConverter.createNewItem(item, armorData, amount);
            }
            case MATERIAL -> result = materialItemConverter.createItem(item, null, amount);
        }

        return result;
    }
}
