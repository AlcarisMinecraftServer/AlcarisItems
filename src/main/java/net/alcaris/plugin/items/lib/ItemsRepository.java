package net.alcaris.plugin.items.lib;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.AlcarisItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class ItemsRepository {
    private final AlcarisItems plugin = AlcarisItems.getInstance();
    private final ItemRegistry itemRegistry;
    private final NamespacedKey itemIdKey;

    public ItemsRepository() {
        AlcarisCore core = (AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore");

        if (core == null) throw new IllegalStateException("AlcarisCore がロードされていません");

        this.itemRegistry = core.getItemRegistry();
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
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

    /**
     * アイテムスタックからアイテムIDを取得する
     * 
     * @param itemStack アイテムスタック
     * @return アイテムID（取得できない場合はnull）
     */
    public String getItemId(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return container.get(itemIdKey, PersistentDataType.STRING);
    }

    /**
     * アイテムスタックからアイテムIDを取得する（Optional版）
     * 
     * @param itemStack アイテムスタック
     * @return アイテムIDのOptional
     */
    public Optional<String> getItemIdOptional(ItemStack itemStack) {
        String itemId = getItemId(itemStack);
        return Optional.ofNullable(itemId);
    }

    /**
     * アイテムスタックがカスタムアイテムかどうかを判定する
     * 
     * @param itemStack アイテムスタック
     * @return カスタムアイテムの場合はtrue
     */
    public boolean isCustomItem(ItemStack itemStack) {
        return getItemId(itemStack) != null;
    }
}
