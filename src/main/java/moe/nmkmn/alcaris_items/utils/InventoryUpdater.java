package moe.nmkmn.alcaris_items.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.nmkmn.alcaris_items.AlcarisItems;
import moe.nmkmn.alcaris_items.converters.FoodItemConverter;
import moe.nmkmn.alcaris_items.converters.ToolItemConverter;
import moe.nmkmn.alcaris_items.models.ItemModel;
import moe.nmkmn.alcaris_items.models.data.FoodDataModel;
import moe.nmkmn.alcaris_items.models.data.ToolDataModel;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class InventoryUpdater {
    private final AlcarisItems plugin;
    private final FoodItemConverter foodConverter;
    private final ToolItemConverter toolConverter;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final NamespacedKey keyId;
    private final NamespacedKey keyVersion;

    public InventoryUpdater(AlcarisItems plugin, FoodItemConverter foodConverter, ToolItemConverter toolConverter) {
        this.plugin = plugin;
        this.foodConverter = foodConverter;
        this.toolConverter = toolConverter;
        this.keyId = new NamespacedKey(plugin, "item_id");
        this.keyVersion = new NamespacedKey(plugin, "item_version");
    }

    public void updatePlayerItems(Player player) {
        Inventory inv = player.getInventory();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(keyId, PersistentDataType.STRING) || !container.has(keyVersion, PersistentDataType.INTEGER)) {
                continue;
            }
            String itemId = container.get(keyId, PersistentDataType.STRING);
            Integer storedVersion = container.get(keyVersion, PersistentDataType.INTEGER);
            if (itemId == null || storedVersion == null) continue;

            File cacheFile = new File(plugin.getDataFolder(), "caches" + File.separator + itemId + ".json");
            if (!cacheFile.exists()) continue;

            try (FileReader reader = new FileReader(cacheFile)) {
                ItemModel model = gson.fromJson(reader, ItemModel.class);
                int currentVersion = model.getVersion();

                if (!storedVersion.equals(currentVersion)) {
                    ItemStack newItem = null;
                    if ("food".equals(model.getCategory())) {
                        FoodDataModel foodData = gson.fromJson(gson.toJson(model.getData()), FoodDataModel.class);
                        newItem = foodConverter.createFoodItem(model, foodData, item.getAmount());
                    } else if ("tool".equals(model.getCategory())) {
                        ToolDataModel toolData = gson.fromJson(gson.toJson(model.getData()), ToolDataModel.class);
                        newItem = updateToolItem(model, toolData, item);
                    }

                    if (newItem != null) {
                        inv.setItem(slot, newItem);
                        plugin.getLogger().info("プレイヤー " + player.getName() + " のインベントリ内のアイテム (" + itemId + ") を更新しました。");
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().severe("インベントリ内アイテム更新中にエラーが発生: " + e.getMessage());
            }
        }
    }

    private ItemStack updateToolItem(ItemModel model, ToolDataModel toolData, ItemStack oldItem) {
        ItemStack newItem = toolConverter.createToolItem(model, toolData, oldItem.getAmount());

        if (newItem.getItemMeta() instanceof Damageable newMeta && oldItem.getItemMeta() instanceof Damageable oldMeta) {
            newMeta.setDamage(oldMeta.getDamage());
            newItem.setItemMeta(newMeta);
        }

        return newItem;
    }

    public void updateAllPlayersItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerItems(player);
        }
    }
}
