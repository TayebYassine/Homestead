package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionTrustPlayerEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableRent;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class TrustPlayerSubCmd extends SubCommandBuilder {
	public TrustPlayerSubCmd() {
		super("trust");
		setUsage("/region trust [player]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.players.trust")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 1) {
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
				RegionControlFlags.TRUST_PLAYERS)) {
			return true;
		}

		String targetName = args[0];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
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

		if (Resources.<RegionsFile>get(ResourceType.Regions).isInstantTrustSystemEnabled()) {
			region.removePlayerInvite(target);

			region.addMember(target);

			Messages.send(player, 199, new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", target.getName())
			);

			RegionTrustPlayerEvent _event = new RegionTrustPlayerEvent(region, player, target);
			Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));
		} else {
			region.addPlayerInvite(target);

			Placeholder placeholder = new Placeholder()
					.add("{region}", region.getName())
					.add("{playername}", target.getName())
					.add("{ownername}", region.getOwner().getName());

			Messages.send(player, 36, placeholder);

			if (target.isOnline()) {
				Messages.send(target.getPlayer(), 139, placeholder);
			}

			RegionManager.addNewLog(region.getUniqueId(), 2, new Placeholder()
					.add("{executor}", player.getName())
					.add("{playername}", target.getName())
			);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(Homestead.getInstance().getOfflinePlayersSync().stream().map(OfflinePlayer::getName).toList());
		}

		return suggestions;
	}
}
