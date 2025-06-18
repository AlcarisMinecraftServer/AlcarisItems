package net.alcaris.plugin.items.listeners;

import net.alcaris.plugin.items.AlcarisItems;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WeaponDurabilityListener implements Listener {
    private final AlcarisItems plugin;
    private final NamespacedKey weaponTypeKey;
    private final NamespacedKey durabilityKey;

    public WeaponDurabilityListener(AlcarisItems plugin) {
        this.plugin = plugin;
        this.weaponTypeKey = new NamespacedKey(plugin, "weapon_type");
        this.durabilityKey = new NamespacedKey(plugin, "durability");
    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!isWeapon(item)) {
            return;
        }

        decreaseDurability(item);
    }

    @EventHandler
    public void onWeaponAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!isWeapon(item)) {
            return;
        }

        decreaseDurability(item);
    }

    private boolean isWeapon(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        return container.has(weaponTypeKey, PersistentDataType.STRING);
    }

    private void decreaseDurability(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        int maxDurability = container.getOrDefault(durabilityKey, PersistentDataType.INTEGER, 0);
        
        if (maxDurability <= 0) {
            return;
        }

        int currentDamage = damageable.getDamage();
        if (currentDamage >= maxDurability) {
            return;
        }

        damageable.setDamage(currentDamage + 1);
        item.setItemMeta(damageable);

        // 耐久値が0になった場合の処理
        if (currentDamage + 1 >= maxDurability) {
            // TODO: 必要に応じて武器が壊れた時の処理を追加
        }
    }
} 