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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import net.alcaris.plugin.items.gui.InsuranceApplyGUI;
import net.alcaris.plugin.items.gui.InsuranceCostTeleportGUI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ItemsRepository {
    private final AlcarisItems plugin = AlcarisItems.getInstance();
    private final ItemRegistry itemRegistry;
    private final NamespacedKey itemIdKey;
    private final NamespacedKey insuranceKey;

    public ItemsRepository() {
        AlcarisCore core = (AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore");

        if (core == null) throw new IllegalStateException("AlcarisCore がロードされていません");

        this.itemRegistry = core.getItemRegistry();
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
        this.insuranceKey = new NamespacedKey(plugin, "insurance");
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

    /**
     * アイテムに保険をつける
     * 
     * @param itemStack 対象のItemStack
     * @return 成功した場合true
     */
    public boolean addInsurance(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        
        // 保険データをPersistentDataに保存
        container.set(insuranceKey, PersistentDataType.BOOLEAN, true);
        
        // loreに保険情報を追加
        List<Component> lore = itemMeta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        // 既存の保険情報を削除
        lore.removeIf(component -> {
            String text = component.toString();
            return text.contains("このアイテムは保険にかけられています");
        });
        
        // 保険情報を追加
        lore.add(Component.text("このアイテムは保険にかけられています")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        
        return true;
    }

    /**
     * アイテムから保険を外す
     * 
     * @param itemStack 対象のItemStack
     * @return 成功した場合true
     */
    public boolean removeInsurance(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        
        // 保険データをPersistentDataから削除
        container.remove(insuranceKey);
        
        // loreから保険情報を削除
        List<Component> lore = itemMeta.lore();
        if (lore != null) {
            lore.removeIf(component -> {
                String text = component.toString();
                return text.contains("このアイテムは保険にかけられています");
            });
            
            itemMeta.lore(lore);
        }
        
        itemStack.setItemMeta(itemMeta);
        
        return true;
    }

    /**
     * アイテムに保険がかかっているかを検知する
     * 
     * @param itemStack 対象のItemStack
     * @return 保険がかかっている場合true
     */
    public boolean hasInsurance(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        Boolean insurance = container.get(insuranceKey, PersistentDataType.BOOLEAN);
        
        return insurance != null && insurance;
    }

    /**
     * 武器の性能値をロールするAPI
     * @param itemStack 対象のItemStack
     * @param type アップグレードタイプ
     * @param specificStat 特定のステータスキー（null可）
     * @return 成功した場合true
     */
    public boolean rollWeaponPerformance(ItemStack itemStack, WeaponItemConverter.UpgradeType type, String specificStat) {
        WeaponItemConverter weaponItemConverter = new WeaponItemConverter(plugin);
        return weaponItemConverter.upgradeWeaponPerformance(itemStack, type, specificStat);
    }

    /**
     * 防具の性能値をロールするAPI
     * @param itemStack 対象のItemStack
     * @param type アップグレードタイプ
     * @param specificStat 特定のステータスキー（null可）
     * @return 成功した場合true
     */
    public boolean rollArmorPerformance(ItemStack itemStack, ArmorItemConverter.UpgradeType type, String specificStat) {
        ArmorItemConverter armorItemConverter = new ArmorItemConverter(plugin);
        return armorItemConverter.upgradeArmorPerformance(itemStack, type, specificStat);
    }

    /**
     * 武器のステータス情報を取得するAPI
     * @param itemStack 対象のItemStack
     * @return ステータス情報のMap（nullの場合は無効なアイテム）
     */
    public Map<String, Object> getWeaponStats(ItemStack itemStack) {
        WeaponItemConverter weaponItemConverter = new WeaponItemConverter(plugin);
        return weaponItemConverter.getWeaponStats(itemStack);
    }

    /**
     * 防具のステータス情報を取得するAPI
     * @param itemStack 対象のItemStack
     * @return ステータス情報のMap（nullの場合は無効なアイテム）
     */
    public Map<String, Object> getArmorStats(ItemStack itemStack) {
        ArmorItemConverter armorItemConverter = new ArmorItemConverter(plugin);
        return armorItemConverter.getArmorStats(itemStack);
    }

    // GUIを開くAPI
    public void openInsuranceManageGUI(Player player) {
        new InsuranceApplyGUI(plugin, this).open(player);
    }

    /**
     * テレポート+コスト支払いGUIを開く
     * @param player 対象プレイヤー
     * @param targetLocation テレポート先
     * @param costPerItem 1アイテムあたり基本コスト
     * @param additionalCost 追加コスト
     */
    public void openInsuranceTeleportGUI(Player player, Location targetLocation, int costPerItem, int additionalCost) {
        new InsuranceCostTeleportGUI(plugin, this, targetLocation, costPerItem, additionalCost).open(player);
    }
}
