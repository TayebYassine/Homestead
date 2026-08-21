package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionChatEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.PrivateChatSession;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Sub-command ({@code /hs chat}) that toggles private region chat or sends a message
 * to the current region's members.
 */
public final class ChatSubCmd extends SubCommandBuilder {

    public ChatSubCmd() {
        super("chat");
        setRegionPermission("homestead.actions.regions.chat");
        setUsage("/hs chat [message]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.chat.0");
            return true;
        }

        if (args.length < 1) {
            if (PrivateChatSession.hasSession(player)) {
                PrivateChatSession.removeSession(player);

                Messages.send(player, "commands.chat.2");
            } else {
                PrivateChatSession.newSession(player);

                Messages.send(player, "commands.chat.1");
            }

            return true;
        }

        String message = String.join(" ", Arrays.asList(args));

        if (ColorTranslator.containsMiniMessageTag(message)) {
            Messages.send(player, "commands.chat.3");
            return true;
        }

        RegionManager.sendPrivateChat(region, player, message);

        Homestead.callEvent(new RegionChatEvent(region, player, message));

        return true;
    }
}



