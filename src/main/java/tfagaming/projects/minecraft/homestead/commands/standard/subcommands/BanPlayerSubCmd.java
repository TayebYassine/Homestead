package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.BanPlayerEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionBan;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BanPlayerSubCmd extends SubCommandBuilder {
	public BanPlayerSubCmd() {
		super("ban");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.players.ban"
		));
		setUsage("/hs ban [player] (reason)");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			Messages.send(player, "commands.ban.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.ban.1");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.BAN_PLAYERS)) {
			return true;
		}

		String targetName = args[0];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			Messages.send(player, "commands.ban.3", targetName);
			return true;
		}

		if (region.isOwner(target) || PlayerUtility.equals(player, target)) {
			Messages.send(player, "commands.ban.4");
			return true;
		}

		RegionBan ban = BanManager.getBannedPlayer(region, target);

		if (ban != null) {
			Messages.send(player, "commands.ban.5", targetName, ban.getReason());
			return true;
		}

		SeRent rent = region.getRent();

		if (rent != null && rent.isRenterer(target)) {
			Messages.send(player, "commands.ban.6");
			return true;
		}

		String reason = Resources.<LanguageFile>get(ResourceType.Language).getString("common.default.reason");

		if (args.length > 1) {
			List<String> reasonList = Arrays.asList(args).subList(1, args.length);
			reason = String.join(" ", reasonList);
		}

		if (ColorTranslator.containsMiniMessageTag(reason)) {
			Messages.send(player, "commands.ban.7");
			return true;
		}

		Player targetOnline = target.isOnline() ? target.getPlayer() : null;

		if (targetOnline != null && RegionManager.isPlayerInsideRegion(targetOnline, region)) {
			Chunk chunk = ChunkUtility.findNearbyUnclaimedChunk(targetOnline.getLocation(), 64);

			if (chunk != null) {
				PlayerUtility.teleportPlayerToChunk(targetOnline, chunk);
			}
		}

		BanManager.banPlayer(region, target, reason);
		LogManager.addLog(region, player, LogManager.PredefinedLog.BAN_PLAYER, target.getName());

		Messages.send(player, "commands.ban.8", targetName, region.getName(), reason);

		Homestead.callEvent(new BanPlayerEvent(region, target, reason));

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
