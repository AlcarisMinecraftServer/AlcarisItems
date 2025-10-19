package net.alcaris.plugin.items;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.commands.CustomItemCommand;
import net.alcaris.plugin.items.commands.ItemLossCommand;
import net.alcaris.plugin.items.converters.*;
import net.alcaris.plugin.items.listeners.InventoryUpdateListener;
import net.alcaris.plugin.items.lib.ItemsRepository;
import net.alcaris.plugin.items.lib.InventoryUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import jp.jyn.jecon.Jecon;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class AlcarisItems extends JavaPlugin {
    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    public Component prefix = miniMessage.deserialize("[<gradient:#8a80ff:#9400d9>AlcarisItems</gradient>] ");

    private static AlcarisItems instance;
    private static ItemsRepository repository;
    private static ItemConverter itemConverter;
    private Jecon jecon;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        instance = this;
        repository = new ItemsRepository();

        FoodItemConverter foodItemConverter = new FoodItemConverter(this);
        ToolItemConverter toolItemConverter = new ToolItemConverter(this);
        MaterialItemConverter materialItemConverter = new MaterialItemConverter(this);
        WeaponItemConverter weaponItemConverter = new WeaponItemConverter(this);
        ArmorItemConverter armorItemConverter = new ArmorItemConverter(this);

        itemConverter = new ItemConverter(foodItemConverter, toolItemConverter, materialItemConverter, weaponItemConverter, armorItemConverter);

        InventoryUpdater inventoryUpdater = new InventoryUpdater(this, foodItemConverter, toolItemConverter, materialItemConverter, weaponItemConverter, armorItemConverter);

        // Listener
        getServer().getPluginManager().registerEvents(new InventoryUpdateListener(inventoryUpdater), this);

        // Scheduler
        getServer().getScheduler().runTaskTimer(this, inventoryUpdater::updateAllPlayersItems, 0L, 1L);

        // after scheduler and before commands registration
        Plugin econPlugin = getServer().getPluginManager().getPlugin("Jecon");
        if (econPlugin instanceof Jecon jeconPlugin) {
            this.jecon = jeconPlugin;
            getLogger().info("Jecon economy hooked.");
        } else {
            getLogger().warning("Jecon plugin not found! Economy features disabled.");
        }

        // Commands
        Objects.requireNonNull(this.getCommand("custom-item")).setExecutor(
                new CustomItemCommand(
                        this,
                        foodItemConverter,
                        toolItemConverter,
                        materialItemConverter,
                        weaponItemConverter,
                        armorItemConverter
                )
        );

        // Console-only command for applying item loss
        ItemLossCommand itemLossCommand = new ItemLossCommand(this);
        Objects.requireNonNull(this.getCommand("item-loss")).setExecutor(itemLossCommand);
        Objects.requireNonNull(this.getCommand("item-loss")).setTabCompleter(itemLossCommand);

        // New sell item command
        Objects.requireNonNull(this.getCommand("item-sell")).setExecutor(new net.alcaris.plugin.items.commands.SellItemCommand(this, repository));

        // register money-convert command
        Objects.requireNonNull(this.getCommand("money-convert")).setExecutor(new net.alcaris.plugin.items.commands.MoneyConvertCommand(this, repository));
    }

    public static AlcarisItems getInstance() {
        return instance;
    }

    @SuppressWarnings("unused")
    public static ItemsRepository getRepository() {
        return repository;
    }

    public static ItemConverter getItemConverter() {
        return itemConverter;
    }

    public Jecon getJecon() {
        return jecon;
    }
}
