package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarSubCmd extends SubCommandBuilder {
	public WarSubCmd() {
		super("war");
		setUsage("/region war [action] (params)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.war")) {
			Messages.send(player, 8);
			return true;
		}

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("wars.enabled");

		if (!isEnabled) {
			Messages.send(player, 105);
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		if (!Homestead.vault.isEconomyReady()) {
			Messages.send(player, 69);

			Logger.warning(Logger.PredefinedMessages.ECONOMY_INTEGRATION_DISABLED.getMessage());

			return true;
		}

		switch (args[0]) {
			case "declare": {
				Region region = TargetRegionSession.getRegion(player);

				if (region != null && WarManager.isRegionInWar(region.getUniqueId())) {
					Messages.send(player, 151);
					return true;
				}

				if (args.length < 4) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				String targetRegionName = args[1];
				Region targetRegion = RegionManager.findRegion(targetRegionName);

				if (targetRegion == null) {
					Messages.send(player, 9);
					return true;
				}

				if (!region.isOwner(player) && region.isPlayerMember(player)) {
					Messages.send(player, 149);
					return true;
				}

				if (region.getUniqueId().equals(targetRegion.getUniqueId()) || region.isOwner(targetRegion.getOwnerId())) {
					Messages.send(player, 148);
					return true;
				}

				if (!(region.isWorldFlagSet(WorldFlags.WARS) && targetRegion.isWorldFlagSet(WorldFlags.WARS))) {
					Messages.send(player, 164);
					return true;
				}

				if (WarManager.isRegionInWar(targetRegion.getUniqueId())) {
					Messages.send(player, 150);
					return true;
				}

				String prizeInput = args[2];

				if ((!NumberUtils.isValidDouble(prizeInput))
						|| (NumberUtils.isValidDouble(prizeInput) && Double.parseDouble(prizeInput) > Integer.MAX_VALUE)) {
					Messages.send(player, 146);
					return true;
				}

				double prize = Double.parseDouble(prizeInput);

				double minPrize = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("wars.min-prize");
				double maxPrize = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("wars.max-prize");

				if (prize < minPrize || prize > maxPrize) {
					Messages.send(player, 160);
					return true;
				}

				if (!(targetRegion.getBank() >= prize && region.getBank() >= prize)) {
					Messages.send(player, 157);
					return true;
				}

				List<String> nameList = Arrays.asList(args).subList(3, args.length);
				String name = String.join(" ", nameList);

				if (name.isEmpty()) name = "War";

				if (name.length() > 512) {
					Messages.send(player, 145);
					return true;
				}

				War war = WarManager.declareWar(name, prize, List.of(region, targetRegion));

				List<String> listString = Resources.<LanguageFile>get(ResourceType.Language).getStringList("147");

				Placeholder placeholder = new Placeholder()
						.add("{war-name}", war.getName())
						.add("{regionplayer}", region.getName())
						.add("{regiontarget}", targetRegion.getName())
						.add("{prize}", Formatter.getBalance(prize));

				List<OfflinePlayer> players = WarManager.getMembersOfWar(war.getUniqueId());

				for (OfflinePlayer p : players) {
					if (p.isOnline()) {
						Player player1 = (Player) p;

						player1.playSound(player1.getLocation(), Sound.EVENT_MOB_EFFECT_RAID_OMEN, SoundCategory.PLAYERS, 1f, 1f);

						for (String string : listString) {
							Messages.send(player1, Formatter.applyPlaceholders(string, placeholder));
						}
					}
				}

				break;
			}

			case "surrender": {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				if (!WarManager.isRegionInWar(region.getUniqueId())) {
					Messages.send(player, 152);
					return true;
				}

				War war = WarManager.surrenderRegionFromFirstWarFound(region.getUniqueId());

				if (war != null && war.getRegions().size() == 1) {
					Region winner = war.getRegions().getFirst();

					double prize = war.getPrize();

					region.withdrawBank(prize);
					winner.addBalanceToBank(prize);

					if (winner.getOwner().isOnline()) {
						Messages.send((Player) winner.getOwner(), 155);
					}

					WarManager.endWar(war.getUniqueId());
				}

				Messages.send(player, 153);

				break;
			}

			case "info": {
				Region region = TargetRegionSession.getRegion(player);

				if (region == null) {
					Messages.send(player, 4);
					return true;
				}

				War war = WarManager.findWarByRegionId(region.getUniqueId());

				if (!WarManager.isRegionInWar(region.getUniqueId()) || war == null) {
					Messages.send(player, 152);
					return true;
				}

				Messages.send(player, 154, new Placeholder()
						.add("{regions}", Formatter.getRegionsOfWar(war))
						.add("{prize}", Formatter.getBalance(war.getPrize()))
				);
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1 && args[0].equalsIgnoreCase("declare")) {
			suggestions.addAll(RegionManager.getAll().stream().map(Region::getName).toList());
		}

		return suggestions;
	}
}
