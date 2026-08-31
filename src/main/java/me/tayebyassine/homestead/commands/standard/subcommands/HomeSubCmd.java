package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.DelayedTeleport;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs home}) that teleports the player to the current region's spawn point.
 */
public final class HomeSubCmd extends SubCommandBuilder {

    public HomeSubCmd() {
        super("home");
        setRegionPermission("homestead.actions.regions.teleport");
        setUsage("/hs home");
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
            Messages.send(player, "commands.home.0");
            return true;
        }

        SeLocation loc = region.getLocation();

        if (loc == null) {
            Messages.send(player, "commands.home.1");
            return true;
        }

        if (!PlayerUtility.isOperator(player)
                && !region.isOwner(player)
                && !(
                PlayerUtility.hasPermissionFlag(region, player, PlayerFlag.TELEPORT_SPAWN, false)
                        && PlayerUtility.hasPermissionFlag(region, player, PlayerFlag.PASSTHROUGH, false)
        )) {
            Messages.send(player, "commands.home.2");
            return true;
        }

        new DelayedTeleport(player, loc.toBukkit());

        return true;
    }
}
