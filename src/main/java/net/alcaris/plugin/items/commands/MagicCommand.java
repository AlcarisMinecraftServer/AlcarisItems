package net.alcaris.plugin.items.commands;
import net.alcaris.plugin.items.magic.MagicRepository;
import net.alcaris.plugin.items.magic.MagicService;
import net.alcaris.plugin.items.magic.MagicSpell;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MagicCommand implements CommandExecutor, TabCompleter {
    private final MagicRepository repo;
    private final MagicService service;

    public MagicCommand(MagicRepository repo, MagicService service) {
        this.repo = repo;
        this.service = service;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Player only");
            return true;
        }
        if (args.length == 0) {
            p.sendMessage("/magic list | /magic select <id> | /magic mp <set|add> <amount>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> {
                p.sendMessage("Available spells:");
                for (MagicSpell s : repo.getAll()) {
                    p.sendMessage("- " + s.getId() + " (" + s.getDisplayName() + ")");
                }
            }
            case "select" -> {
                if (args.length < 2) {
                    p.sendMessage("/magic select <id>");
                    return true;
                }
                MagicSpell spell = repo.get(args[1]);
                if (spell == null) {
                    p.sendMessage("Unknown spell id");
                    return true;
                }
                service.setSelectedMagic(p, spell.getId());
                p.sendMessage("Selected spell: " + spell.getDisplayName());
            }
            case "mp" -> {
                if (args.length < 3) {
                    p.sendMessage("/magic mp <set|add> <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    if (args[1].equalsIgnoreCase("set")) {
                        service.setPlayerMp(p, amount);
                    } else if (args[1].equalsIgnoreCase("add")) {
                        service.setPlayerMp(p, service.getPlayerMp(p) + amount);
                    }
                    p.sendMessage("MP: " + service.getPlayerMp(p));
                } catch (NumberFormatException e) {
                    p.sendMessage("amount must be number");
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> res = new ArrayList<>();
        if (args.length == 1) {
            res.add("list");
            res.add("select");
            res.add("mp");
            return res;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("select")) {
            return repo.getAll().stream().map(MagicSpell::getId).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("mp")) {
            res.add("set");
            res.add("add");
            return res;
        }
        return res;
    }
}


