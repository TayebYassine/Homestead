package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.gui.menus.RegionsWithWelcomeSigns;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.java.NumberUtils;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.DelayedTeleport;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs visit}) that teleports to a region's welcome sign or spawn,
 * or opens the welcome signs menu when no region is specified.
 */
public final class VisitRegionSubCmd extends SubCommandBuilder {

    public VisitRegionSubCmd() {
        super("visit");
        setRegionPermission("homestead.actions.regions.teleport");
        setUsage("/hs visit [region/playername] (index)");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (Resources.<RegionsFile>get(ResourceType.Regions).isWelcomeSignEnabled()) {
            return visitByWelcomeSigns(player, args);
        }

        return visitBySpawn(player, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            if (Resources.<RegionsFile>get(ResourceType.Regions).isWelcomeSignEnabled()) {
                suggestions.addAll(RegionManager.getPlayersWithRegionsHasWelcomeSigns().stream()
                        .map(OfflinePlayer::getName).toList());
            } else if (PlayerUtility.isOperator(player)) {
                suggestions.addAll(RegionManager.getRegionNames());
            } else {
                suggestions.addAll(RegionManager.getPublicRegions().stream().map(Region::getName).toList());
            }
        } else if (args.length == 2 && Resources.<RegionsFile>get(ResourceType.Regions).isWelcomeSignEnabled()) {
            for (int i = 0; i < RegionManager.getPlayersWithRegionsHasWelcomeSigns().size(); i++) {
                suggestions.add(String.valueOf(i));
            }
        }

        return suggestions;
    }

    private boolean visitByWelcomeSigns(Player player, String[] args) {
        if (args.length < 1) {
            new RegionsWithWelcomeSigns(player);

            return true;
        }

        String playerName = args[0];

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

        if (target == null) {
            Messages.send(player, "commands.visit.0", playerName);
            return true;
        }

        String indexInput = args.length >= 2 ? args[1] : "0";

        if (!NumberUtils.isValidInteger(indexInput)) {
            Messages.send(player, "commands.visit.1");
            return true;
        }

        int index = Integer.parseInt(indexInput);

        List<Region> filteredRegions = RegionManager.getRegionsOwnedByPlayer(target).stream()
                .filter(region -> region.getWelcomeSign() != null)
                .toList();

        if (filteredRegions.isEmpty()) {
            Messages.send(player, "commands.visit.2");
            return true;
        }

        if (index < 0 || index > filteredRegions.size() - 1) {
            Messages.send(player, "commands.visit.3");
            return true;
        }

        SeLocation sign = filteredRegions.get(index).getWelcomeSign();

        if (sign == null) {
            return true;
        }

        new DelayedTeleport(player, sign.toBukkit());

        return true;
    }

    private boolean visitBySpawn(Player player, String[] args) {
        if (args.length < 1) {
            Messages.send(player, "commands.visit.4");
            return true;
        }

        String regionName = args[0];

        Region region = RegionManager.findRegion(regionName);

        if (region == null) {
            Messages.send(player, "commands.visit.5", regionName);
            return true;
        }

        if (region.getLocation() == null) {
            Messages.send(player, "commands.visit.6");
            return true;
        }

        if (!PlayerUtility.isOperator(player)
                && !region.isOwner(player)
                && !(PlayerUtility.hasPermissionFlag(region, player, PlayerFlag.TELEPORT_SPAWN, false)
                && PlayerUtility.hasPermissionFlag(region, player, PlayerFlag.PASSTHROUGH, false))) {
            Messages.send(player, "commands.visit.7");
            return true;
        }

        new DelayedTeleport(player, region.getLocation().toBukkit());

        return true;
    }
}




