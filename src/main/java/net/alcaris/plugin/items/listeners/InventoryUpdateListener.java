package net.alcaris.plugin.items.listeners;

import net.alcaris.plugin.core.event.ItemRegistryReloadEvent;
import net.alcaris.plugin.items.lib.InventoryUpdater;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryUpdateListener implements Listener {
    private final InventoryUpdater updater;
    private final Map<UUID, Long> lastUpdateTime = new ConcurrentHashMap<>();
    private static final long UPDATE_COOLDOWN = 5000L;

    public InventoryUpdateListener(InventoryUpdater updater) {
        this.updater = updater;
    }

    @EventHandler
    public void onItemReload(ItemRegistryReloadEvent event) {
        updater.updateAllPlayersItems();
        lastUpdateTime.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updater.updatePlayerItems(event.getPlayer());
        lastUpdateTime.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastUpdateTime.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            updateWithCooldown(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            updateWithCooldown(player);
        }
    }

    private void updateWithCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTime.get(uuid);

        if (lastUpdate == null || (currentTime - lastUpdate) >= UPDATE_COOLDOWN) {
            updater.updatePlayerItems(player);
            lastUpdateTime.put(uuid, currentTime);
        }
    }
}
