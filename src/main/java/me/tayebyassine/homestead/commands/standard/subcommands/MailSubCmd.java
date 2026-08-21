package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.PlayerMailEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.minecraft.chat.ColorTranslator;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sub-command ({@code /hs mail}) that sends a mail message to a region.
 */
public final class MailSubCmd extends SubCommandBuilder {

    private static final int MAX_MAILS_PER_PLAYER = 10;

    public MailSubCmd() {
        super("mail");
        setRegionPermission("homestead.actions.regions.mail");
        setUsage("/hs mail [region] [message]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 2) {
            Messages.send(player, "commands.mail.0");
            return true;
        }

        Region region = RegionManager.findRegion(args[0]);

        if (region == null) {
            Messages.send(player, "commands.mail.1", args[0]);
            return true;
        }

        long playerMails = LogManager.getUnreadLogs(region).stream()
                .filter(log -> log.getAuthor().equals(player.getName()))
                .count();

        if (playerMails >= MAX_MAILS_PER_PLAYER) {
            Messages.send(player, "commands.mail.2");
            return true;
        }

        String message = String.join(" ", Arrays.asList(args).subList(1, args.length));

        if (ColorTranslator.containsMiniMessageTag(message)) {
            Messages.send(player, "commands.mail.3");
            return true;
        }

        LogManager.addLog(region, player.getName(), message);

        Messages.send(player, "commands.mail.4");

        Homestead.callEvent(new PlayerMailEvent(region, player, message));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(RegionManager.getRegionNames());
        }

        return suggestions;
    }
}



