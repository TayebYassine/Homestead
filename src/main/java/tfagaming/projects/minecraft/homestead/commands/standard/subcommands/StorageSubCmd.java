package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.storage.RegionStorage;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

public class StorageSubCmd extends SubCommandBuilder {
	public StorageSubCmd() {
		super("storage");
		setUsage("/region storage");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

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

		if (!PlayerUtility.isOperator(player) && !region.isOwner(player) && !region.isPlayerMember(player)) {
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
