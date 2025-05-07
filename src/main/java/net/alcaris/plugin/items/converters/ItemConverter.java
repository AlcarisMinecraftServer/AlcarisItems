package net.alcaris.plugin.items.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alcaris.plugin.items.models.ItemModel;
import net.alcaris.plugin.items.models.data.FoodDataModel;
import net.alcaris.plugin.items.models.data.ToolDataModel;
import net.alcaris.plugin.items.models.data.WeaponDataModel;
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

    public ItemStack convert(ItemModel item, int amount) {
        ItemStack result = null;

        switch (item.getCategory()) {
            case ItemModel.ItemCategory.FOOD -> {
                FoodDataModel foodData = gson.fromJson(gson.toJson(item.getData()), FoodDataModel.class);
                result = foodItemConverter.createItem(item, foodData, amount);
            }
            case ItemModel.ItemCategory.TOOL -> {
                ToolDataModel toolData = gson.fromJson(gson.toJson(item.getData()), ToolDataModel.class);
                result = toolItemConverter.createItem(item, toolData, amount);
            }
            case ItemModel.ItemCategory.WEAPON -> {
                WeaponDataModel weaponData = gson.fromJson(gson.toJson(item.getData()), WeaponDataModel.class);
                result = weaponItemConverter.createNewItem(item, weaponData, amount);
            }
            case ItemModel.ItemCategory.MATERIAL -> result = materialItemConverter.createItem(item, null, amount);
        }

        return result;
    }
}
