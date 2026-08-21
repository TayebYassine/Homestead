package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.RegionCreateEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.StringUtils;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs create}) that creates a new region owned by the player.
 */
public final class CreateRegionSubCmd extends SubCommandBuilder {

    public CreateRegionSubCmd() {
        super("create");
        setRegionPermission("homestead.actions.regions.create");
        setUsage("/hs create [name]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.create.0");
            return true;
        }

        if (Limits.hasReachedLimit(player, null, Limits.LimitType.REGIONS)) {
            Messages.send(player, "commands.create.3");
            return true;
        }

        String regionName = args[0];

        if (!StringUtils.isValidRegionName(regionName)) {
            Messages.send(player, "commands.create.1");
            return true;
        }

        if (RegionManager.isNameUsed(regionName)) {
            Messages.send(player, "commands.create.2");
            return true;
        }

        Region region = RegionManager.createRegion(regionName, player);

        Messages.send(player, "commands.create.4", regionName);

        TargetRegionSession.newSession(player, region);

        Homestead.callEvent(new RegionCreateEvent(region, player));

        return true;
    }
}



