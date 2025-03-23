package net.alcaris.plugin.items.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alcaris.plugin.items.models.ItemModel;
import net.alcaris.plugin.items.models.ItemsResponse;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class CacheManager {
    private final JavaPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public CacheManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveCaches(String response) throws IOException {
        File cachesDir = new File(plugin.getDataFolder(), "caches");
        if (!cachesDir.exists() && !cachesDir.mkdirs()) {
            throw new IOException("Couldn't create cache directory: " + cachesDir.getAbsolutePath());
        }

        ItemsResponse data = gson.fromJson(response, ItemsResponse.class);
        for (ItemModel item : data.getData()) {
            String prettyJson = gson.toJson(item);
            File cacheFile = new File(cachesDir, item.getId() + ".json");

            boolean needsUpdate = true;

            if (cacheFile.exists()) {
                try {
                    String existingContent = Files.readString(cacheFile.toPath());
                    if (existingContent.equals(prettyJson)) {
                        needsUpdate = false;
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Error reading cache file " + cacheFile.getAbsolutePath() +
                            ", attempting update. Error: " + e.getMessage());
                }
            }

            if (needsUpdate) {
                try (FileWriter writer = new FileWriter(cacheFile)) {
                    writer.write(prettyJson);
                    plugin.getLogger().info("Cache file updated: " + cacheFile.getName());
                } catch (IOException e) {
                    plugin.getLogger().severe("Couldn't write cache file: " + cacheFile.getAbsolutePath());
                }
            }
        }
    }

    public boolean hasCache() {
        File cachesDir = new File(plugin.getDataFolder(), "caches");
        return cachesDir.exists() && cachesDir.isDirectory() &&
                cachesDir.listFiles() != null && Objects.requireNonNull(cachesDir.listFiles()).length > 0;
    }
}