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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import jp.jyn.jecon.Jecon;
import java.math.BigDecimal;

/**
 * アイテム売却GUI。プレイヤーがアイテムを入れ、[決定] ボタンで売却しお金を受け取る。
 */
public class SellItemGUI implements Listener {

    private final AlcarisItems plugin;
    private final ItemsRepository repository;
    private final Inventory inventory;

    private static final int INVENTORY_SIZE = 54; // 6行
    private static final int BUTTON_CONFIRM_SLOT = 49; // 下段中央(1-based 50 番目)
    private static final int MAX_DEPOSIT_SLOT = 45; // 0-44 がデポジット

    public SellItemGUI(AlcarisItems plugin, ItemsRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        this.inventory = Bukkit.createInventory(null, INVENTORY_SIZE, Component.text("アイテム売却", NamedTextColor.GOLD));

        // 初期ボタン
        updatePriceDisplay();

        // イベント登録
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, plugin);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    /**
     * デポジットスロットのアイテム合計売却額を計算
     */
    private int calculateTotalPrice() {
        int total = 0;
        for (int i = 0; i < MAX_DEPOSIT_SLOT; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType().isAir()) continue;
            int pricePer = repository.getSellPrice(stack);
            total += pricePer * stack.getAmount();
        }
        return total;
    }

    private void updatePriceDisplay() {
        int total = calculateTotalPrice();
        ItemStack confirm = new ItemStack(Material.EMERALD);
        ItemMeta meta = confirm.getItemMeta();
        meta.displayName(Component.text("決定 (合計: " + total + "G)", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        confirm.setItemMeta(meta);
        inventory.setItem(BUTTON_CONFIRM_SLOT, confirm);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        int rawSlot = event.getRawSlot();
        boolean clickedTop = event.getClickedInventory() != null && event.getClickedInventory().equals(inventory);

        if (clickedTop) {
            // confirm button
            if (rawSlot == BUTTON_CONFIRM_SLOT) {
                event.setCancelled(true);
                handleConfirm((Player) event.getWhoClicked());
                return;
            }
            // deposit area allow movement
            if (rawSlot < MAX_DEPOSIT_SLOT) {
                // allow shift-click etc. We'll update price later
            } else {
                // other GUI slots cancel
                event.setCancelled(true);
            }
        }

        // update display after tick to reflect new inventory state
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePriceDisplay();
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        // If any slot in deposit or confirm affected, update after tick
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePriceDisplay();
            }
        }.runTask(plugin);
    }

    private void handleConfirm(Player player) {
        int totalEarned = 0;
        List<ItemStack> unsellable = new ArrayList<>();

        for (int i = 0; i < MAX_DEPOSIT_SLOT; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType().isAir()) continue;

            if (!repository.canSell(stack)) {
                unsellable.add(stack.clone());
                continue;
            }

            int pricePer = repository.getSellPrice(stack);
            if (pricePer <= 0) {
                unsellable.add(stack.clone());
                continue;
            }

            totalEarned += pricePer * stack.getAmount();
            inventory.setItem(i, null); // remove sold item
        }

        // Pay money
        if (totalEarned > 0) {
            Jecon jecon = plugin.getJecon();
            if (jecon != null) {
                java.util.UUID uuid = player.getUniqueId();
                java.util.Optional<java.math.BigDecimal> opt = jecon.getRepository().getDecimal(uuid);
                java.math.BigDecimal cur = opt.orElse(java.math.BigDecimal.ZERO);
                jecon.getRepository().set(uuid, cur.add(java.math.BigDecimal.valueOf(totalEarned)));
            } else {
                // Fallback: use command if Jecon not found
                String cmd = "money give " + player.getName() + " " + totalEarned;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            player.sendMessage(Component.text(totalEarned + "G を獲得しました！", NamedTextColor.GREEN));
        }

        // Handle unsellable items
        for (ItemStack item : unsellable) {
            String name;
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                name = item.getItemMeta().getDisplayName();
            } else {
                name = item.getType().name();
            }
            player.getInventory().addItem(item);
            player.sendMessage(Component.text("\"" + name + "\" というアイテムは売れません！", NamedTextColor.RED));
        }

        // Close GUI after one tick to ensure updates
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

        // return remaining items
        Player player = (Player) event.getPlayer();
        for (int i = 0; i < MAX_DEPOSIT_SLOT; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                player.getInventory().addItem(item.clone());
                inventory.setItem(i, null);
            }
        }

        HandlerList.unregisterAll(this);
    }
}