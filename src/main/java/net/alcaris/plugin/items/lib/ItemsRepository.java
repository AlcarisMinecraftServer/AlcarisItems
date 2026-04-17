package net.alcaris.plugin.items.lib;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.converters.WeaponItemConverter;
import net.alcaris.plugin.items.converters.ArmorItemConverter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Map;

public class ItemsRepository {
    private final AlcarisItems plugin;
    private final ItemRegistry itemRegistry;
    private final NamespacedKey itemIdKey;

    public ItemsRepository() {
        this.plugin = AlcarisItems.getInstance();
        AlcarisCore core = (AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore");

        if (core == null) throw new IllegalStateException("AlcarisCore がロードされていません");

        this.itemRegistry = core.getItemRegistry();
        this.itemIdKey = plugin.getItemIdKey();
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

    public ItemStack createItem(String id) {
        return createItem(id, 1);
    }

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

    public Optional<String> getItemIdOptional(ItemStack itemStack) {
        return Optional.ofNullable(getItemId(itemStack));
    }

    public boolean isCustomItem(ItemStack itemStack) {
        return getItemId(itemStack) != null;
    }

    public boolean rollWeaponPerformance(ItemStack itemStack, WeaponItemConverter.UpgradeType type, String specificStat) {
        WeaponItemConverter weaponItemConverter = new WeaponItemConverter(plugin);
        return weaponItemConverter.upgradeWeaponPerformance(itemStack, type, specificStat);
    }

    public boolean rollArmorPerformance(ItemStack itemStack, ArmorItemConverter.UpgradeType type, String specificStat) {
        ArmorItemConverter armorItemConverter = new ArmorItemConverter(plugin);
        return armorItemConverter.upgradeArmorPerformance(itemStack, type, specificStat);
    }

    public Map<String, Object> getWeaponStats(ItemStack itemStack) {
        WeaponItemConverter weaponItemConverter = new WeaponItemConverter(plugin);
        return weaponItemConverter.getWeaponStats(itemStack);
    }

    public Map<String, Object> getArmorStats(ItemStack itemStack) {
        ArmorItemConverter armorItemConverter = new ArmorItemConverter(plugin);
        return armorItemConverter.getArmorStats(itemStack);
    }

    public Map<String, Integer> getMaterialScores(ItemStack itemStack) {
        Map<String, Integer> scores = new LinkedHashMap<>(MaterialScoreUtils.toOrderedMap(null));

        if (itemStack == null || itemStack.getType().isAir()) {
            return scores;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return scores;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        for (String key : MaterialScoreUtils.SCORE_KEYS) {
            scores.put(key, container.getOrDefault(new NamespacedKey(plugin, key), PersistentDataType.INTEGER, 0));
        }

        return scores;
    }

    public int getMaterialScore(ItemStack itemStack, String key) {
        if (key == null || !MaterialScoreUtils.SCORE_KEYS.contains(key)) {
            return 0;
        }

        return getMaterialScores(itemStack).getOrDefault(key, 0);
    }
}
