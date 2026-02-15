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
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.*;

public class WarSubCmd extends SubCommandBuilder {
	public WarSubCmd() {
		super("war");
		setUsage("/region war [action] (params)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.war")) {
			Messages.send(player, 8);
			return true;
		}

		boolean isEnabled = Homestead.config.getBoolean("wars.enabled");

		if (!isEnabled) {
			Messages.send(player, 105);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		if (!Homestead.vault.isEconomyReady()) {
			Messages.send(player, 69);
			Logger.warning("The player \"" + player.getName() + "\" (UUID: " + player.getUniqueId() + ") executed a command that requires economy implementation, but it's disabled.");
			Logger.warning("The execution has been ignored, you may resolve this issue by installing a plugin that implements economy on the server.");

			return true;
		}

		switch (args[0]) {
			case "declare": {
				Region region = TargetRegionSession.getRegion(player);

				if (region != null && WarsManager.isRegionInWar(region.getUniqueId())) {
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
				Region targetRegion = RegionsManager.findRegion(targetRegionName);

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

				if (WarsManager.isRegionInWar(targetRegion.getUniqueId())) {
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

				double minPrize = Homestead.config.getDouble("wars.min-prize");
				double maxPrize = Homestead.config.getDouble("wars.max-prize");

				if (prize < minPrize || prize > maxPrize) {
					Messages.send(player, 160);
					return true;
				}

				if (!(targetRegion.getBank() >= prize && region.getBank() >= prize)) {
					Messages.send(player, 157);
					return true;
				}

				List<String> nameList = Arrays.asList(args).subList(4, args.length);
				String name = String.join(" ", nameList);

				if (name.isEmpty()) name = "Unnamed War";

				if (name.length() > 512) {
					Messages.send(player, 145);
					return true;
				}

				War war = WarsManager.declareWar(name, prize, List.of(region, targetRegion));

				List<String> listString = Homestead.language.getStringList("147");

				Map<String, String> replacements = new HashMap<String, String>();
				replacements.put("{war-name}", war.getName());
				replacements.put("{regionplayer}", region.getName());
				replacements.put("{regiontarget}", targetRegion.getName());
				replacements.put("{prize}", Formatters.getBalance(prize));

				List<OfflinePlayer> players = WarsManager.getMembersOfWar(war.getUniqueId());

				for (OfflinePlayer p : players) {
					if (p.isOnline()) {
						Player player1 = (Player) p;

						player1.playSound(player1.getLocation(), Sound.EVENT_MOB_EFFECT_RAID_OMEN, SoundCategory.PLAYERS, 1f, 1f);

						for (String string : listString) {
							player1.sendMessage(ColorTranslator.translate(Formatters.applyPlaceholders(string, replacements)));
						}
					}
				}

				break;
			}

			case "surrender": {
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

				if (!WarsManager.isRegionInWar(region.getUniqueId())) {
					Messages.send(player, 152);
					return true;
				}

				War war = WarsManager.surrenderRegionFromFirstWarFound(region.getUniqueId());

				if (war != null && war.getRegions().size() == 1) {
					Region winner = war.getRegions().getFirst();

					double prize = war.getPrize();

					region.withdrawBank(prize);
					winner.addBalanceToBank(prize);

					if (winner.getOwner().isOnline()) {
						Messages.send((Player) winner.getOwner(), 155);
					}

					WarsManager.endWar(war.getUniqueId());
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

				War war = WarsManager.findWarByRegionId(region.getUniqueId());

				if (!WarsManager.isRegionInWar(region.getUniqueId()) || war == null) {
					Messages.send(player, 152);
					return true;
				}

				Messages.send(player, 154, new Placeholder()
						.add("{regions}", Formatters.getRegionsOfWar(war))
						.add("{prize}", Formatters.getBalance(war.getPrize()))
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

		if (args.length == 0) {

		}

		return suggestions;
	}
}
