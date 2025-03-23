package net.alcaris.plugin.items.listeners;

import net.alcaris.plugin.items.AlcarisItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AdminAlertListener implements Listener {
    private final AlcarisItems plugin;
    private final boolean isUsingCacheFallback;

    public AdminAlertListener(AlcarisItems plugin, boolean isUsingCacheFallback) {
        this.plugin = plugin;
        this.isUsingCacheFallback = isUsingCacheFallback;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!isUsingCacheFallback) return;

        Player player = event.getPlayer();
        if (player.hasPermission("alcaris_items.alert")) {
            player.sendMessage(
                    plugin.prefix.append(
                            Component.text("最新のデータが取得できませんでした、古いデータを使用します。")
                                    .color(NamedTextColor.RED)
                    )
            );
        }
    }
}
