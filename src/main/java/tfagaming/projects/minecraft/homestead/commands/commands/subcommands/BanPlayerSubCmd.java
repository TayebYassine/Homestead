package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.Arrays;
import java.util.List;

public class BanPlayerSubCmd extends SubCommandBuilder {
	public BanPlayerSubCmd() {
		super("ban");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.players.ban")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.BAN_PLAYERS)) {
			return true;
		}

		String targetName = args[1];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			Messages.send(player, 29, new Placeholder()
					.add("{playername}", targetName)
			);
			return true;
		}

		if (region.isPlayerBanned(target)) {
			Messages.send(player, 32, new Placeholder()
					.add("{region}", region.getName())
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

		String reason = Homestead.language.getString("default.reason");

		if (args.length > 2) {
			List<String> reasonList = Arrays.asList(args).subList(2, args.length);
			reason = String.join(" ", reasonList);
		}

		region.banPlayer(target, reason);

		Messages.send(player, 31, new Placeholder()
				.add("{region}", region.getName())
				.add("{playername}", target.getName())
				.add("{region}", reason)
		);

		if (region.isPlayerMember(target)) {
			region.removeMember(target);
		}

		if (region.isPlayerInvited(target)) {
			region.removePlayerInvite(target);
		}

		return true;
	}
}
