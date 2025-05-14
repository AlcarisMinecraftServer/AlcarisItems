package net.alcaris.plugin.items.commands;

import net.alcaris.plugin.core.AlcarisCore;
import net.alcaris.plugin.core.model.item.ItemBaseModel;
import net.alcaris.plugin.core.registry.ItemRegistry;
import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.converters.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class CustomItemCommand implements CommandExecutor, TabCompleter {
    private final AlcarisItems plugin;
    private final ItemRegistry itemRegistry;
    private final ItemConverter itemConverter;

    public CustomItemCommand(
            AlcarisItems plugin,
            FoodItemConverter foodItemConverter,
            ToolItemConverter toolItemConverter,
            MaterialItemConverter materialItemConverter,
            WeaponItemConverter weaponItemConverter
    ) {
        this.plugin = plugin;
        this.itemRegistry = ((AlcarisCore) Objects.requireNonNull(
                Bukkit.getPluginManager().getPlugin("AlcarisCore"),
                "AlcarisCore がロードされていません"
        )).getItemRegistry();

        this.itemConverter = new ItemConverter(foodItemConverter, toolItemConverter, materialItemConverter,weaponItemConverter);
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, String[] args
    ) {
        final String input = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1 -> Stream.of("give")
                    .filter(s -> s.startsWith(input))
                    .forEach(completions::add);
            case 2 -> {
                if (args[0].equalsIgnoreCase("give")) {
                    Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(n -> n.toLowerCase().startsWith(input))
                            .forEach(completions::add);
                }
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("give")) {
                    itemRegistry.getAll().stream()
                            .map(ItemBaseModel::getId)
                            .filter(id -> id.toLowerCase().startsWith(input))
                            .forEach(completions::add);
                }
            }
            case 4 -> {
                if (args[0].equalsIgnoreCase("give")) {
                    completions.add("<amount>");
                }
            }
        }

        return completions;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, String[] args
    ) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("give")) {
            sendUsage(sender);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("プレイヤーが見つかりません。")
                    .color(NamedTextColor.RED));
            return true;
        }

        String itemId = args[2];
        Optional<ItemBaseModel> optModel = itemRegistry.get(itemId);

        if (optModel.isEmpty()) {
            sender.sendMessage(Component.text("アイテム ID が無効です: " + itemId)
                    .color(NamedTextColor.RED));
            return true;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException ex) {
                sender.sendMessage(Component.text("無効な個数です。1 を使用します。")
                        .color(NamedTextColor.YELLOW));
            }
        }

        ItemStack stack = itemConverter.convert(optModel.get(), amount);
        if (stack == null) {
            sender.sendMessage(Component.text("ItemStack の生成に失敗しました。")
                    .color(NamedTextColor.RED));
            return true;
        }

        target.getInventory().addItem(stack);
        sender.sendMessage(Component.text()
                .append(plugin.prefix)
                .append(Component.text("アイテムを付与しました: " + itemId + " ×" + amount)
                        .color(NamedTextColor.GREEN)));

        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text()
                .append(plugin.prefix)
                .append(Component.text("使用法: /custom-item give <player> <id> [amount]")
                        .color(NamedTextColor.RED)));
    }
}
