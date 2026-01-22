package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferOwnershipSubCmd extends SubCommandBuilder {
	public TransferOwnershipSubCmd() {
		super("transfer");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 3) {
			PlayerUtils.sendMessage(sender, 0);
			return true;
		}

		String regionName = args[1];

		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			PlayerUtils.sendMessage(sender, 9);
			return true;
		}

		String playerName = args[2];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

		if (target == null) {
			Map<String, String> replacements = new HashMap<String, String>();
			replacements.put("{playername}", playerName);

			PlayerUtils.sendMessage(sender, 29, replacements);
			return true;
		}

		if (region.isOwner(target.getUniqueId())) {
			PlayerUtils.sendMessage(sender, 192);
			return false;
		}

		if (region.isPlayerBanned(target)) {
			region.unbanPlayer(target);
		}

		region.setOwner(target);

		if (region.isPlayerMember(target)) region.removeMember(target);
		if (region.isPlayerInvited(target)) region.removePlayerInvite(target);

		Map<String, String> replacements = new HashMap<>();
		replacements.put("{region}", region.getName());
		replacements.put("{player}", target.getName());

		PlayerUtils.sendMessage(sender, 193, replacements);

		return true;
	}
}
