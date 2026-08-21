package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.PlayerInfo;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-command ({@code /hs player}) that opens the info menu of a player,
 * or of the sender when no player is specified.
 */
public final class PlayerInfoSubCmd extends SubCommandBuilder {

    public PlayerInfoSubCmd() {
        super("player");
        setRegionPermission();
        setUsage("/hs player [player]");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            new PlayerInfo(player, player, player::closeInventory);
            return true;
        }

        String playerName = args[0];

        OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

        if (target == null) {
            Messages.send(player, "commands.player.0", playerName);
            return true;
        }

        new PlayerInfo(player, target, player::closeInventory);

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
            suggestions.addAll(Homestead.getInstance().getOfflinePlayerNamesSync());
        }

        return suggestions;
    }
}



