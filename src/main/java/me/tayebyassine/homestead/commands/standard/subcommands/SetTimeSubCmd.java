package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.weatherandtime.RegionTime;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs settime}) that sets the time of the current region.
 */
public final class SetTimeSubCmd extends SubCommandBuilder {

    public SetTimeSubCmd() {
        super("settime");
        setRegionPermission("homestead.actions.regions.update.time");
        setUsage("/hs settime [time]");
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
            Messages.send(player, "commands.settime.0");
            return true;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.settime.1");
            return true;
        }

        String timeInput = args[0];
        int time = RegionTime.parse(timeInput);

        if (time == -1) {
            Messages.send(player, "commands.settime.2");
            return true;
        }

        region.setTime(time);

        Messages.send(player, "commands.settime.3", timeInput);

        LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_TIME, RegionTime.from(time));

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
            suggestions.addAll(RegionTime.getAll());
        }

        return suggestions;
    }
}



