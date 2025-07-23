package net.alcaris.plugin.items.listeners;

import net.alcaris.plugin.core.events.ItemRegistryReloadEvent;
import net.alcaris.plugin.items.lib.InventoryUpdater;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class InventoryUpdateListener implements Listener {
    private final InventoryUpdater updater;

    public InventoryUpdateListener(InventoryUpdater updater) {
        this.updater = updater;
    }

    @EventHandler
    public void onItemReload(ItemRegistryReloadEvent event) {
        updater.updateAllPlayersItems();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updater.updatePlayerItems(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            updater.updatePlayerItems((Player) event.getWhoClicked());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            updater.updatePlayerItems((Player) event.getWhoClicked());
        }
    }
}
