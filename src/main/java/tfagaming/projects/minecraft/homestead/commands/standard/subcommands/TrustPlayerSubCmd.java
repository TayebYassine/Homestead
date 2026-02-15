package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class TrustPlayerSubCmd extends LegacySubCommandBuilder {
	public TrustPlayerSubCmd() {
		super("trust");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.players.trust")) {
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
				RegionControlFlags.TRUST_PLAYERS)) {
			return true;
		}

		String targetName = args[1];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			Map<String, String> replacements = new HashMap<String, String>();
			replacements.put("{playername}", targetName);

			Messages.send(player, 29, new Placeholder()
					.add("{playername}", targetName)
			);
			return true;
		}

		if (region.isPlayerBanned(target)) {
			Messages.send(player, 74);
			return true;
		}

		if (region.isPlayerMember(target)) {
			Messages.send(player, 48, new Placeholder()
					.add("{playername}", target.getName())
			);
			return true;
		}

		if (region.isPlayerInvited(target)) {
			Messages.send(player, 35, new Placeholder()
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

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			Messages.send(player, 116);
			return true;
		}

		if (Homestead.config.getBoolean("special-feat.ignore-trust-acceptance-system")) {
			region.removePlayerInvite(target);

			region.addMember(target);

			Messages.send(player, 199, new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", target.getName())
			);
		} else {
			region.addPlayerInvite(target);

			Messages.send(player, 36, new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", target.getName())
					.add("{ownername}", region.getOwner().getName())
			);

			if (target.isOnline()) {
				Messages.send(target.getPlayer(), 139, new Placeholder()
						.add("{region}", region.getName())
						.add("{playername}", target.getName())
						.add("{ownername}", region.getOwner().getName())
				);
			}

			// TODO Fix this
			// RegionsManager.addNewLog(region.getUniqueId(), 2, replacements);
		}

		return true;
	}
}
