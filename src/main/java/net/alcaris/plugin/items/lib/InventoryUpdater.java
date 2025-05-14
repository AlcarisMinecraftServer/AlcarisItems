package net.alcaris.plugin.items.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.model.item.ItemFoodModel;
import net.alcaris.plugin.core.model.item.ItemToolModel;
import net.alcaris.plugin.core.model.item.ItemWeaponModel;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.converters.FoodItemConverter;
import net.alcaris.plugin.items.converters.MaterialItemConverter;
import net.alcaris.plugin.items.converters.ToolItemConverter;
import net.alcaris.plugin.items.converters.WeaponItemConverter;
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
import java.util.Objects;
import java.util.Optional;

public class InventoryUpdater {

    private final AlcarisItems plugin;
    private final ItemRegistry itemRegistry;
    private final FoodItemConverter foodConverter;
    private final ToolItemConverter toolConverter;
    private final MaterialItemConverter materialConverter;
    private final WeaponItemConverter weaponConverter;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final NamespacedKey keyId;
    private final NamespacedKey keyVersion;

    public InventoryUpdater(
            AlcarisItems plugin,
            FoodItemConverter foodConverter,
            ToolItemConverter toolConverter,
            MaterialItemConverter materialConverter,
            WeaponItemConverter weaponItemConverter
    ) {
        this.plugin = plugin;
        this.foodConverter = foodConverter;
        this.toolConverter = toolConverter;
        this.materialConverter = materialConverter;
        this.weaponConverter = weaponItemConverter;

        this.keyId = new NamespacedKey(plugin, "item_id");
        this.keyVersion = new NamespacedKey(plugin, "item_version");

        this.itemRegistry = ((AlcarisCore) Objects.requireNonNull(
                Bukkit.getPluginManager().getPlugin("AlcarisCore"),
                "AlcarisCore がロードされていません"
        )).getItemRegistry();
    }

    public void updatePlayerItems(Player player) {
        Inventory inv = player.getInventory();

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (!pdc.has(keyId, PersistentDataType.STRING) ||
                    !pdc.has(keyVersion, PersistentDataType.LONG)) {
                continue;
            }

            String itemId   = pdc.get(keyId, PersistentDataType.STRING);
            Long   oldVer   = pdc.get(keyVersion, PersistentDataType.LONG);
            if (itemId == null || oldVer == null) continue;

            Optional<ItemBaseModel> opt = itemRegistry.get(itemId);
            if (opt.isEmpty()) continue;

            ItemBaseModel model = opt.get();
            long newVer = model.getVersion();
            if (oldVer == newVer) continue;

            ItemStack newItem = createUpdatedStack(model, item);
            if (newItem == null) continue;

            inv.setItem(slot, newItem);
            plugin.getLogger().info(
                    "プレイヤー " + player.getName() +
                            " のアイテム (" + itemId + ") をバージョン " + oldVer +
                            " → " + newVer + " に更新しました。"
            );
        }
    }

    private ItemStack createUpdatedStack(ItemBaseModel model, ItemStack oldStack) {
        return switch (model.getCategory()) {
            case FOOD -> {
                ItemFoodModel food = gson.fromJson(
                        gson.toJson(model.getData()), ItemFoodModel.class);
                yield foodConverter.createItem(model, food, oldStack.getAmount());
            }
            case TOOL -> {
                ItemToolModel tool = gson.fromJson(
                        gson.toJson(model.getData()), ItemToolModel.class);
                yield copyDamage(toolConverter.createItem(model, tool, oldStack.getAmount()), oldStack);
            }
            case ARMOR -> null;
            case WEAPON -> {
                ItemWeaponModel weapon = gson.fromJson(
                        gson.toJson(model.getData()), ItemWeaponModel.class);
                yield copyDamage(weaponConverter.updateItem(model, weapon, oldStack.getAmount(), oldStack), oldStack);
            }
            case MATERIAL -> materialConverter.createItem(model, null, oldStack.getAmount());
        };
    }

    private ItemStack copyDamage(ItemStack newStack, ItemStack oldStack) {
        if (newStack == null) return null;

        if (newStack.getItemMeta() instanceof Damageable newMeta &&
                oldStack.getItemMeta() instanceof Damageable oldMeta) {

            newMeta.setDamage(oldMeta.getDamage());
            newStack.setItemMeta(newMeta);
        }
        return newStack;
    }
    
    public void updateAllPlayersItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerItems(player);
        }
    }
}
