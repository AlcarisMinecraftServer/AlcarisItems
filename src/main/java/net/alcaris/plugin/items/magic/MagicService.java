package net.alcaris.plugin.items.magic;

import net.alcaris.plugin.items.AlcarisItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.*;

public class MagicService {
    private final AlcarisItems plugin;
    private final MagicRepository repository;

    private final NamespacedKey selectedMagicKey;
    private final NamespacedKey mpKey;
    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();
    private Object playerStatsRepository; // AlcarisPlayers repository (loaded via reflection)
    private Class<?> playerStatsRepositoryClass;
    private Class<?> gameStatsViewClass;

    public MagicService(AlcarisItems plugin, MagicRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        this.selectedMagicKey = new NamespacedKey(plugin, "selected_magic");
        this.mpKey = new NamespacedKey(plugin, "mp");

        // Try to load AlcarisPlayer repository using provider plugin's classloader and Bukkit Services
        try {
            Plugin ap = Bukkit.getPluginManager().getPlugin("AlcarisPlayer");
            if (ap != null) {
                ClassLoader apCl = ap.getClass().getClassLoader();
                this.playerStatsRepositoryClass = Class.forName(
                        "somen1000.trump.alcarisPlayer.repository.PlayerStatsRepository",
                        true,
                        apCl
                );
                RegisteredServiceProvider<?> reg = Bukkit.getServicesManager().getRegistration((Class) this.playerStatsRepositoryClass);
                if (reg != null && reg.getProvider() != null) {
                    this.playerStatsRepository = reg.getProvider();
                    for (Class<?> inner : playerStatsRepositoryClass.getDeclaredClasses()) {
                        if (inner.getSimpleName().equals("GameStatsView")) {
                            this.gameStatsViewClass = inner;
                            break;
                        }
                    }
                    plugin.getLogger().info("AlcarisPlayer repository hooked.");
                } else {
                    plugin.getLogger().warning("AlcarisPlayer service not found. Falling back to PDC MP.");
                }
            } else {
                plugin.getLogger().warning("AlcarisPlayer plugin not found. Falling back to PDC MP.");
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to hook AlcarisPlayer repository: " + t.getMessage());
        }
    }

    public void setSelectedMagic(Player player, String id) {
        player.getPersistentDataContainer().set(selectedMagicKey, PersistentDataType.STRING, id);
    }

    public String getSelectedMagic(Player player) {
        return player.getPersistentDataContainer().get(selectedMagicKey, PersistentDataType.STRING);
    }

    public int getPlayerMp(Player player) {
        if (playerStatsRepository != null) {
            try {
                Method getStats = playerStatsRepositoryClass.getMethod("getStats", Player.class);
                Object view = getStats.invoke(playerStatsRepository, player);
                if (view != null) {
                    Method getMp = view.getClass().getMethod("getMp");
                    double mp = ((Number) getMp.invoke(view)).doubleValue();
                    return (int) Math.floor(mp + 1e-6);
                }
            } catch (Throwable ignored) {}
        }
        // Fallback: direct reflection to PlayerGameStats
        try {
            Plugin ap = Bukkit.getPluginManager().getPlugin("AlcarisPlayer");
            if (ap != null) {
                ClassLoader apCl = ap.getClass().getClassLoader();
                Class<?> pgsClass = Class.forName("somen1000.trump.alcarisPlayer.data.PlayerGameStats", true, apCl);
                Object pgsInstance = null;
                try {
                    Method getInstance = pgsClass.getMethod("getInstance");
                    pgsInstance = getInstance.invoke(null);
                } catch (NoSuchMethodException ignored) {}
                Object statsHolder;
                Method getGameStats = pgsClass.getMethod("getGameStats", Player.class);
                statsHolder = pgsInstance != null ? getGameStats.invoke(pgsInstance, player) : getGameStats.invoke(null, player);
                if (statsHolder != null) {
                    Method getMp = statsHolder.getClass().getMethod("getMp");
                    double mp = ((Number) getMp.invoke(statsHolder)).doubleValue();
                    return (int) Math.floor(mp + 1e-6);
                }
            }
        } catch (Throwable ignored) {}
        Integer v = player.getPersistentDataContainer().get(mpKey, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public void setPlayerMp(Player player, int mp) {
        if (playerStatsRepository != null) {
            try {
                Method setMp = playerStatsRepositoryClass.getMethod("setMp", Player.class, double.class);
                Object res = setMp.invoke(playerStatsRepository, player, (double) Math.max(mp, 0));
                if (res instanceof Boolean b && b) return;
                // fall through to fallback if repository refused
            } catch (Throwable ignored) {}
        }
        // Fallback: direct reflection to PlayerGameStats
        try {
            Plugin ap = Bukkit.getPluginManager().getPlugin("AlcarisPlayer");
            if (ap != null) {
                ClassLoader apCl = ap.getClass().getClassLoader();
                Class<?> pgsClass = Class.forName("somen1000.trump.alcarisPlayer.data.PlayerGameStats", true, apCl);
                Object pgsInstance = null;
                try {
                    Method getInstance = pgsClass.getMethod("getInstance");
                    pgsInstance = getInstance.invoke(null);
                } catch (NoSuchMethodException ignored) {}
                Object statsHolder;
                Method getGameStats = pgsClass.getMethod("getGameStats", Player.class);
                statsHolder = pgsInstance != null ? getGameStats.invoke(pgsInstance, player) : getGameStats.invoke(null, player);
                if (statsHolder != null) {
                    Method setMpM = statsHolder.getClass().getMethod("setMp", double.class);
                    setMpM.invoke(statsHolder, (double) Math.max(mp, 0));
                    return;
                }
            }
        } catch (Throwable ignored) {}
        player.getPersistentDataContainer().set(mpKey, PersistentDataType.INTEGER, Math.max(mp, 0));
    }

    public boolean consumeMp(Player player, int amount) {
        if (playerStatsRepository != null) {
            try {
                Method removeMp = playerStatsRepositoryClass.getMethod("removeMp", Player.class, double.class);
                Object res = removeMp.invoke(playerStatsRepository, player, (double) amount);
                if (res instanceof Boolean b) {
                    if (b) return true;
                    // if repository refused, try fallback instead of failing immediately
                } else {
                    // unknown return type → assume success
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        // Fallback: direct reflection to PlayerGameStats
        try {
            Plugin ap = Bukkit.getPluginManager().getPlugin("AlcarisPlayer");
            if (ap != null) {
                ClassLoader apCl = ap.getClass().getClassLoader();
                Class<?> pgsClass = Class.forName("somen1000.trump.alcarisPlayer.data.PlayerGameStats", true, apCl);
                Object pgsInstance = null;
                try {
                    Method getInstance = pgsClass.getMethod("getInstance");
                    pgsInstance = getInstance.invoke(null);
                } catch (NoSuchMethodException ignored) {}
                Object statsHolder;
                Method getGameStats = pgsClass.getMethod("getGameStats", Player.class);
                statsHolder = pgsInstance != null ? getGameStats.invoke(pgsInstance, player) : getGameStats.invoke(null, player);
                if (statsHolder != null) {
                    Method getMpM = statsHolder.getClass().getMethod("getMp");
                    double cur = ((Number) getMpM.invoke(statsHolder)).doubleValue();
                    if (cur < amount) return false;
                    Method setMpM = statsHolder.getClass().getMethod("setMp", double.class);
                    setMpM.invoke(statsHolder, cur - amount);
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        int cur = getPlayerMp(player);
        if (cur < amount) return false;
        setPlayerMp(player, cur - amount);
        return true;
    }

    public double getAbilityDamageMultiplier(Player player) {
        if (playerStatsRepository != null) {
            try {
                Method getStats = playerStatsRepositoryClass.getMethod("getStats", Player.class);
                Object view = getStats.invoke(playerStatsRepository, player);
                if (view != null) {
                    String statName = plugin.getConfig().getString("magic.abilityMultiplierStat", "mat");
                    double scale = plugin.getConfig().getDouble("magic.abilityMultiplierScale", 0.01);
                    String methodName = "get" + statName.substring(0, 1).toUpperCase(Locale.ROOT) + statName.substring(1);
                    Method getter = view.getClass().getMethod(methodName);
                    double statValue = ((Number) getter.invoke(view)).doubleValue();
                    return Math.max(0.0, 1.0 + statValue * scale);
                }
            } catch (Throwable ignored) {}
        }
        // Fallback: direct reflection to PlayerGameStats
        try {
            Plugin ap = Bukkit.getPluginManager().getPlugin("AlcarisPlayer");
            if (ap != null) {
                ClassLoader apCl = ap.getClass().getClassLoader();
                Class<?> pgsClass = Class.forName("somen1000.trump.alcarisPlayer.data.PlayerGameStats", true, apCl);
                Object pgsInstance = null;
                try {
                    Method getInstance = pgsClass.getMethod("getInstance");
                    pgsInstance = getInstance.invoke(null);
                } catch (NoSuchMethodException ignored) {}
                Object statsHolder;
                Method getGameStats = pgsClass.getMethod("getGameStats", Player.class);
                statsHolder = pgsInstance != null ? getGameStats.invoke(pgsInstance, player) : getGameStats.invoke(null, player);
                if (statsHolder != null) {
                    String statName = plugin.getConfig().getString("magic.abilityMultiplierStat", "mat");
                    double scale = plugin.getConfig().getDouble("magic.abilityMultiplierScale", 0.01);
                    String methodName = "get" + statName.substring(0, 1).toUpperCase(Locale.ROOT) + statName.substring(1);
                    Method getter = statsHolder.getClass().getMethod(methodName);
                    double statValue = ((Number) getter.invoke(statsHolder)).doubleValue();
                    return Math.max(0.0, 1.0 + statValue * scale);
                }
            }
        } catch (Throwable ignored) {}
        return 1.0;
    }

    public boolean isOnCooldown(Player player, MagicSpell spell) {
        Map<String, Long> map = playerCooldowns.getOrDefault(player.getUniqueId(), Collections.emptyMap());
        long now = System.currentTimeMillis();
        Long until = map.get(spell.getId());
        return until != null && until > now;
    }

    public void setCooldown(Player player, MagicSpell spell) {
        playerCooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(spell.getId(), System.currentTimeMillis() + spell.getCooldownTicks() * 50L);
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer c = meta.getPersistentDataContainer();
        String id = c.get(new NamespacedKey(plugin, "item_id"), PersistentDataType.STRING);
        if (id == null) return false;
        return id.startsWith("magic_wand_");
    }

    public boolean castSelected(Player player) {
        String selected = getSelectedMagic(player);
        if (selected == null) return false;
        MagicSpell spell = repository.get(selected);
        if (spell == null) return false;
        if (isOnCooldown(player, spell)) return false;
        if (!consumeMp(player, spell.getMpCost())) return false;

        switch (spell.getType()) {
            case BUFF -> castBuff(player, spell);
            case AOE -> castAoe(player, spell);
            case THROW -> castThrow(player, spell);
        }
        setCooldown(player, spell);
        return true;
    }

    private void castBuff(Player player, MagicSpell spell) {
        MagicSpellBuffConfig cfg = spell.getBuffConfig();
        if (cfg == null) return;
        for (MagicSpellBuffConfig.EffectSpec e : cfg.getEffects()) {
            PotionEffectType type = PotionEffectType.getByName(e.type);
            if (type == null) continue;
            player.addPotionEffect(new PotionEffect(type, e.durationTicks, e.amplifier, true, true));
        }
        player.getWorld().spawnParticle(Particle.INSTANT_EFFECT, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    }

    private void castAoe(Player player, MagicSpell spell) {
        MagicSpellAoeConfig cfg = spell.getAoeConfig();
        if (cfg == null) return;
        double r = cfg.getRadius();
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 3, r/2, 1, r/2, 0.01);
        double multiplier = getAbilityDamageMultiplier(player);
        for (Entity e : player.getNearbyEntities(r, r, r)) {
            if (e instanceof Player p && p.equals(player)) continue;
            if (e instanceof LivingEntity le) {
                le.damage(cfg.getDamage() * multiplier, player);
                if (cfg.getIgniteSeconds() > 0) {
                    le.setFireTicks(cfg.getIgniteSeconds() * 20);
                }
            }
        }
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
    }

    private void castThrow(Player player, MagicSpell spell) {
        MagicSpellThrowConfig cfg = spell.getThrowConfig();
        if (cfg == null) return;
        Projectile projectile = player.launchProjectile(org.bukkit.entity.Snowball.class);
        projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(1.25));
        projectile.getPersistentDataContainer().set(new NamespacedKey(plugin, "magic_throw_radius"), PersistentDataType.DOUBLE, cfg.getRadius());
        projectile.getPersistentDataContainer().set(new NamespacedKey(plugin, "magic_throw_damage"), PersistentDataType.DOUBLE, cfg.getDamage());
    }
}


