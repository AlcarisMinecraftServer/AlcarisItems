package moe.nmkmn.alcaris_items;

import moe.nmkmn.alcaris_items.commands.CustomItemCommand;
import moe.nmkmn.alcaris_items.converters.FoodItemConverter;
import moe.nmkmn.alcaris_items.converters.ToolItemConverter;
import moe.nmkmn.alcaris_items.listeners.InventoryUpdateListener;
import moe.nmkmn.alcaris_items.utils.ApiClientManager;
import moe.nmkmn.alcaris_items.listeners.AdminAlertListener;
import moe.nmkmn.alcaris_items.utils.CacheManager;
import moe.nmkmn.alcaris_items.utils.InventoryUpdater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AlcarisItems extends JavaPlugin {
    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    public Component prefix = miniMessage.deserialize("[<gradient:#8a80ff:#9400d9>AlcarisItems</gradient>] ");

    public final String API_URL = "http://localhost:8080/v2";
    private boolean isUsingCacheFallback = false;

    @Override
    public void onEnable() {
        CacheManager cacheManager = new CacheManager(this);
        ApiClientManager apiClient = new ApiClientManager(API_URL);

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
        InventoryUpdater inventoryUpdater = new InventoryUpdater(this, foodItemConverter, toolItemConverter);

        // Listener
        getServer().getPluginManager().registerEvents(new AdminAlertListener(this, isUsingCacheFallback), this);
        getServer().getPluginManager().registerEvents(new InventoryUpdateListener(inventoryUpdater), this);

        // Scheduler
        getServer().getScheduler().runTaskTimer(this, inventoryUpdater::updateAllPlayersItems, 0L, 1200L);

        // Commands
        Objects.requireNonNull(this.getCommand("custom-item")).setExecutor(new CustomItemCommand(this, foodItemConverter, toolItemConverter));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
