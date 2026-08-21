package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RegionLevels;
import me.tayebyassine.homestead.managers.LevelManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.LevelsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs levels}) that opens the level overview of the current region.
 */
public final class LevelsSubCmd extends SubCommandBuilder {

    public LevelsSubCmd() {
        super("levels");
        setRegionPermission();
        setUsage("/hs levels");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (!Resources.<LevelsFile>get(ResourceType.Levels).isEnabled()) {
            Messages.send(player, "commands.levels.0");
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.levels.1");
            return true;
        }

        LevelManager.getOrCreateLevel(region.getUniqueId());

        new RegionLevels(player, region, player::closeInventory);

        return true;
    }
}



