package net.alcaris.plugin.items.magic;

import net.alcaris.plugin.items.AlcarisItems;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class MagicRepository {
    private final AlcarisItems plugin;
    private final Map<String, MagicSpell> idToSpell = new HashMap<>();

    public MagicRepository(AlcarisItems plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "magic.yml");
        if (!file.exists()) {
            plugin.saveResource("magic.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection spellsSec = cfg.getConfigurationSection("spells");
        idToSpell.clear();
        if (spellsSec == null) return;
        for (String id : spellsSec.getKeys(false)) {
            ConfigurationSection s = spellsSec.getConfigurationSection(id);
            if (s == null) continue;
            String name = s.getString("name", id);
            int mp = s.getInt("mp", 0);
            int cooldown = s.getInt("cooldown", 0);
            MagicType type = MagicType.valueOf(s.getString("type", "BUFF").toUpperCase(Locale.ROOT));
            ConfigurationSection conf = s.getConfigurationSection("config");

            MagicSpellBuffConfig buff = null;
            MagicSpellAoeConfig aoe = null;
            MagicSpellThrowConfig thr = null;
            if (conf != null) {
                switch (type) {
                    case BUFF -> {
                        List<MagicSpellBuffConfig.EffectSpec> effects = new ArrayList<>();
                        List<?> list = conf.getList("effects");
                        if (list != null) {
                            for (Object o : list) {
                                if (o instanceof Map) {
                                    Map<?,?> m = (Map<?,?>) o;
                                    String t = Objects.toString(m.get("type"), "SPEED");
                                    int amp = Integer.parseInt(Objects.toString(m.get("amplifier"), "0"));
                                    int dur = Integer.parseInt(Objects.toString(m.get("duration"), "200"));
                                    effects.add(new MagicSpellBuffConfig.EffectSpec(t, amp, dur));
                                }
                            }
                        }
                        buff = new MagicSpellBuffConfig(effects);
                    }
                    case AOE -> {
                        double radius = conf.getDouble("radius", 4.0);
                        double damage = conf.getDouble("damage", 4.0);
                        int ignite = conf.getInt("igniteSeconds", 0);
                        aoe = new MagicSpellAoeConfig(radius, damage, ignite);
                    }
                    case THROW -> {
                        String projectile = conf.getString("projectile", "SNOWBALL");
                        double radius = conf.getDouble("radius", 4.0);
                        double damage = conf.getDouble("damage", 6.0);
                        thr = new MagicSpellThrowConfig(projectile, radius, damage);
                    }
                }
            }
            MagicSpell spell = new MagicSpell(id, name, mp, cooldown, type, buff, aoe, thr);
            idToSpell.put(id, spell);
        }
        plugin.getLogger().info("Loaded spells: " + idToSpell.keySet());
    }

    public MagicSpell get(String id) { return idToSpell.get(id); }
    public Collection<MagicSpell> getAll() { return idToSpell.values(); }
}


