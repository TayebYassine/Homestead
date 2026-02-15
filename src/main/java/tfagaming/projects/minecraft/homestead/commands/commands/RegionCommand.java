package tfagaming.projects.minecraft.homestead.commands.commands;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.commands.commands.subcommands.*;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionsMenu;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIconTools;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBannedPlayer;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.commands.AutoCompleteFilter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringSimilarity;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;

import java.util.ArrayList;
import java.util.List;

public class RegionCommand extends CommandBuilder {
	public RegionCommand() {
		super("region", "rg", "hs", "homestead");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (args.length == 0) {
			new RegionsMenu(player);

			return true;
		}

		String subCommand = args[0].toLowerCase();

		if (getSubcommands().contains(subCommand)) {
			if (!player.hasPermission("homestead.commands.region." + subCommand)) {
				Messages.send(player, 8);
				return true;
			}
		}

		return switch (subCommand) {
			case "create" -> new CreateRegionSubCmd().onExecution(sender, args);
			case "delete" -> new DeleteRegionSubCmd().onExecution(sender, args);
			case "set" -> new SetRegionSubCmd().onExecution(sender, args);
			case "rename" -> new RenameRegionSubCmd().onExecution(sender, args);
			case "borders" -> new RegionBordersSubCmd().onExecution(sender, args);
			case "ban" -> new BanPlayerSubCmd().onExecution(sender, args);
			case "unban" -> new UnbanPlayerSubCmd().onExecution(sender, args);
			case "trust" -> new TrustPlayerSubCmd().onExecution(sender, args);
			case "untrust" -> new UntrustPlayerSubCmd().onExecution(sender, args);
			case "flags" -> new FlagsSubCmd().onExecution(sender, args);
			case "accept" -> new AcceptInviteSubCmd().onExecution(sender, args);
			case "deny" -> new DenyInviteSubCmd().onExecution(sender, args);
			case "visit" -> new VisitRegionSubCmd().onExecution(sender, args);
			case "subareas" -> new SubAreasSubCmd().onExecution(sender, args);
			case "deposit" -> new DepositBankSubCmd().onExecution(sender, args);
			case "withdraw" -> new WithdrawBankSubCmd().onExecution(sender, args);
			case "menu" -> new MenuSubCmd().onExecution(sender, args);
			case "player" -> new PlayerInfoSubCmd().onExecution(sender, args);
			case "home" -> new HomeSubCmd().onExecution(sender, args);
			case "info" -> new RegionInfoSubCmd().onExecution(sender, args);
			case "banlist" -> new BanlistSubCmd().onExecution(sender, args);
			case "members" -> new MembersSubCmd().onExecution(sender, args);
			case "claimlist" -> new ClaimlistSubCmd().onExecution(sender, args);
			case "help" -> new HelpSubCmd().onExecution(sender, args);
			case "logs" -> new LogsSubCmd().onExecution(sender, args);
			case "rate" -> new RegionRateSubCmd().onExecution(sender, args);
			case "top" -> new RegionTopSubCmd().onExecution(sender, args);
			case "auto" -> new AutoSubCmd().onExecution(sender, args);
			case "kick" -> new KickPlayerSubCmd().onExecution(sender, args);
			case "war" -> new WarSubCmd().onExecution(sender, args);
			case "chat" -> new ChatSubCmd().onExecution(sender, args);
			case "mail" -> new MailSubCmd().onExecution(sender, args);
			case "balance" -> new BalanceSubCmd().onExecution(sender, args);
			case "rewards" -> new RewardsSubCmd().onExecution(sender, args);
			case "merge" -> new MergeRegionSubCmd().onExecution(sender, args);
			case "mergeaccept" -> new MergeAcceptRegionSubCmd().onExecution(sender, args);
			case "levels" -> new LevelsSubCmd().onExecution(sender, args);
			default -> {
				String similarity = String.join(", ", StringSimilarity.findTopSimilarStrings(getSubcommands(), subCommand));

				Messages.send(player, 7, new Placeholder()
						.add("{similarity-subcms}", similarity)
				);

				yield true;
			}
		};
	}

