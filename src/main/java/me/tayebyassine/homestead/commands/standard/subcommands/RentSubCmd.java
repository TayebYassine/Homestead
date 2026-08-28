package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RentConfigMenu;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.SubArea;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs rent}) that configures renting for the current region or a specific sub-area.
 */
public final class RentSubCmd extends SubCommandBuilder {

    public RentSubCmd() {
        super("rent");
        setRegionPermission();
        setUsage("/hs rent [subarea-name]");
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
            Messages.send(player, "commands.rent.0");
            return true;
        }

        if (!region.isOwner(player)) {
            Messages.send(player, "commands.rent.15");
            return true;
        }

        if (!Homestead.VAULT.isEconomyReady()) {
            Messages.send(player, "common.economy_disabled");
            return true;
        }

        if (!Resources.<RegionsFile>get(ResourceType.Regions).isRentingEnabled()) {
            Messages.send(player, "commands.rent.1");
            return true;
        }

        if (args.length == 0) {
            new RentConfigMenu(player, region, null);
            return true;
        }

        String subAreaName = args[0];
        SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), subAreaName);

        if (subArea == null) {
            Messages.send(player, "commands.rent.2", subAreaName);
            return true;
        }

        new RentConfigMenu(player, region, subArea);
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
            Region region = TargetRegionSession.getRegion(player);
            if (region != null) {
                suggestions.addAll(SubAreaManager.getSubAreasOfRegion(region).stream().map(SubArea::getName).toList());
            }
        }

        return suggestions;
    }
}