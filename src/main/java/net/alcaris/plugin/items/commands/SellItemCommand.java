package net.alcaris.plugin.items.commands;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.lib.ItemsRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /item-sell コマンド実装。プレイヤーがGUIを開いてアイテムを売却できる。
 */
public class SellItemCommand implements CommandExecutor {

    private final AlcarisItems plugin;
    private final ItemsRepository repository;

    public SellItemCommand(AlcarisItems plugin, ItemsRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ使用できます。", NamedTextColor.RED));
            return true;
        }

        repository.openItemSellGUI(player);
        return true;
    }
}