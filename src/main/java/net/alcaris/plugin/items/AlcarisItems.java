package net.alcaris.plugin.items;

import net.alcaris.plugin.items.commands.CustomItemCommand;
import net.alcaris.plugin.items.converters.FoodItemConverter;
import net.alcaris.plugin.items.converters.ItemConverter;
import net.alcaris.plugin.items.converters.MaterialItemConverter;
import net.alcaris.plugin.items.converters.ToolItemConverter;
import net.alcaris.plugin.items.listeners.InventoryUpdateListener;
import net.alcaris.plugin.items.repositorys.ItemsRepository;
import net.alcaris.plugin.items.utils.ApiClientManager;
import net.alcaris.plugin.items.listeners.AdminAlertListener;
import net.alcaris.plugin.items.utils.CacheManager;
import net.alcaris.plugin.items.utils.InventoryUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AlcarisItems extends JavaPlugin {
    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    public Component prefix = miniMessage.deserialize("[<gradient:#8a80ff:#9400d9>AlcarisItems</gradient>] ");

    private static AlcarisItems instance;
    private static ItemsRepository repository;
    private static ItemConverter itemConverter;

    public String API_URL;
    public String API_KEY;
    private boolean isUsingCacheFallback = false;

    @Override
    public void onEnable() {
        instance = this;
        repository = new ItemsRepository();

        saveDefaultConfig();

        API_URL = getConfig().getString("apiUrl");
        API_KEY = getConfig().getString("apiKey");

        if (API_URL == null || API_KEY == null) {
            getLogger().severe("API URL and API KEY are missing.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        CacheManager cacheManager = new CacheManager(this);
        ApiClientManager apiClient = new ApiClientManager(API_URL, API_KEY);

        getLogger().info("アイテムデータを取得中...");
        String response = null;
        try {
            response = apiClient.fetchItemData();
            cacheManager.saveCaches(response);
            getLogger().info("アイテムデータの取得とキャッシュ保存に成功しました。");
        } catch (Exception e) {
            getLogger().severe("APIからアイテムデータを取得できませんでした: " + e.getMessage());
        }

        if (response == null) {
            if (!cacheManager.hasCache()) {
                getLogger().severe("キャッシュデータも存在しないため、サーバーをシャットダウンします。");
                Bukkit.getServer().shutdown();
                return;
            } else {
                getLogger().warning("APIアクセスに失敗したため、キャッシュデータを使用します。");
                isUsingCacheFallback = true;
            }
        }

        FoodItemConverter foodItemConverter = new FoodItemConverter(this);
        ToolItemConverter toolItemConverter = new ToolItemConverter(this);
        MaterialItemConverter materialItemConverter = new MaterialItemConverter(this);

        itemConverter = new ItemConverter(foodItemConverter, toolItemConverter, materialItemConverter);

        InventoryUpdater inventoryUpdater = new InventoryUpdater(this, foodItemConverter, toolItemConverter, materialItemConverter);

        // Listener
        getServer().getPluginManager().registerEvents(new AdminAlertListener(this, isUsingCacheFallback), this);
        getServer().getPluginManager().registerEvents(new InventoryUpdateListener(inventoryUpdater), this);

        // Scheduler
        getServer().getScheduler().runTaskTimer(this, inventoryUpdater::updateAllPlayersItems, 0L, 1200L);

        // Commands
        Objects.requireNonNull(this.getCommand("custom-item")).setExecutor(
                new CustomItemCommand(
                        this,
                        foodItemConverter,
                        toolItemConverter,
                        materialItemConverter
                )
        );
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
}
