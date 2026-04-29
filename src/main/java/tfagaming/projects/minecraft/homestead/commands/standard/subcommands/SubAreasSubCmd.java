package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.SubAreasMenu;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener.Selection;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.models.SubArea;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;




import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.tools.minecraft.subareas.SubAreaUtility;

import java.util.ArrayList;
import java.util.List;

public class SubAreasSubCmd extends SubCommandBuilder {
	public SubAreasSubCmd() {
		super("subareas");
		setUsage("/region subareas [action] [subarea] (params)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (args.length == 0) {
			new SubAreasMenu(player, region);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.MANAGE_SUBAREAS)) {
			return true;
		}

		switch (args[0]) {
			case "create": {
				if (!player.hasPermission("homestead.region.subareas.create")) {
					Messages.send(player, 8);
					return true;
				}

				Selection session = SelectionToolListener.getPlayerSession(player);

				if (session == null) {
					Messages.send(player, 54);
					return true;
				}

				Block firstCorner = session.getFirstPosition();
				Block secondCorner = session.getSecondPosition();

				for (Chunk chunk : ChunkUtility.getChunksInArea(firstCorner, secondCorner)) {
					if (!ChunkManager.isChunkClaimedByRegion(region, chunk)) {
						Messages.send(player, 55);
						return true;
					}
				}

				if (SubAreaUtility.isIntersectingOtherSubArea(region.getUniqueId(), new SeBlock(firstCorner),
						new SeBlock(secondCorner))) {
					Messages.send(player, 56);
					return true;
				}

				String name = args[1];

				if (!StringUtils.isValidSubAreaName(name)) {
					Messages.send(player, 57);
					return true;
				}

				if (SubAreaManager.isNameUsed(region.getUniqueId(), name)) {
					Messages.send(player, 58);
					return true;
				}

				if (Limits.hasReachedLimit(null, region, Limits.LimitType.SUBAREAS_PER_REGION)) {
					Messages.send(player, 116);
					return true;
				}

				int volume = SubAreaUtility.getVolume(firstCorner, secondCorner);
				int maxVolume = Limits.getRegionLimit(region, Limits.LimitType.MAX_SUBAREA_VOLUME);

				if (volume >= maxVolume) {
					Messages.send(player, 117, new Placeholder()
							.add("{max}", maxVolume)
							.add("{volume}", volume)
					);
					return true;
				}

				SubAreaManager.createSubArea(region, name, firstCorner.getWorld(), firstCorner, secondCorner);

				SelectionToolListener.cancelPlayerSession(player);

				Messages.send(player, 59, new Placeholder()
						.add("{subarea}", name)
						.add("{subarea-volume}", volume)
				);

				LogManager.addLog(region, player, LogManager.PredefinedLog.CREATE_SUBAREA);

				break;
			}
			case "rename": {
				if (!player.hasPermission("homestead.region.subareas.rename")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String name = args[1];
				String newName = args[2];

				SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				if (!StringUtils.isValidSubAreaName(newName)) {
					Messages.send(player, 57);
					return true;
				}

				if (subArea.getName().equalsIgnoreCase(newName)) {
					Messages.send(player, 11);
					return true;
				}

				if (SubAreaManager.isNameUsed(region.getUniqueId(), newName)) {
					Messages.send(player, 58);
					return true;
				}

				if (ColorTranslator.containsMiniMessageTag(newName)) {
					Messages.send(player, 30);
					return true;
				}

				final String oldName = subArea.getName();

				subArea.setName(newName);

				Messages.send(player, 61, new Placeholder()
						.add("{oldname}", oldName)
						.add("{newname}", newName)
				);

				return true;
			}
			case "delete": {
				if (!player.hasPermission("homestead.region.subareas.delete")) {
					Messages.send(player, 8);
					return true;
				}

				String name = args[1];

				SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				SubAreaManager.deleteSubArea(subArea.getUniqueId());

				Messages.send(player, 62, new Placeholder()
						.add("{subarea}", subArea.getName())
				);

				LogManager.addLog(region, player, LogManager.PredefinedLog.DELETE_SUBAREA);

				return true;
			}
			case "resize": {
				if (!player.hasPermission("homestead.region.subareas.resize")) {
					Messages.send(player, 8);
					return true;
				}

				String name = args[1];

				SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				Selection session = SelectionToolListener.getPlayerSession(player);

				if (session == null) {
					Messages.send(player, 54);
					return true;
				}

				Block firstCorner = session.getFirstPosition();
				Block secondCorner = session.getSecondPosition();

				for (Chunk chunk : ChunkUtility.getChunksInArea(firstCorner, secondCorner)) {
					if (!(ChunkManager.isChunkClaimedByRegion(region, chunk) && firstCorner.getWorld().getUID().equals(subArea.getWorldId()))) {
						Messages.send(player, 55);
						return true;
					}
				}

				SubArea intersectedSubArea = SubAreaUtility.getIntersectedSubArea(region.getUniqueId(), new SeBlock(firstCorner),
						new SeBlock(secondCorner));

				if (intersectedSubArea != null && intersectedSubArea.getUniqueId() != subArea.getUniqueId()) {
					Messages.send(player, 56);
					return true;
				}

				subArea.setPoint1(firstCorner);
				subArea.setPoint2(secondCorner);

				Messages.send(player, 215);

				return true;
			}
			case "flags": {
				if (!player.hasPermission("homestead.region.subareas.flags")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String name = args[1];

				SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				String flagInput = args[2];

				if (!PlayerFlags.getFlags().contains(flagInput)) {
					Messages.send(player, 41);
					return true;
				}

				long flags = subArea.getPlayerFlags();
				long flag = PlayerFlags.valueOf(flagInput);

				boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

				if (args.length > 3) {
					String flagStateInput = args[3];

					switch (flagStateInput.toLowerCase()) {
						case "1":
						case "t":
						case "true":
						case "allow":
							currentState = false;
							break;
						case "0":
						case "f":
						case "false":
						case "deny":
							currentState = true;
							break;
						default:
							break;
					}
				}

				long newFlags;

				if (currentState) {
					newFlags = FlagsCalculator.removeFlag(flags, flag);
				} else {
					newFlags = FlagsCalculator.addFlag(flags, flag);
				}

				subArea.setPlayerFlags(newFlags);

				Messages.send(player, 63, new Placeholder()
						.add("{region}", region.getName())
						.add("{flag}", flagInput)
						.add("{subarea}", subArea.getName())
						.add("{state}", Formatter.getFlagState(!currentState))
				);

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagInput, subArea.getName(), Formatter.getFlagState(!currentState));

				return true;
			}
			case "players": {
				if (!player.hasPermission("homestead.region.subareas.players")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 4) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String name = args[1];

				SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				String playerName = args[2];

				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

				if (target == null) {
					Messages.send(player, 29, new Placeholder()
							.add("{playername}", playerName)
					);
					return true;
				}

				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.MANAGE_SUBAREAS)) {
					return true;
				}

				String action = args[3];

				switch (action.toLowerCase()) {
					case "add": {
						if (region.isOwner(target)) {
							Messages.send(player, 30);
							return true;
						}

						if (!MemberManager.isMemberOfRegion(region, target)) {
							Messages.send(player, 171);
							return true;
						}

						if (!MemberManager.isMemberOfSubArea(subArea, target)) {
							Messages.send(player, 174);
							return true;
						}

						MemberManager.addMemberToSubArea(target, subArea);

						Messages.send(player, 172, new Placeholder()
								.add("{subarea}", subArea.getName())
								.add("{player}", target.getName())
						);

						LogManager.addLog(region, player, LogManager.PredefinedLog.ADD_PLAYER_SUBAREA, target.getName(), subArea.getName());

						return true;
					}

					case "remove": {
						if (!MemberManager.isMemberOfSubArea(subArea, target)) {
							Messages.send(player, 175);
							return true;
						}

						MemberManager.removeMemberFromSubArea(target, subArea);

						Messages.send(player, 173, new Placeholder()
								.add("{subarea}", subArea.getName())
								.add("{player}", target.getName())
						);

						LogManager.addLog(region, player, LogManager.PredefinedLog.REMOVE_PLAYER_SUBAREA, target.getName(), subArea.getName());

						return true;
					}

					case "flags": {
						if (!player.hasPermission("homestead.region.subareas.players.flags")) {
							Messages.send(player, 8);
							return true;
						}

						if (args.length < 5) {
							Messages.send(player, 0, new Placeholder()
									.add("{usage}", getUsage())
							);
							return true;
						}

						RegionMember member = MemberManager.getMemberOfSubArea(subArea, target);

						if (member == null) {
							Messages.send(player, 170);
							return true;
						}

						String flagInput = args[4];

						if (!PlayerFlags.getFlags().contains(flagInput)) {
							Messages.send(player, 41);
							return true;
						}

						long flags = member.getPlayerFlags();
						long flag = PlayerFlags.valueOf(flagInput);

						boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

						if (args.length > 5) {
							String flagStateInput = args[5];

							switch (flagStateInput.toLowerCase()) {
								case "1":
								case "t":
								case "true":
								case "allow":
									currentState = false;
									break;
								case "0":
								case "f":
								case "false":
								case "deny":
									currentState = true;
									break;
								default:
									break;
							}
						}

						long newFlags;

						if (currentState) {
							newFlags = FlagsCalculator.removeFlag(flags, flag);
						} else {
							newFlags = FlagsCalculator.addFlag(flags, flag);
						}

						member.setPlayerFlags(newFlags);

						Messages.send(player, 169, new Placeholder()
								.add("{region}", region.getName())
								.add("{flag}", flagInput)
								.add("{state}", Formatter.getFlagState(!currentState))
								.add("{subarea}", subArea.getName())
								.add("{player}", target.getName())
						);

						LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagInput, member.getPlayerName(), Formatter.getFlagState(!currentState));

						return true;
					}

					default: {
						Messages.send(player, 0, new Placeholder()
								.add("{usage}", getUsage())
						);
						return true;
					}
				}
			}
			default: {
				Messages.send(player, 0, new Placeholder()
						.add("{usage}", getUsage())
				);
				break;
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1)
			suggestions.addAll(List.of("create", "delete", "rename", "flags", "players", "resize"));
		else if (args.length == 2 && !args[0].equals("create")) {
			Region region = TargetRegionSession.getRegion(player);

			if (region != null) {
				suggestions.addAll(SubAreaManager.getSubAreasOfRegion(region.getUniqueId()).stream()
						.map(SubArea::getName).toList());
			}
		} else if (args.length == 3 && args[0].equals("players")) {
			Region region = TargetRegionSession.getRegion(player);

			if (region != null) {
				for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
					OfflinePlayer bukkitMember = member.getPlayer();

					if (bukkitMember != null) {
						suggestions.add(bukkitMember.getName());
					}
				}
			}
		} else if ((args.length == 3 && args[0].equals("flags") || (args.length == 5 && args[0].equals("players") && args[3].equals("flags"))))
			suggestions.addAll(PlayerFlags.getFlags());
		else if ((args.length == 4 && args[0].equals("flags") || (args.length == 6 && args[0].equals("players") && args[3].equals("flags"))))
			suggestions.addAll(List.of("allow", "deny"));
		else if (args.length == 4 && args[0].equals("players"))
			suggestions.addAll(List.of("add", "remove", "flags"));

		return suggestions;
	}
}
