package moe.nmkmn.alcaris_items.repositorys;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import moe.nmkmn.alcaris_items.AlcarisItems;
import moe.nmkmn.alcaris_items.models.ItemModel;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class ItemsRepository {
    private final AlcarisItems plugin = AlcarisItems.getInstance();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ItemStack createItem(String id, int amount) {
        File cacheFile = new File(plugin.getDataFolder(), "caches" + File.separator + id + ".json");

        if (!cacheFile.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(cacheFile)) {
            ItemModel itemModel = gson.fromJson(reader, ItemModel.class);

            if (!Objects.equals(itemModel.getId(), id)) {
                return null;
            }

            return AlcarisItems.getItemConverter().convert(itemModel, amount);
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading cache file for item " + id + ": " + e.getMessage());
            return null;
        }
    }

    public ItemStack createItem(String id) {
        return createItem(id, 1);
    }
}
