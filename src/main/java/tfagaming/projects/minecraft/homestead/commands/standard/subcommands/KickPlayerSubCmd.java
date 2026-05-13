package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class KickPlayerSubCmd extends SubCommandBuilder {
	public KickPlayerSubCmd() {
		super("kick");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.kick"
		));
		setUsage("/region kick [player]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			reply(player, "kick.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			reply(player, "kick.1");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.KICK_PLAYERS)) {
			reply(player, "kick.2");
			return true;
		}

		String targetName = args[0];

		Player target = Bukkit.getPlayer(targetName);

		if (target == null) {
			reply(player, "kick.3");
			return true;
		}

		if (BanManager.isBanned(region, target)) {
			reply(player, "kick.4");
			return true;
		}

		if (region.isOwner(target) || target.getUniqueId().equals(player.getUniqueId())) {
			reply(player, "kick.5");
			return true;
		}

		SeRent rent = region.getRent();

		if (rent != null && rent.getRenterId().equals(target.getUniqueId())) {
			reply(player, "kick.6");
			return true;
		}

		if (!RegionManager.isPlayerInsideRegion(target, region)) {
			reply(player, "kick.7");
			return true;
		}

		Chunk chunk = ChunkUtility.findNearbyUnclaimedChunk(target.getLocation(), 64);

		if (chunk != null) {
			PlayerUtility.teleportPlayerToChunk(target, chunk);
		}

		reply(player, "kick.8", targetName);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(Homestead.getInstance().getOnlinePlayerNamesSync());
		}

		return suggestions;
	}
}
