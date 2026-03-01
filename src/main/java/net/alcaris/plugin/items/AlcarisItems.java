package net.alcaris.plugin.items;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alcaris.plugin.items.commands.CustomItemCommand;
import net.alcaris.plugin.items.converters.*;
import net.alcaris.plugin.items.listeners.InventoryUpdateListener;
import net.alcaris.plugin.items.lib.InventoryUpdater;
import net.alcaris.plugin.items.lib.ItemsRepository;
import net.alcaris.plugin.items.listeners.MagicListener;
import net.alcaris.plugin.items.magic.MagicRepository;
import net.alcaris.plugin.items.magic.MagicService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AlcarisItems extends JavaPlugin {
    public static final long UPDATE_INTERVAL_TICKS = 1200L;

    public static final String ITEM_ID_KEY = "item_id";
    public static final String ITEM_VERSION_KEY = "item_version";

    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    public Component prefix = miniMessage.deserialize("[<gradient:#8a80ff:#9400d9>AlcarisItems</gradient>] ");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static AlcarisItems instance;
    private static ItemsRepository repository;
    private static ItemConverter itemConverter;

    private MagicService magicService;

    @Override
    public void onEnable() {
        instance = this;
        repository = new ItemsRepository();

        FoodItemConverter foodItemConverter = new FoodItemConverter(this);
        ToolItemConverter toolItemConverter = new ToolItemConverter(this);
        MaterialItemConverter materialItemConverter = new MaterialItemConverter(this);
        WeaponItemConverter weaponItemConverter = new WeaponItemConverter(this);
        ArmorItemConverter armorItemConverter = new ArmorItemConverter(this);

        itemConverter = new ItemConverter(foodItemConverter, toolItemConverter, materialItemConverter, weaponItemConverter, armorItemConverter);

        InventoryUpdater inventoryUpdater = new InventoryUpdater(this, itemConverter);

        getServer().getPluginManager().registerEvents(new InventoryUpdateListener(inventoryUpdater), this);

        getServer().getScheduler().runTaskTimer(this, inventoryUpdater::updateAllPlayersItems, 0L, UPDATE_INTERVAL_TICKS);

        Objects.requireNonNull(this.getCommand("custom-item")).setExecutor(
                new CustomItemCommand(this, weaponItemConverter, armorItemConverter)
        );

        MagicRepository magicRepository = new MagicRepository(this);
        magicRepository.load();
        this.magicService = new MagicService(this, magicRepository);
        getServer().getPluginManager().registerEvents(new MagicListener(this, magicService), this);
        Objects.requireNonNull(this.getCommand("magic")).setExecutor(new net.alcaris.plugin.items.commands.MagicCommand(magicRepository, magicService));
        Objects.requireNonNull(this.getCommand("magic")).setTabCompleter(new net.alcaris.plugin.items.commands.MagicCommand(magicRepository, magicService));
    }

    public static Gson getGson() {
        return GSON;
    }

    public NamespacedKey getItemIdKey() {
        return new NamespacedKey(this, ITEM_ID_KEY);
    }

    public NamespacedKey getItemVersionKey() {
        return new NamespacedKey(this, ITEM_VERSION_KEY);
    }

    public static AlcarisItems getInstance() {
        return instance;
    }

    public static ItemsRepository getRepository() {
        return repository;
    }

    public static ItemConverter getItemConverter() {
        return itemConverter;
    }

    public MagicService getMagicService() {
        return magicService;
    }
}
