package net.alcaris.plugin.items.commands;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.lib.ItemsRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Console-only command that applies item loss to a specified player by invoking
 * ItemsRepository#applyItemLoss(Player).
 *
 * Usage (console): /item-loss <player>
 */
public class ItemLossCommand implements CommandExecutor, TabCompleter {

    private final AlcarisItems plugin;
    private final ItemsRepository repository;

    public ItemLossCommand(AlcarisItems plugin) {
        this.plugin = plugin;
        this.repository = AlcarisItems.getRepository();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Restrict to console only
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(Component.text("このコマンドはコンソールからのみ実行できます。")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(Component.text("使用法: /" + label + " <player>")
                    .color(NamedTextColor.YELLOW));
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            sender.sendMessage(Component.text("プレイヤーが見つかりません: " + playerName)
                    .color(NamedTextColor.RED));
            return true;
        }

        repository.applyItemLoss(target);
        sender.sendMessage(Component.text()
                .append(plugin.prefix)
                .append(Component.text("アイテムロストを適用しました: " + target.getName())
                        .color(NamedTextColor.GREEN)));
        return true;
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .forEach(completions::add);
        }
        return completions;
    }
}
