package net.alcaris.plugin.items.gui;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.lib.ItemsRepository;
import net.alcaris.plugin.items.enums.InsuranceType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import jp.jyn.jecon.Jecon;
import java.math.BigDecimal;

public class InsuranceCostTeleportGUI implements Listener {

    private final AlcarisItems plugin;
    private final ItemsRepository repository;
    private final Location targetLocation;
    private final int costPerItem;
    private final int additionalCost;
    private final Jecon jecon;

    private final Inventory inventory;

    private static final int INVENTORY_SIZE = 9; // 1 行だけで OK
    private static final int BUTTON_CONFIRM_SLOT = 4;

    public InsuranceCostTeleportGUI(AlcarisItems plugin, ItemsRepository repository, Location targetLocation,
                                    int costPerItem, int additionalCost) {
        this.plugin = plugin;
        this.repository = repository;
        this.targetLocation = targetLocation;
        this.costPerItem = costPerItem;
        this.additionalCost = additionalCost;
        this.jecon = plugin.getJecon();

        this.inventory = Bukkit.createInventory(null, INVENTORY_SIZE, Component.text("保険-支払い+テレポート", NamedTextColor.AQUA));

        // イベント登録
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, plugin);
    }

    private int calculateCost(Player player) {
        int total = additionalCost;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType().isAir()) continue;
            if (!repository.hasInsurance(stack)) continue;
            InsuranceType type = repository.getInsuranceType(stack);
            if (type == null) type = InsuranceType.STANDARD;
            total += type.getBaseDiamondCost() * costPerItem * stack.getAmount();
        }
        return total;
    }

    private void updateCostDisplay(Player player) {
        int cost = calculateCost(player);
        ItemStack confirm = new ItemStack(Material.PAPER);
        ItemMeta meta = confirm.getItemMeta();
        meta.displayName(Component.text("支払ってテレポート (費用: " + cost + "G)", NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        confirm.setItemMeta(meta);
        inventory.setItem(BUTTON_CONFIRM_SLOT, confirm);
    }

    public void open(Player player) {
        updateCostDisplay(player);
        player.openInventory(inventory);
    }

    // InventoryHolder を実装しなくなったため getInventory は不要

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        int rawSlot = event.getRawSlot();

        // すべてキャンセル（アイテム移動不可）
        event.setCancelled(true);

        if (rawSlot == BUTTON_CONFIRM_SLOT) {
            handleConfirm((Player) event.getWhoClicked());
        }
    }

    private void handleConfirm(Player player) {
        int cost = calculateCost(player);
        if (cost < 0) {
            player.sendMessage(Component.text("費用が計算できません。", NamedTextColor.RED));
            return;
        }

        boolean paid = false;
        if (jecon != null) {
            java.util.UUID uuid = player.getUniqueId();
            java.util.Optional<BigDecimal> opt = jecon.getRepository().getDecimal(uuid);
            BigDecimal balance = opt.orElse(BigDecimal.ZERO);
            if (balance.compareTo(BigDecimal.valueOf(cost)) < 0) {
                player.sendMessage(Component.text("残高が不足しています。必要: " + cost + "G", NamedTextColor.RED));
                return;
            }
            jecon.getRepository().set(uuid, balance.subtract(BigDecimal.valueOf(cost)));
            paid = true;
        }
        if (!paid) {
            int diamondsAvailable = countDiamonds(player);
            if (diamondsAvailable < cost) {
                player.sendMessage(Component.text("ダイヤモンドが不足しています。必要: " + cost, NamedTextColor.RED));
                return;
            }
            removeDiamonds(player, cost);
        }

        Location teleportLocation = targetLocation.clone().add(0.5, 0, 0.5);
        player.teleport(teleportLocation);

        // Invincibility command for 600 seconds
        String invincibleCmd = "alcarisplayer:invincible " + player.getName() + " 600";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), invincibleCmd);

        player.sendMessage(Component.text("テレポートしました！", NamedTextColor.GREEN));
        
        closeLater(player);
    }

    private int countDiamonds(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.DIAMOND) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removeDiamonds(Player player, int amount) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (amount <= 0) break;
            if (stack == null || stack.getType() != Material.DIAMOND) continue;
            int take = Math.min(amount, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            amount -= take;
        }
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

        // 追加処理なし（アイテム移動不可のため）

        HandlerList.unregisterAll(this);
    }
} 