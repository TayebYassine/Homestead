package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.PlayerJoinSubAreaEvent;
import tfagaming.projects.minecraft.homestead.api.events.PlayerLeftSubAreaEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
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
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/region subareas [create|conf] ...");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (args.length == 0) {
			new SubAreasMenu(player, region);
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.MANAGE_SUBAREAS)) {
			return true;
		}

		String action = args[0];

		switch (action) {
			case "create": {
				if (args.length < 2) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", "/hs subareas create [name]")
					);
					return true;
				}

				if (!player.hasPermission("homestead.actions.regions.subareas.create")) {
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

				return true;
			}

			case "conf": {
				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", "/hs subareas conf [subarea name] [action] (params)")
					);
					return true;
				}

				String subAreaName = args[1];
				String confAction = args[2];

				SubArea subArea = SubAreaManager.findSubArea(region.getUniqueId(), subAreaName);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				switch (confAction) {
					case "delete": {
						if (!player.hasPermission("homestead.actions.regions.subareas.delete")) {
							Messages.send(player, 8);
							return true;
						}

						SubAreaManager.deleteSubArea(subArea.getUniqueId());

						Messages.send(player, 62, new Placeholder()
								.add("{subarea}", subArea.getName())
						);

						LogManager.addLog(region, player, LogManager.PredefinedLog.DELETE_SUBAREA);

						return true;
					}

					case "rename": {
						if (!player.hasPermission("homestead.actions.regions.subareas.update.name")) {
							Messages.send(player, 8);
							return true;
						}

						if (args.length < 4) {
							Messages.send(player, 0, new Placeholder()
									.add("{usage}", "/hs subareas conf [subarea name] rename [new name]")
							);
							return true;
						}

						String newName = args[3];

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

					case "resize": {
						if (!player.hasPermission("homestead.actions.regions.subareas.resize")) {
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
						if (!player.hasPermission("homestead.actions.regions.subareas.update.flags.global")) {
							Messages.send(player, 8);
							return true;
						}

						if (args.length < 4) {
							Messages.send(player, 0, new Placeholder()
									.add("{usage}", "/hs subareas conf [subarea name] flags [flag] (state)")
							);
							return true;
						}

						String flagInput = args[3];

						if (!PlayerFlags.getFlags().contains(flagInput)) {
							Messages.send(player, 41);
							return true;
						}

						long flags = subArea.getPlayerFlags();
						long flag = PlayerFlags.valueOf(flagInput);

						boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

						if (args.length > 4) {
							String flagStateInput = args[4];

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
						if (args.length < 4) {
							Messages.send(player, 0, new Placeholder()
									.add("{usage}", "/hs subareas conf [subarea name] players [add|remove|flags] ...")
							);
							return true;
						}

						String playerAction = args[3];

						switch (playerAction.toLowerCase()) {
							case "add": {
								if (args.length < 5) {
									Messages.send(player, 0, new Placeholder()
											.add("{usage}", "/hs subareas conf [subarea name] players add [player]")
									);
									return true;
								}

								if (!player.hasPermission("homestead.actions.regions.subareas.players.add")) {
									Messages.send(player, 8);
									return true;
								}

								String playerName = args[4];
								OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

								if (target == null) {
									Messages.send(player, 29, new Placeholder()
											.add("{playername}", playerName)
									);
									return true;
								}

								if (region.isOwner(target)) {
									Messages.send(player, 30);
									return true;
								}

								if (!MemberManager.isMemberOfRegion(region, target)) {
									Messages.send(player, 171);
									return true;
								}

								if (MemberManager.isMemberOfSubArea(subArea, target)) {
									Messages.send(player, 174);
									return true;
								}

								MemberManager.addMemberToSubArea(target, subArea);

								Messages.send(player, 172, new Placeholder()
										.add("{subarea}", subArea.getName())
										.add("{player}", target.getName())
								);

								LogManager.addLog(region, player, LogManager.PredefinedLog.ADD_PLAYER_SUBAREA, target.getName(), subArea.getName());

								Homestead.callEvent(new PlayerJoinSubAreaEvent(subArea, target));

								return true;
							}

							case "remove": {
								if (args.length < 5) {
									Messages.send(player, 0, new Placeholder()
											.add("{usage}", "/hs subareas conf [subarea name] players remove [player]")
									);
									return true;
								}

								if (!player.hasPermission("homestead.actions.regions.subareas.players.remove")) {
									Messages.send(player, 8);
									return true;
								}

								String playerName = args[4];
								OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

								if (target == null) {
									Messages.send(player, 29, new Placeholder()
											.add("{playername}", playerName)
									);
									return true;
								}

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

								Homestead.callEvent(new PlayerLeftSubAreaEvent(subArea, player));

								return true;
							}

							case "flags": {
								if (args.length < 6) {
									Messages.send(player, 0, new Placeholder()
											.add("{usage}", "/hs subareas conf [subarea name] players flags [player] [flag] (state)")
									);
									return true;
								}

								if (!player.hasPermission("homestead.actions.regions.subareas.update.flags.members")) {
									Messages.send(player, 8);
									return true;
								}

								String playerName = args[4];
								OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

								if (target == null) {
									Messages.send(player, 29, new Placeholder()
											.add("{playername}", playerName)
									);
									return true;
								}

								RegionMember member = MemberManager.getMemberOfSubArea(subArea, target);

								if (member == null) {
									Messages.send(player, 170);
									return true;
								}

								String flagInput = args[5];

								if (!PlayerFlags.getFlags().contains(flagInput)) {
									Messages.send(player, 41);
									return true;
								}

								long flags = member.getPlayerFlags();
								long flag = PlayerFlags.valueOf(flagInput);

								boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

								if (args.length > 6) {
									String flagStateInput = args[6];

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
										.add("{usage}", "/hs subareas conf [subarea name] players [add|remove|flags] ...")
								);
								return true;
							}
						}
					}

					default: {
						Messages.send(player, 0, new Placeholder()
								.add("{usage}", "/hs subareas conf [subarea name] [delete|rename|resize|flags|players] (params)")
						);
						return true;
					}
				}
			}

			default: {
				Messages.send(player, 0, new Placeholder()
						.add("{usage}", getUsage())
				);
				return true;
			}
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(List.of("create", "conf"));
		} else if (args.length == 2) {
			if (args[0].equals("conf")) {
				Region region = TargetRegionSession.getRegion(player);
				if (region != null) {
					suggestions.addAll(SubAreaManager.getSubAreasOfRegion(region.getUniqueId()).stream()
							.map(SubArea::getName).toList());
				}
			}
		} else if (args.length == 3 && args[0].equals("conf")) {
			suggestions.addAll(List.of("delete", "rename", "resize", "flags", "players"));
		} else if (args.length == 4 && args[0].equals("conf")) {
			if (args[2].equals("flags")) {
				suggestions.addAll(PlayerFlags.getFlags());
			} else if (args[2].equals("players")) {
				suggestions.addAll(List.of("add", "remove", "flags"));
			}
		} else if (args.length == 5 && args[0].equals("conf") && args[2].equals("players")) {
			if (args[3].equals("add") || args[3].equals("remove")) {
				Region region = TargetRegionSession.getRegion(player);
				if (region != null) {
					for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
						OfflinePlayer bukkitMember = member.getPlayer();
						if (bukkitMember != null) {
							suggestions.add(bukkitMember.getName());
						}
					}
				}
			} else if (args[3].equals("flags")) {
				Region region = TargetRegionSession.getRegion(player);
				if (region != null) {
					for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
						OfflinePlayer bukkitMember = member.getPlayer();
						if (bukkitMember != null) {
							suggestions.add(bukkitMember.getName());
						}
					}
				}
			}
		} else if (args.length == 6 && args[0].equals("conf") && args[2].equals("players") && args[3].equals("flags")) {
			suggestions.addAll(PlayerFlags.getFlags());
		} else if (args.length == 7 && args[0].equals("conf") && args[2].equals("players") && args[3].equals("flags")) {
			suggestions.addAll(List.of("allow", "deny"));
		} else if (args.length == 5 && args[0].equals("conf") && args[2].equals("flags")) {
			suggestions.addAll(List.of("allow", "deny"));
		}

		return suggestions;
	}
}