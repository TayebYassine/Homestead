package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.War;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarSubCmd extends SubCommandBuilder {
	public WarSubCmd() {
		super("war");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.war"
		));
		setUsage("/hs war [action] (params)");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("wars.enabled");

		if (!isEnabled) {
			Messages.send(player, "commands.war.1");
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, "commands.war.2", getUsage());
			return true;
		}

		if (!Homestead.VAULT.isEconomyReady()) {
			Messages.send(player, "commands.war.3");

			Logger.warning(Logger.PredefinedMessage.ECONOMY_INTEGRATION_DISABLED);

			return true;
		}

		switch (args[0]) {
			case "declare": {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, "commands.war.4");
					return true;
				}

				if (WarManager.isRegionInWar(region.getUniqueId())) {
					Messages.send(player, "commands.war.5");
					return true;
				}

				if (args.length < 4) {
					Messages.send(player, "commands.war.2", "/hs war declare [target] [prize] (war name)");
					return true;
				}

				String targetRegionName = args[1];
				Region targetRegion = RegionManager.findRegion(targetRegionName);

				if (targetRegion == null) {
					Messages.send(player, "commands.war.6");
					return true;
				}

				if (!region.isOwner(player) && MemberManager.isMemberOfRegion(region, player)) {
					Messages.send(player, "commands.war.7");
					return true;
				}

				if (region.getUniqueId() == targetRegion.getUniqueId() || region.isOwner(targetRegion.getOwnerId())) {
					Messages.send(player, "commands.war.8");
					return true;
				}

				if (!(region.isWorldFlagSet(WorldFlags.WARS) && targetRegion.isWorldFlagSet(WorldFlags.WARS))) {
					Messages.send(player, "commands.war.9");
					return true;
				}

				if (WarManager.isRegionInWar(targetRegion.getUniqueId())) {
					Messages.send(player, "commands.war.10");
					return true;
				}

				String prizeInput = args[2];

				if ((!NumberUtils.isValidDouble(prizeInput))
						|| (NumberUtils.isValidDouble(prizeInput) && Double.parseDouble(prizeInput) > Integer.MAX_VALUE)) {
					Messages.send(player, "commands.war.11");
					return true;
				}

				double prize = Double.parseDouble(prizeInput);

				double minPrize = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("wars.min-prize");
				double maxPrize = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("wars.max-prize");

				if (prize < minPrize || prize > maxPrize) {
					Messages.send(player, "commands.war.12", Formatter.getBalance(minPrize), Formatter.getBalance(maxPrize));
					return true;
				}

				if (!(targetRegion.getBank() >= prize && region.getBank() >= prize)) {
					Messages.send(player, "commands.war.13");
					return true;
				}

				List<String> nameList = Arrays.asList(args).subList(3, args.length);
				String name = String.join(" ", nameList);

				if (name.isEmpty()) name = "War";

				if (name.length() > 128) {
					Messages.send(player, "commands.war.14");
					return true;
				}

				if (ColorTranslator.containsMiniMessageTag(name)) {
					Messages.send(player, "commands.war.14");
					return true;
				}

				War war = WarManager.declareWar(name, prize, region, targetRegion);

				WarManager.broadcastDeclarationOfWar(war);

				break;
			}

			case "surrender": {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, "commands.war.4");
					return true;
				}

				if (!WarManager.isRegionInWar(region.getUniqueId())) {
					Messages.send(player, "commands.war.15");
					return true;
				}

				War war = WarManager.findWarByRegion(region.getUniqueId());

				final List<OfflinePlayer> warMembers = List.copyOf(WarManager.getMembersOfWar(war.getUniqueId()));

				war = WarManager.removeRegionFromWar(region.getUniqueId());

				if (war != null) {
					Region winner = war.getWinner();

					if (winner != null) {
						double prize = war.getPrize();

						region.withdrawBank(prize);
						winner.depositBank(prize);

						OfflinePlayer offlineOwner = winner.getOwner();
						Player owner = offlineOwner != null && offlineOwner.isOnline() ? (Player) offlineOwner : null;

						if (owner != null) {
							Messages.send(owner, "common.war_player_winner");

							Cooldown.startCooldown(owner, Cooldown.Type.WAR_FLAG_DISABLED);
						}
					}

					OfflinePlayer offlineOwner = region.getOwner();
					Player owner = offlineOwner != null && offlineOwner.isOnline() ? (Player) offlineOwner : null;

					if (owner != null) {
						Cooldown.startCooldown(owner, Cooldown.Type.WAR_FLAG_DISABLED);
					}

					WarManager.tellPlayersWarEnded(warMembers, winner);

					WarManager.endWar(war.getUniqueId());
				}

				Messages.send(player, "commands.war.16");

				break;
			}

			case "info": {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, "commands.war.4");
					return true;
				}

				War war = WarManager.findWarByRegion(region.getUniqueId());

				if (war == null) {
					Messages.send(player, "commands.war.15");
					return true;
				}

				Messages.send(player, "commands.war.17", Formatter.getRegionsOfWar(war), Formatter.getBalance(war.getPrize()));
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 2 && args[0].equalsIgnoreCase("declare")) {
			suggestions.addAll(RegionManager.getAll().stream().map(Region::getName).toList());
		}

		return suggestions;
	}
}