	@Override
	public List<String> onAutoComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			return Lists.newArrayList();
		}

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			List<String> subcommands = getSubcommands().stream()
					.filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
					.toList();

			for (String subcommand : subcommands) {
				if (player.hasPermission("homestead.commands.region." + subcommand)) {
					suggestions.add(subcommand);
				}
			}

			return suggestions;
		}

		if (getSubcommands().contains(args[0].toLowerCase())) {
			if (!player.hasPermission("homestead.commands.region." + args[0].toLowerCase())) {
				return new ArrayList<>();
			}
		}

		switch (args[0].toLowerCase()) {
			case "borders": {
				if (args.length == 2)
					suggestions.add("stop");
				break;
			}
			case "delete": {
				if (args.length == 2)
					suggestions.add("confirm");
				break;
			}
			case "set": {
				if (args.length == 2)
					suggestions.addAll(
							List.of("displayname", "target", "description", "mapcolor", "spawn", "icon", "tax"));
				else if (args.length == 3 && args[1].equalsIgnoreCase("target")) {
					if (PlayerUtils.isOperator(player)) {
						suggestions.addAll(RegionsManager.getAll().stream().map(Region::getName).toList());
					} else {
						suggestions.addAll(
								RegionsManager.getRegionsOwnedByPlayer(player).stream().map(Region::getName).toList());
						suggestions.addAll(
								RegionsManager.getRegionsHasPlayerAsMember(player).stream().map(Region::getName).toList());
					}
				} else if (args.length == 3 && args[1].equalsIgnoreCase("mapcolor"))
					suggestions.addAll(MapColor.getAll());
				else if (args.length == 3 && args[1].equalsIgnoreCase("icon")) {
					suggestions.addAll(RegionIconTools.getAllIcons());
					suggestions.add("Default");
				}
				break;
			}
			case "kick": {
				if (args.length == 2)
					suggestions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
				break;
			}
			case "ban": {
				if (args.length == 2)
					suggestions.addAll(Homestead.getInstance().getOfflinePlayersSync().stream()
							.map(OfflinePlayer::getName).toList());
				break;
			}
			case "unban": {
				if (args.length == 2) {
					Region region = TargetRegionSession.getRegion(player);

					if (region != null) {
						for (SerializableBannedPlayer bannedPlayer : region.getBannedPlayers()) {
							OfflinePlayer bukkitBannedPlayer = bannedPlayer.getBukkitOfflinePlayer();

							if (bukkitBannedPlayer != null) {
								suggestions.add(bukkitBannedPlayer.getName());
							}
						}
					}
				}
				break;
			}
			case "player":
			case "trust": {
				if (args.length == 2)
					suggestions.addAll(Homestead.getInstance().getOfflinePlayersSync().stream()
							.map(OfflinePlayer::getName).toList());
				break;
			}
			case "untrust": {
				if (args.length == 2) {
					Region region = TargetRegionSession.getRegion(player);

					if (region != null) {
						for (SerializableMember member : region.getMembers()) {
							OfflinePlayer bukkitMember = member.getBukkitOfflinePlayer();

							if (bukkitMember != null) {
								suggestions.add(bukkitMember.getName());
							}
						}

						suggestions.addAll(region.getInvitedPlayers().stream().map(OfflinePlayer::getName).toList());
					}
				}
				break;
			}
			case "flags": {
				if (args.length == 2)
					suggestions.addAll(List.of("member", "global", "world"));
				else if (args.length == 3 && args[1].equalsIgnoreCase("member")) {
					Region region = TargetRegionSession.getRegion(player);

					if (region != null) {
						for (SerializableMember member : region.getMembers()) {
							OfflinePlayer bukkitMember = member.getBukkitOfflinePlayer();

							if (bukkitMember != null) {
								suggestions.add(bukkitMember.getName());
							}
						}
					}
				} else if (args.length == 3 && args[1].equalsIgnoreCase("global")) {
					suggestions.addAll(PlayerFlags.getFlags());
				} else if (args.length == 3 && args[1].equalsIgnoreCase("world")) {
					suggestions.addAll(WorldFlags.getFlags());
				} else if (args.length == 4 && args[1].equalsIgnoreCase("member")) {
					suggestions.addAll(PlayerFlags.getFlags());
				} else if ((args.length == 4 && args[1].equalsIgnoreCase("global"))
						|| (args.length == 4 && args[1].equalsIgnoreCase("world"))
						|| args.length == 5 && args[1].equalsIgnoreCase("member")) {
					suggestions.addAll(List.of("allow", "deny"));
				}
				break;
			}
			case "accept":
			case "deny": {
				if (args.length == 2)
					suggestions.addAll(
							RegionsManager.getRegionsInvitedPlayer(player).stream().map(Region::getName).toList());
				break;
			}
			case "visit": {
				if (args.length == 2)
					if (Homestead.config.isWelcomeSignEnabled()) {
						suggestions.addAll(RegionsManager.getPlayersWithRegionsHasWelcomeSigns().stream().map(OfflinePlayer::getName).toList());
					} else {
						if (PlayerUtils.isOperator(player)) {
							suggestions.addAll(
									RegionsManager.getAll().stream().map(Region::getName).toList());
						} else {
							suggestions.addAll(
									RegionsManager.getPublicRegions().stream().map(Region::getName).toList());
						}
					}
				if (args.length == 3)
					if (Homestead.config.isWelcomeSignEnabled()) {
						for (int i = 0; i < RegionsManager.getPlayersWithRegionsHasWelcomeSigns().size(); i++) {
							suggestions.add(String.valueOf(i));
						}
					}
				break;
			}
			case "subareas": {
				if (args.length == 2)
					suggestions.addAll(List.of("create", "delete", "rename", "flags", "players"));
				else if (args.length == 3 && !args[1].equals("create")) {
					Region region = TargetRegionSession.getRegion(player);

					if (region != null) {
						suggestions.addAll(SubAreasManager.getSubAreasOfRegion(region.getUniqueId()).stream()
								.map(SubArea::getName).toList());
					}
				} else if (args.length == 4 && args[1].equals("players")) {
					Region region = TargetRegionSession.getRegion(player);

					if (region != null) {
						for (SerializableMember member : region.getMembers()) {
							OfflinePlayer bukkitMember = member.getBukkitOfflinePlayer();

							if (bukkitMember != null) {
								suggestions.add(bukkitMember.getName());
							}
						}
					}
				} else if ((args.length == 4 && args[1].equals("flags") || (args.length == 6 && args[1].equals("players") && args[4].equals("flags"))))
					suggestions.addAll(PlayerFlags.getFlags());
				else if ((args.length == 5 && args[1].equals("flags") || (args.length == 7 && args[1].equals("players") && args[4].equals("flags"))))
					suggestions.addAll(List.of("allow", "deny"));
				break;
			}
			case "deposit":
			case "withdraw":
				if (args.length == 2)
					suggestions.add("all");
				break;
			case "balance":
			case "mail":
			case "info":
			case "rate":
				if (args.length == 2)
					suggestions
							.addAll(RegionsManager.getAll().stream().map(Region::getName).toList());
				break;
			case "war":
				if (args.length == 3 && args[1].equalsIgnoreCase("declare"))
					suggestions
							.addAll(RegionsManager.getAll().stream().map(Region::getName).toList());
				break;
			case "merge": {
				if (args.length == 2)
					suggestions
							.addAll(RegionsManager.getAll().stream().map(Region::getName).toList());
			}
		}

		return AutoCompleteFilter.filter(suggestions, args);
	}

	public List<String> getSubcommands() {
		return Lists.newArrayList("create", "delete", "set", "rename", "borders", "ban", "unban", "trust", "untrust",
				"flags", "accept", "deny", "visit", "subareas", "deposit", "withdraw", "menu", "player", "home",
				"info", "banlist", "members", "claimlist", "help", "logs", "rate", "top", "auto", "kick", "war", "chat",
				"mail", "balance", "rewards", "merge", "mergeaccept", "levels");
	}
}
