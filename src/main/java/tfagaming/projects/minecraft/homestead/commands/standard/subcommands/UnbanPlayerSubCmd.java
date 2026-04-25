package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.RegionUnbanPlayerEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;


import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class UnbanPlayerSubCmd extends SubCommandBuilder {
	public UnbanPlayerSubCmd() {
		super("unban");
		setUsage("/region unban [player]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.players.unban")) {
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

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.UNBAN_PLAYERS)) {
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

		if (!BannedPlayerManager.isBanned(region, target)) {
			Messages.send(player, 33, new Placeholder()
					.add("{playername}", target.getName())
			);
			return true;
		}

		region.unbanPlayer(target);

		Messages.send(player, 34, new Placeholder()
				.add("{region}", region.getName())
				.add("{playername}", target.getName())
		);

		RegionUnbanPlayerEvent _event = new RegionUnbanPlayerEvent(region, player, target);
		Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(_event));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			Region region = TargetRegionSession.getRegion(player);

			if (region != null) {
				for (SerializableBannedPlayer each : region.getBannedPlayers()) {
					OfflinePlayer p = each.bukkit();

					if (p != null) {
						suggestions.add(p.getName());
					}
				}
			}
		}

		return suggestions;
	}
}
