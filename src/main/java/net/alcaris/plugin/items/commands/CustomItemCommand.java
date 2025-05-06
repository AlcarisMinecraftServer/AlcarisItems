package net.alcaris.plugin.items.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.converters.*;
import net.alcaris.plugin.items.models.ItemModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CustomItemCommand implements CommandExecutor, TabCompleter {
    private final AlcarisItems plugin;
    private final ItemConverter itemConverter;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public CustomItemCommand(
            AlcarisItems plugin,
            FoodItemConverter foodItemConverter,
            ToolItemConverter toolItemConverter,
            MaterialItemConverter materialItemConverter,
            WeaponItemConverter weaponItemConverter
    ) {
        this.plugin = plugin;

        this.itemConverter = new ItemConverter(foodItemConverter, toolItemConverter, materialItemConverter,weaponItemConverter);
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        final String input = args[args.length - 1].toLowerCase();

        switch (args.length) {
            case 1 -> {
                List<String> options = List.of("give");
                options.stream()
                        .filter(option -> option.toLowerCase().startsWith(input))
                        .forEach(completions::add);
            }
            case 2 -> {
                if (args[0].equals("give")) {
                    Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .forEach(completions::add);
                }
            }
            case 3 -> {
                if (args[0].equals("give")) {
                    Path caches = plugin.getDataFolder().toPath().resolve("caches");

                    try (Stream<Path> stream = Files.list(caches)) {
                        stream.map(path -> path.getFileName().toString().replace(".json", ""))
                                .filter(name -> name.toLowerCase().startsWith(input))
                                .forEach(completions::add);
                    } catch (IOException e) {
                        plugin.getLogger().severe(e.getMessage());
                    }
                }
            }
            case 4 -> {
                if (args[0].equals("give")) {
                    completions.add("<amount>");
                }
            }
        }

        return completions;
    }

    @Override
    @Deprecated(forRemoval = true)
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(
                    plugin.prefix.append(
                            Component.text("使用法: /custom-item give <player> <id> [amount]")
                                    .color(NamedTextColor.RED)
                    )
            );
            return true;
        }

        if (!args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(
                    plugin.prefix.append(
                            Component.text("使用法: /custom-item give <player> <id> [amount]")
                                    .color(NamedTextColor.RED)
                    )
            );
            return true;
        }

        Player targetPlayer = null;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equals(args[1])) {
                targetPlayer = onlinePlayer;
                break;
            }
        }

        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "プレイヤーが存在しません。");
            return true;
        }

        String itemId = args[2];
        File cacheFile = new File(plugin.getDataFolder(), "caches" + File.separator + itemId + ".json");

        if (!cacheFile.exists()) {
            sender.sendMessage(ChatColor.RED + "指定されたアイテムは存在しません: " + itemId);
            return true;
        }

        try (FileReader reader = new FileReader(cacheFile)) {
            ItemModel itemData = gson.fromJson(reader, ItemModel.class);

            if (!Objects.equals(itemData.getId(), itemId)) {
                sender.sendMessage(ChatColor.RED + "指定されたアイテムはキャッシュに存在しません: " + itemId);
                return true;
            }

            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.YELLOW + "無効な個数が指定されました。1 個を付与します。");
                }
            }

            ItemStack customItem = itemConverter.convert(itemData, amount);

            if (customItem == null) {
                sender.sendMessage(ChatColor.RED + "アイテムの生成に失敗しました: " + itemId);
                return true;
            }

            targetPlayer.getInventory().addItem(customItem);
            sender.sendMessage(ChatColor.GREEN + "アイテムを受け取りました: " + itemId);
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "アイテムの読み込み中にエラーが発生しました: " + e.getMessage());
            plugin.getLogger().severe("Error reading cache file for item " + itemId + ": " + e.getMessage());
        }

        return true;
    }

}
