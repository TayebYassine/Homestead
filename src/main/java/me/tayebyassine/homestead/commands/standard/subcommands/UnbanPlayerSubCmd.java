package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.UnbanPlayerEvent;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionBan;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs unban}) that unbans a player from the current region.
 */
public final class UnbanPlayerSubCmd extends SubCommandBuilder {

    public UnbanPlayerSubCmd() {
        super("unban");
        setRegionPermission("homestead.actions.regions.players.unban");
        setUsage("/hs unban [player]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            Messages.send(player, "commands.unban.0");
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.unban.1");
            return true;
        }

        if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
                ControlFlags.UNBAN_PLAYERS)) {
            return true;
        }

        String targetName = args[0];

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

        if (target == null) {
            Messages.send(player, "commands.unban.2", targetName);
            return true;
        }

        if (!BanManager.isBanned(region, target)) {
            Messages.send(player, "commands.unban.3");
            return true;
        }

        BanManager.unbanPlayer(region, target);

        Messages.send(player, "commands.unban.4");

        LogManager.addLog(region, player, LogManager.PredefinedLog.UNBAN_PLAYER, target.getName());

        Homestead.callEvent(new UnbanPlayerEvent(region, player));

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
                for (RegionBan ban : BanManager.getBansOfRegion(region)) {
                    suggestions.add(ban.getPlayerName());
                }
            }
        }

        return suggestions;
    }
}



