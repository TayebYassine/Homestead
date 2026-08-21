package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RegionRating;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs rate}) that opens the rating menu of a region.
 */
public final class RateRegionSubCmd extends SubCommandBuilder {

    public RateRegionSubCmd() {
        super("rate");
        setRegionPermission("homestead.actions.regions.rate");
        setUsage("/hs rate [region]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.rate.0");
            return true;
        }

        String regionName = args[0];

        Region region = RegionManager.findRegion(regionName);

        if (region == null) {
            Messages.send(player, "commands.rate.1", regionName);
            return true;
        }

        new RegionRating(player, region, player::closeInventory);

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



