package me.tayebyassine.homestead.listeners.player;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.UUID;

/**
 * Applies a per-player cooldown to Homestead commands to prevent spam, based on the configured
 * {@code COMMANDS_COOLDOWN} limit.
 */
public final class CommandsCooldownListener implements Listener {
    private static final HashSet<UUID> COOLDOWN = new HashSet<>();

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        PluginCommand pluginCommand = Homestead.getInstance().getServer().getPluginCommand(command);

        if (pluginCommand != null && pluginCommand.getPlugin().equals(Homestead.getInstance())) {
            Player player = event.getPlayer();

            if (COOLDOWN.contains(player.getUniqueId())) {
                event.setCancelled(true);

                Messages.send(player, "common.cooldown_command");
            } else {
                int cooldownPlayer = Limits.getPlayerLimit(player, Limits.LimitType.COMMANDS_COOLDOWN);

                if (cooldownPlayer > 0) {
                    COOLDOWN.add(player.getUniqueId());

                    Homestead.getInstance().runAsyncTaskLater(() -> {
                        COOLDOWN.remove(player.getUniqueId());
                    }, cooldownPlayer);
                }
            }
        }
    }
}
