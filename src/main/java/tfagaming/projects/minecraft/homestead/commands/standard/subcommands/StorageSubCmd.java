package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.storage.RegionStorage;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.List;

public class StorageSubCmd extends SubCommandBuilder {
	public StorageSubCmd() {
		super("storage");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.storage"
		));
		setUsage("/region storage");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).isRegionStorageEnabled();

		if (!isEnabled) {
			Messages.send(player, 105);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player) && !MemberManager.isMemberOfRegion(region, player)) {
			Messages.send(player, 30);
			return true;
		}

		if (!RegionStorage.hasStorage(region)) {
			RegionStorage.createStorage(region, Resources.<RegionsFile>get(ResourceType.Regions).getRegionStorageSize());
		}

		RegionStorage.openStorage(region, player);

		return true;
	}
}
