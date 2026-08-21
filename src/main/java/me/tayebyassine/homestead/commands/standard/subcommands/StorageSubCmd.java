package me.tayebyassine.homestead.commands.standard.subcommands;

import me.tayebyassine.homestead.commands.CommandSenderType;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.storage.RegionStorage;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Sub-command ({@code /hs storage}) that opens the storage of the current region.
 */
public final class StorageSubCmd extends SubCommandBuilder {

    public StorageSubCmd() {
        super("storage");
        setRegionPermission("homestead.actions.regions.storage");
        setUsage("/hs storage");
        setAllowedCommandSenders(CommandSenderType.PLAYER);
    }

    @Override
    public boolean onExecution(CommandSender sender, String[] args) {
        Player player = asPlayer(sender);
        if (player == null) {
            return false;
        }

        if (!Resources.<RegionsFile>get(ResourceType.Regions).isRegionStorageEnabled()) {
            Messages.send(player, "commands.storage.0");
            return true;
        }

        Region region = TargetRegionSession.getRegion(player);

        if (region == null) {
            Messages.send(player, "commands.storage.1");
            return true;
        }

        if (!PlayerUtility.isOperator(player)
                && !region.isOwner(player)
                && !MemberManager.isMemberOfRegion(region, player)) {
            Messages.send(player, "commands.storage.2");
            return true;
        }

        if (!RegionStorage.hasStorage(region)) {
            RegionStorage.createStorage(region, Resources.<RegionsFile>get(ResourceType.Regions).getRegionStorageSize());
        }

        RegionStorage.openStorage(region, player);

        return true;
    }
}



