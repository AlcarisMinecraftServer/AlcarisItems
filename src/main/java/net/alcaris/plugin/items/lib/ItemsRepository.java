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
import net.alcaris.plugin.items.enums.InsuranceType;
import java.util.concurrent.ThreadLocalRandom;

public class ItemsRepository {
    private final AlcarisItems plugin = AlcarisItems.getInstance();
    private final ItemRegistry itemRegistry;
    private final NamespacedKey itemIdKey;
    private final NamespacedKey insuranceKey;
    private final NamespacedKey insuranceTypeKey;

    public ItemsRepository() {
        AlcarisCore core = (AlcarisCore) Bukkit.getPluginManager().getPlugin("AlcarisCore");

        if (core == null) throw new IllegalStateException("AlcarisCore がロードされていません");

        this.itemRegistry = core.getItemRegistry();
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
        this.insuranceKey = new NamespacedKey(plugin, "insurance");
        this.insuranceTypeKey = new NamespacedKey(plugin, "insurance_type");
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
        return addInsurance(itemStack, InsuranceType.STANDARD);
    }

    public boolean addInsurance(ItemStack itemStack, InsuranceType type) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(insuranceKey, PersistentDataType.BOOLEAN, true);
        container.set(insuranceTypeKey, PersistentDataType.STRING, type.name());
        List<Component> lore = itemMeta.lore();
        if (lore == null) lore = new ArrayList<>();
        lore.removeIf(c -> c.toString().contains("保険"));
        lore.add(Component.text("このアイテムは" + type.getDisplayName() + "に加入しています")
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
        container.remove(insuranceTypeKey);
        
        // loreから保険情報を削除
        List<Component> lore = itemMeta.lore();
        if (lore != null) {
            lore.removeIf(component -> {
                String text = component.toString();
                return text.contains("このアイテムは保険にかけられています") || text.contains("このアイテムは");
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

    public InsuranceType getInsuranceType(ItemStack itemStack) {
        if (!hasInsurance(itemStack)) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return InsuranceType.STANDARD;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String typeName = container.get(insuranceTypeKey, PersistentDataType.STRING);
        if (typeName == null) {
            return InsuranceType.STANDARD;
        }
        try {
            return InsuranceType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return InsuranceType.STANDARD;
        }
    }

    public void applyItemLoss(Player player) {
        if (player == null) return;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType().isAir()) continue;
            if (!hasInsurance(stack)) {
                player.getInventory().setItem(i, null);
                continue;
            }
            InsuranceType type = getInsuranceType(stack);
            if (type == null) type = InsuranceType.STANDARD;
            double chance = type.getLossProbability();
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                player.getInventory().setItem(i, null);
            }
        }
        player.updateInventory();
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

    // -----------------------
    // 売却関連メソッド
    // -----------------------
    /**
     * アイテム1スタックあたりの売却額を取得します。
     * getPrice() の sell フィールドを参照し、sellable でない場合は 0 を返します。
     */
    public int getSellPrice(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) return 0;
        String itemId = getItemId(itemStack);
        if (itemId == null) return 0;
        Optional<ItemBaseModel> optModel = itemRegistry.get(itemId);
        if (optModel.isEmpty()) return 0;
        ItemBaseModel model = optModel.get();
        if (model.getPrice() == null || !model.getPrice().getCanSell()) return 0;
        return model.getPrice().getSell();
    }

    /**
     * 対象アイテムが売却可能か判定します。
     */
    public boolean canSell(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) return false;
        String itemId = getItemId(itemStack);
        if (itemId == null) return false;
        Optional<ItemBaseModel> optModel = itemRegistry.get(itemId);
        if (optModel.isEmpty()) return false;
        ItemBaseModel model = optModel.get();
        return model.getPrice() != null && model.getPrice().getCanSell();
    }

    /**
     * 売却GUIを開くAPI
     */
    public void openItemSellGUI(Player player) {
        new net.alcaris.plugin.items.gui.SellItemGUI(plugin, this).open(player);
    }
}
