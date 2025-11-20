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
    private final WeaponItemConverter weaponItemConverter;
    private final ArmorItemConverter armorItemConverter;

    public CustomItemCommand(
            AlcarisItems plugin,
            WeaponItemConverter weaponItemConverter,
            ArmorItemConverter armorItemConverter
    ) {
        this.plugin = plugin;
        this.itemRegistry = ((AlcarisCore) Objects.requireNonNull(
                Bukkit.getPluginManager().getPlugin("AlcarisCore"),
                "AlcarisCore がロードされていません"
        )).getItemRegistry();
        this.weaponItemConverter = weaponItemConverter;
        this.armorItemConverter = armorItemConverter;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, String[] args
    ) {
        final String input = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            Stream.of("give", "upgrade")
                    .filter(s -> s.startsWith(input))
                    .forEach(completions::add);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("give")) {
                Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(n -> n.toLowerCase().startsWith(input))
                        .forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("upgrade")) {
                Stream.of("weapon", "armor")
                        .filter(s -> s.startsWith(input))
                        .forEach(completions::add);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                itemRegistry.getAll().stream()
                        .map(ItemBaseModel::getId)
                        .filter(id -> id.toLowerCase().startsWith(input))
                        .forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("upgrade") && args[1].equalsIgnoreCase("weapon")) {
                Stream.of(WeaponItemConverter.UpgradeType.values())
                        .map(Enum::name)
                        .filter(s -> s.toLowerCase().startsWith(input))
                        .forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("upgrade") && args[1].equalsIgnoreCase("armor")) {
                Stream.of(ArmorItemConverter.UpgradeType.values())
                        .map(Enum::name)
                        .filter(s -> s.toLowerCase().startsWith(input))
                        .forEach(completions::add);
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("give")) {
                completions.add("<amount>");
            } else if (args[0].equalsIgnoreCase("upgrade") && args[1].equalsIgnoreCase("weapon")) {
                Stream.of("damage", "walkSpeed", "attackRange", "attackSpeed", "xpBonus", "lootBonus")
                        .filter(s -> s.toLowerCase().startsWith(input))
                        .forEach(completions::add);
            } else if (args[0].equalsIgnoreCase("upgrade") && args[1].equalsIgnoreCase("armor")) {
                Stream.of("hp", "hpr", "mp", "mpr", "atk", "def", "mat", "mdf", "dex", "speed")
                        .filter(s -> s.toLowerCase().startsWith(input))
                        .forEach(completions::add);
            }
        }

        return completions;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, String[] args
    ) {
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> handleGiveCommand(sender, args);
            case "upgrade" -> handleUpgradeCommand(sender, args);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("プレイヤーが見つかりません。")
                    .color(NamedTextColor.RED));
            return;
        }

        String itemId = args[2];
        Optional<ItemBaseModel> optModel = itemRegistry.get(itemId);

        if (optModel.isEmpty()) {
            sender.sendMessage(Component.text("アイテム ID が無効です: " + itemId)
                    .color(NamedTextColor.RED));
            return;
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

        ItemStack stack = AlcarisItems.getItemConverter().convert(optModel.get(), amount);
        if (stack == null || stack.getType().isAir()) {
            sender.sendMessage(Component.text("ItemStack の生成に失敗しました。")
                    .color(NamedTextColor.RED));
            return;
        }

        target.getInventory().addItem(stack);
        sender.sendMessage(Component.text()
                .append(plugin.prefix)
                .append(Component.text("アイテムを付与しました: " + itemId + " ×" + amount)
                        .color(NamedTextColor.GREEN)));
    }

    private void handleUpgradeCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("このコマンドはプレイヤーのみ使用できます。")
                    .color(NamedTextColor.RED));
            return;
        }

        if (args.length < 3) {
            sendUpgradeUsage(sender);
            return;
        }

        if (!args[1].equalsIgnoreCase("weapon") && !args[1].equalsIgnoreCase("armor")) {
            sendUpgradeUsage(sender);
            return;
        }

        try {
            String upgradeTypeStr = args[2].toUpperCase();
            String specificStat = args.length >= 4 ? args[3].toLowerCase() : null;

            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType().isAir()) {
                player.sendMessage(Component.text("アイテムを手に持ってください。")
                        .color(NamedTextColor.RED));
                return;
            }

            boolean success = false;
            if (args[1].equalsIgnoreCase("weapon")) {
                WeaponItemConverter.UpgradeType upgradeType;
                try {
                    upgradeType = WeaponItemConverter.UpgradeType.valueOf(upgradeTypeStr);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Component.text("無効なアップグレードタイプです。使用可能なタイプ: ROLL_ALL, FIXED_INCREMENT, ROLL_SPECIFIC, ROLL_AND_KEEP_HIGH")
                            .color(NamedTextColor.RED));
                    return;
                }
                success = weaponItemConverter.upgradeWeaponPerformance(heldItem, upgradeType, specificStat);
            } else if (args[1].equalsIgnoreCase("armor")) {
                ArmorItemConverter.UpgradeType upgradeType;
                try {
                    upgradeType = ArmorItemConverter.UpgradeType.valueOf(upgradeTypeStr);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Component.text("無効なアップグレードタイプです。使用可能なタイプ: ROLL_ALL, FIXED_INCREMENT, ROLL_SPECIFIC, ROLL_AND_KEEP_HIGH")
                            .color(NamedTextColor.RED));
                    return;
                }
                success = armorItemConverter.upgradeArmorPerformance(heldItem, upgradeType, specificStat);
            }

            if (success) {
                String itemType = args[1].equalsIgnoreCase("weapon") ? "武器" : "防具";
                player.sendMessage(Component.text()
                        .append(plugin.prefix)
                        .append(Component.text(itemType + "の性能値をアップグレードしました。")
                                .color(NamedTextColor.GREEN)));
            } else {
                String itemType = args[1].equalsIgnoreCase("weapon") ? "武器" : "防具";
                player.sendMessage(Component.text()
                        .append(plugin.prefix)
                        .append(Component.text(itemType + "の性能値アップグレードに失敗しました。最大改造回数を確認してください。")
                                .color(NamedTextColor.RED)));
            }
        } catch (Exception e) {
            String itemType = args[1].equalsIgnoreCase("weapon") ? "武器" : "防具";
            player.sendMessage(Component.text(itemType + "のアップグレードに失敗しました: " + e.getMessage())
                    .color(NamedTextColor.RED));
            sendUpgradeUsage(sender);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text()
                .append(plugin.prefix)
                .append(Component.text("使用法: /custom-item give <player> <id> [amount]")
                        .color(NamedTextColor.RED)));
    }

    private void sendUpgradeUsage(CommandSender sender) {
        sender.sendMessage(Component.text()
                .append(plugin.prefix)
                .append(Component.text("使用法: /custom-item upgrade <weapon|armor> <UpgradeType> [specificStat]")
                        .color(NamedTextColor.RED)));
        sender.sendMessage(Component.text("UpgradeType: ROLL_ALL, FIXED_INCREMENT, ROLL_SPECIFIC, ROLL_AND_KEEP_HIGH")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("武器のspecificStat: damage, walkSpeed, attackRange, attackSpeed, xpBonus, lootBonus")
                .color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("防具のspecificStat: hp, hpr, mp, mpr, atk, def, mat, mdf, dex, speed")
                .color(NamedTextColor.YELLOW));
    }
}
