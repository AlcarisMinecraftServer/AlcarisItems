package net.alcaris.plugin.items.lib;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.AlcarisItems;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ItemsRepository {
    private final AlcarisItems plugin = AlcarisItems.getInstance();
    private final ItemRegistry itemRegistry;

    public ItemsRepository() {
        AlcarisCore core = (AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore");

        if (core == null) throw new IllegalStateException("AlcarisCore がロードされていません");

        this.itemRegistry = core.getItemRegistry();
    }

    public ItemStack createItem(String id, int amount) {
        Optional<ItemBaseModel> opt = itemRegistry.get(id);
        if (opt.isEmpty()) {
            plugin.getLogger().warning("Unknown item id: " + id);
            return null;
        }

        return AlcarisItems.getItemConverter().convert(opt.get(), amount);
    }

    public ItemStack createItem(String id) {
        return createItem(id, 1);
    }
}
