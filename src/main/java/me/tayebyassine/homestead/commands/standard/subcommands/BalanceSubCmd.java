package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs balance}) that shows the bank balance of a region.
 */
public final class BalanceSubCmd extends SubCommandBuilder {

    public BalanceSubCmd() {
        super("balance");
        setRegionPermission();
        setUsage("/hs balance (region)");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (!Homestead.VAULT.isEconomyReady()) {
            Messages.send(player, "commands.balance.0");
            return true;
        }

        Region region;

        if (args.length >= 1) {
            region = RegionManager.findRegion(args[0]);

            if (region == null) {
                Messages.send(player, "commands.balance.1", args[0]);
                return true;
            }
        } else {
            region = TargetRegionSession.getRegion(player);

            if (region == null) {
                Messages.send(player, "commands.balance.2");
                return true;
            }
        }

        Messages.send(player, "commands.balance.3", region.getName(), Formatter.getBalance(region.getBank()));

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



