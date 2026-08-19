package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

import java.util.List;

public class StorageSubCmd extends SubCommandBuilder {
	public StorageSubCmd() {
		super("storage");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.storage"
		));
		setUsage("/hs storage");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).isRegionStorageEnabled();

		if (!isEnabled) {
			Messages.send(player, "commands.storage.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.storage.1");
			return true;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player) && !MemberManager.isMemberOfRegion(region, player)) {
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
