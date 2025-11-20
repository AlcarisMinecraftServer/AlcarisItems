package net.alcaris.plugin.items.lib;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.converters.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;
import org.bukkit.Material;

public class InventoryUpdater {
    private final AlcarisItems plugin;
    private final ItemRegistry itemRegistry;
    private final ItemConverter itemConverter;

    private final NamespacedKey keyId;
    private final NamespacedKey keyVersion;

    public InventoryUpdater(AlcarisItems plugin, ItemConverter itemConverter) {
        this.plugin = plugin;
        this.itemConverter = itemConverter;
        this.keyId = plugin.getItemIdKey();
        this.keyVersion = plugin.getItemVersionKey();

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
                // If config enables replacement and item is vanilla armor, replace with custom item
                if (plugin.getConfig().getBoolean("replaceVanillaArmor", false) && isVanillaArmor(item.getType())) {
                    String vanillaId = "vanilla_" + item.getType().name().toLowerCase();
                    Optional<ItemBaseModel> vanillaModel = itemRegistry.get(vanillaId);
                    if (vanillaModel.isPresent()) {
                        ItemStack newItem = AlcarisItems.getItemConverter().convert(vanillaModel.get(), item.getAmount());
                        newItem = copyDamage(newItem, item);
                        inv.setItem(slot, newItem);
                        plugin.getLogger().info("プレイヤー " + player.getName() + " のバニラ防具 " + item.getType().name() + " を " + vanillaId + " に置き換えました。");
                    }
                }
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

            if (plugin.getLogger().isLoggable(java.util.logging.Level.FINE)) {
                plugin.getLogger().fine(
                        "プレイヤー " + player.getName() +
                                " のアイテム (" + itemId + ") をバージョン " + oldVer +
                                " → " + newVer + " に更新しました。"
                );
            }
        }
    }

    private ItemStack createUpdatedStack(ItemBaseModel model, ItemStack oldStack) {
        ItemStack newStack = itemConverter.convert(model, oldStack.getAmount(), oldStack);
        return copyDamage(newStack, oldStack);
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

    private boolean isVanillaArmor(Material material) {
        return material.name().endsWith("_HELMET") ||
               material.name().endsWith("_CHESTPLATE") ||
               material.name().endsWith("_LEGGINGS") ||
               material.name().endsWith("_BOOTS");
    }
    
    public void updateAllPlayersItems() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerItems(player);
        }
    }
}
