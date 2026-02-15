package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class KickPlayerSubCmd extends SubCommandBuilder {
	public KickPlayerSubCmd() {
		super("kick");
		setUsage("/region kick [player]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.players.kick")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.KICK_PLAYERS)) {
			return true;
		}

		String targetName = args[0];

		Player target = Bukkit.getPlayer(targetName);

		if (target == null) {
			Messages.send(player, 29, new Placeholder()
					.add("{playername}", targetName)
			);
			return true;
		}

		if (region.isPlayerBanned(target)) {
			Messages.send(player, 32, new Placeholder()
					.add("{playername}", target.getName())
			);
			return true;
		}

		if (region.isOwner(target)) {
			Messages.send(player, 30);
			return true;
		}

		SerializableRent rent = region.getRent();

		if (rent != null && rent.getPlayerId().equals(target.getUniqueId())) {
			Messages.send(player, 196);
			return true;
		}

		if (!RegionsManager.isPlayerInsideRegion(target, region)) {
			Messages.send(player, 143);
			return true;
		}

		Chunk chunk = ChunkUtils.findNearbyUnclaimedChunk(target);

		if (chunk != null) {
			PlayerUtils.teleportPlayerToChunk(target, chunk);
		}

		Messages.send(player, 144, new Placeholder()
				.add("{region}", region.getName())
				.add("{playername}", target.getName())
		);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 0) {

		}

		return suggestions;
	}
}
