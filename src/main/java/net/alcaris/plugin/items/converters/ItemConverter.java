package net.alcaris.plugin.items.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemFoodModel;
import net.alcaris.plugin.core.model.item.ItemToolModel;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;
import org.bukkit.inventory.ItemStack;

public class ItemConverter {
    private final FoodItemConverter foodItemConverter;
    private final ToolItemConverter toolItemConverter;
    private final MaterialItemConverter materialItemConverter;
    private final WeaponItemConverter weaponItemConverter;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ItemConverter(
            FoodItemConverter foodItemConverter,
            ToolItemConverter toolItemConverter,
            MaterialItemConverter materialItemConverter, WeaponItemConverter weaponItemConverter
    ) {
        this.foodItemConverter = foodItemConverter;
        this.toolItemConverter = toolItemConverter;
        this.materialItemConverter = materialItemConverter;
        this.weaponItemConverter = weaponItemConverter;
    }

    public ItemStack convert(ItemBaseModel item, int amount) {
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
                result = weaponItemConverter.createNewItem(item, weaponData, amount);
            }
            case MATERIAL -> result = materialItemConverter.createItem(item, null, amount);
        }

        return result;
    }
}
