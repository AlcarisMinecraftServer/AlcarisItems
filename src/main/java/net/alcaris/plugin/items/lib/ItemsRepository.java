package net.alcaris.plugin.items.lib;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.AlcarisItems;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ItemsRepository {
    private final AlcarisItems plugin;
    private final ItemRegistry itemRegistry;

    public ItemsRepository() {
        this.plugin = AlcarisItems.getInstance();
        AlcarisCore core = (AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore");

        if (core == null) throw new IllegalStateException("AlcarisCore がロードされていません");

        this.itemRegistry = core.getItemRegistry();
    }

    public ItemStack createItem(String id, int amount) {
        if (id == null || id.isBlank()) {
            plugin.getLogger().warning("Invalid item id: null or blank");
            return null;
        }

        if (amount <= 0) {
            plugin.getLogger().warning("Invalid amount: " + amount + " for item: " + id);
            return null;
        }

        Optional<ItemBaseModel> opt = itemRegistry.get(id);
        if (opt.isEmpty()) {
            plugin.getLogger().warning("Unknown item id: " + id);
            return null;
        }

        ItemStack result = AlcarisItems.getItemConverter().convert(opt.get(), amount);
        if (result == null || result.getType().isAir()) {
            plugin.getLogger().warning("Failed to convert item: " + id);
            return null;
        }

        return result;
    }

    @SuppressWarnings("unused")
    public ItemStack createItem(String id) {
        return createItem(id, 1);
    }
}
