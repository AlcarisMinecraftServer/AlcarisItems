package net.alcaris.plugin.items.gui;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.lib.ItemsRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI 1: プレイヤーが複数アイテムを入れ、すべてに保険を付与または解除できるGUI。
 */
public class InsuranceApplyGUI implements Listener {

    private final AlcarisItems plugin;
    private final ItemsRepository repository;
    private final Inventory inventory;

    private static final int INVENTORY_SIZE = 54; // 6行 * 9列
    private static final int BUTTON_APPLY_SLOT = 48; // 緑のガラス: 保険をつける
    private static final int BUTTON_REMOVE_SLOT = 50; // 赤のガラス: 保険を外す
    private static final int MAX_DEPOSIT_SLOT = 45; // 0-44がアイテムエリア

    public InsuranceApplyGUI(AlcarisItems plugin, ItemsRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        this.inventory = Bukkit.createInventory(null, INVENTORY_SIZE, Component.text("保険管理", NamedTextColor.GOLD));

        // ボタンをセットアップ
        inventory.setItem(BUTTON_APPLY_SLOT, createButton(Material.LIME_STAINED_GLASS_PANE, "全てに保険をつける"));
        inventory.setItem(BUTTON_REMOVE_SLOT, createButton(Material.RED_STAINED_GLASS_PANE, "全ての保険を外す"));

        // イベント登録
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, plugin);
    }

    private ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    // InventoryHolder を実装しなくなったため getInventory は不要

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        int rawSlot = event.getRawSlot();

        // クリックしたインベントリが GUI (上段) かどうか
        boolean clickedTop = event.getClickedInventory() != null && event.getClickedInventory().equals(inventory);

        if (clickedTop) {
            // ボタンエリアはキャンセル
            if (rawSlot == BUTTON_APPLY_SLOT) {
                event.setCancelled(true);
                applyInsuranceToAll((Player) event.getWhoClicked());
                return;
            }
            if (rawSlot == BUTTON_REMOVE_SLOT) {
                event.setCancelled(true);
                removeInsuranceFromAll((Player) event.getWhoClicked());
                return;
            }

            // アイテムエリア (0-44) はキャンセルしない → アイテム移動を許可
            if (rawSlot < MAX_DEPOSIT_SLOT) {
                return; // allow
            }
        }

        // それ以外（プレイヤーインベントリなど）はデフォルト動作
    }

    private void applyInsuranceToAll(Player player) {
        int successCount = 0;
        for (int i = 0; i < MAX_DEPOSIT_SLOT; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            if (repository.addInsurance(item)) {
                successCount++;
            }
        }
        player.sendMessage(Component.text(successCount + " 個のアイテムに保険をつけました。", NamedTextColor.GREEN));
        closeLater(player);
    }

    private void removeInsuranceFromAll(Player player) {
        int successCount = 0;
        for (int i = 0; i < MAX_DEPOSIT_SLOT; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            if (repository.removeInsurance(item)) {
                successCount++;
            }
        }
        player.sendMessage(Component.text(successCount + " 個のアイテムから保険を外しました。", NamedTextColor.YELLOW));
        closeLater(player);
    }

    private void closeLater(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.closeInventory();
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        // GUIに残ったアイテムをプレイヤーに返却
        Player player = (Player) event.getPlayer();
        for (int i = 0; i < MAX_DEPOSIT_SLOT; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                player.getInventory().addItem(item.clone());
                inventory.setItem(i, null);
            }
        }

        // リスナー解除
        HandlerList.unregisterAll(this);
    }
} 