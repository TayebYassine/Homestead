package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.borders.ChunkBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs borders}) that toggles the visual display of region chunk borders.
 */
public final class BordersSubCmd extends SubCommandBuilder {

    public BordersSubCmd() {
        super("borders");
        setRegionPermission();
        setUsage("/hs borders (stop)");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (!Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled()) {
            Messages.send(player, "commands.borders.0");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            ChunkBorder.stop(player);

            Messages.send(player, "commands.borders.1");

            return true;
        }

        ChunkBorder.show(player);

        Messages.send(player, "commands.borders.2");

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
            suggestions.add("stop");
        }

        return suggestions;
    }
}



