package net.alcaris.plugin.items.listeners;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.magic.MagicService;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class MagicListener implements Listener {
    private final AlcarisItems plugin;
    private final MagicService magicService;

    public MagicListener(AlcarisItems plugin, MagicService magicService) {
        this.plugin = plugin;
        this.magicService = magicService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        int defaultMp = plugin.getConfig().getInt("magic.defaultMp", 100);
        Player p = event.getPlayer();
        if (magicService.getPlayerMp(p) <= 0) {
            magicService.setPlayerMp(p, defaultMp);
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!magicService.isWand(item)) return;
        boolean ok = magicService.castSelected(player);
        if (ok) {
            player.swingMainHand();
            event.setCancelled(true);
        } else {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 0.5f);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        Double radius = proj.getPersistentDataContainer().get(new NamespacedKey(plugin, "magic_throw_radius"), PersistentDataType.DOUBLE);
        Double damage = proj.getPersistentDataContainer().get(new NamespacedKey(plugin, "magic_throw_damage"), PersistentDataType.DOUBLE);
        if (radius == null || damage == null) return;
        Entity shooter = (Entity) proj.getShooter();
        proj.getWorld().spawnParticle(Particle.EXPLOSION, proj.getLocation(), 5, radius / 3, radius / 3, radius / 3, 0.02);
        proj.getWorld().playSound(proj.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        for (Entity e : proj.getNearbyEntities(radius, radius, radius)) {
            if (e.equals(shooter)) continue;
            if (e instanceof org.bukkit.entity.LivingEntity le) {
                double multiplier = shooter instanceof Player p ? plugin.getMagicService().getAbilityDamageMultiplier(p) : 1.0;
                le.damage(damage * multiplier, shooter != null ? shooter : proj);
            }
        }
    }
}
