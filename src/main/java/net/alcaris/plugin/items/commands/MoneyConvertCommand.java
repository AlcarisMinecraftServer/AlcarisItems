package net.alcaris.plugin.items.commands;

import net.alcaris.plugin.items.AlcarisItems;
import net.alcaris.plugin.items.lib.ItemsRepository;
import jp.jyn.jecon.Jecon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * /money-convert で 1000G を money_1000 アイテムに変換するコマンド (最大64個)。
 */
public class MoneyConvertCommand implements CommandExecutor {

    private static final int UNIT_PRICE = 1000;
    private static final int MAX_STACK = 64;

    private final AlcarisItems plugin;
    private final ItemsRepository repository;

    public MoneyConvertCommand(AlcarisItems plugin, ItemsRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("プレイヤーのみ使用できます。", NamedTextColor.RED));
            return true;
        }

        Jecon jecon = plugin.getJecon();
        if (jecon == null) {
            player.sendMessage(Component.text("Jecon が利用できません。", NamedTextColor.RED));
            return true;
        }

        java.util.Optional<java.math.BigDecimal> optBal = jecon.getRepository().getDecimal(player.getUniqueId());
        BigDecimal balance = optBal.orElse(BigDecimal.ZERO);

        int affordable = balance.divide(BigDecimal.valueOf(UNIT_PRICE)).intValue();
        if (affordable <= 0) {
            player.sendMessage(Component.text("残高が不足しています。", NamedTextColor.RED));
            return true;
        }
        int itemAmount = Math.min(affordable, MAX_STACK);

        BigDecimal deduct = BigDecimal.valueOf((long) itemAmount * UNIT_PRICE);
        jecon.getRepository().set(player.getUniqueId(), balance.subtract(deduct));

        // give item
        var itemStack = repository.createItem("money_1000", itemAmount);
        if (itemStack == null) {
            player.sendMessage(Component.text("money_1000 アイテムが見つかりません。", NamedTextColor.RED));
            // refund money
            jecon.getRepository().set(player.getUniqueId(), balance);
            return true;
        }
        player.getInventory().addItem(itemStack);
        player.sendMessage(Component.text(itemAmount + "個の小切手を受け取りました！", NamedTextColor.GREEN));
        return true;
    }
}