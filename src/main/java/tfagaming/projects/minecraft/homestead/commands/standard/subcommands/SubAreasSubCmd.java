package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.SubAreasMenu;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener;
import tfagaming.projects.minecraft.homestead.listeners.SelectionToolListener.Selection;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.subareas.SubAreaUtils;

public class SubAreasSubCmd extends LegacySubCommandBuilder {
	public SubAreasSubCmd() {
		super("subareas");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (args.length == 1) {
			new SubAreasMenu(player, region);
			return true;
		}

		if (args.length < 3) {
			Messages.send(player, 0);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.MANAGE_SUBAREAS)) {
			return true;
		}

		switch (args[1]) {
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

				for (Chunk chunk : ChunkUtils.getChunksInArea(firstCorner, secondCorner)) {
					Region chunkRegion = ChunksManager.getRegionOwnsTheChunk(chunk);

					if (chunkRegion == null || !chunkRegion.getUniqueId().equals(region.getUniqueId())) {
						Messages.send(player, 55);
						return true;
					}
				}

				if (SubAreaUtils.isIntersectingOtherSubArea(region.getUniqueId(), new SerializableBlock(firstCorner),
						new SerializableBlock(secondCorner))) {
					Messages.send(player, 56);
					return true;
				}

				String name = args[2];

				if (!StringUtils.isValidSubAreaName(name)) {
					Messages.send(player, 57);
					return true;
				}

				if (SubAreasManager.isNameUsed(region.getUniqueId(), name)) {
					Messages.send(player, 58);
					return true;
				}

				if (Limits.hasReachedLimit(null, region, Limits.LimitType.SUBAREAS_PER_REGION)) {
					Messages.send(player, 116);
					return true;
				}

				int volume = SubArea.getVolume(firstCorner, secondCorner);
				int maxVolume = Limits.getRegionLimit(region, Limits.LimitType.MAX_SUBAREA_VOLUME);

				if (volume >= maxVolume) {
					Messages.send(player, 117, new Placeholder()
							.add("{max}", maxVolume)
							.add("{volume}", volume)
					);
					return true;
				}

				SubAreasManager.createSubArea(region.getUniqueId(), name, firstCorner.getWorld(), firstCorner, secondCorner, region.getPlayerFlags());

				SelectionToolListener.cancelPlayerSession(player);

				Messages.send(player, 59, new Placeholder()
						.add("{subarea}", name)
						.add("{subarea-volume}", volume)
				);

				break;
			}
			case "rename": {
				if (!player.hasPermission("homestead.region.subareas.rename")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 4) {
					Messages.send(player, 0);
					return true;
				}

				String name = args[2];
				String newName = args[3];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

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

				if (SubAreasManager.isNameUsed(region.getUniqueId(), newName)) {
					Messages.send(player, 58);
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

				String name = args[2];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				SubAreasManager.deleteSubArea(subArea.getUniqueId());

				Messages.send(player, 62, new Placeholder()
						.add("{subarea}", subArea.getName())
				);

				return true;
			}
			case "flags": {
				if (!player.hasPermission("homestead.region.subareas.flags")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 4) {
					Messages.send(player, 0);
					return true;
				}

				String name = args[2];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				String flagInput = args[3];

				if (!PlayerFlags.getFlags().contains(flagInput)) {
					Messages.send(player, 41);
					return true;
				}

				long flags = subArea.getFlags();
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

				subArea.setFlags(newFlags);

				Messages.send(player, 63, new Placeholder()
						.add("{region}", region.getName())
						.add("{flag}", flagInput)
						.add("{subarea}", subArea.getName())
						.add("{state}", currentState ? "Deny" : "Allow")
				);

				return true;
			}
			case "players": {
				if (!player.hasPermission("homestead.region.subareas.players")) {
					Messages.send(player, 8);
					return true;
				}

				if (args.length < 5) {
					Messages.send(player, 0);
					return true;
				}

				String name = args[2];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					Messages.send(player, 60);
					return true;
				}

				String playerName = args[3];

				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

				if (target == null) {
					Messages.send(player, 29, new Placeholder()
							.add("{playername}", playerName)
					);
					return true;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.MANAGE_SUBAREAS)) {
					return true;
				}

				String action = args[4];

				switch (action.toLowerCase()) {
					case "add": {
						if (region.isOwner(target)) {
							Messages.send(player, 30);
							return true;
						}

						if (!region.isPlayerMember(target)) {
							Messages.send(player, 171);
							return true;
						}

						if (subArea.isPlayerMember(target)) {
							Messages.send(player, 174);
							return true;
						}

						subArea.addMember(target);

						Messages.send(player, 172, new Placeholder()
								.add("{subarea}", subArea.getName())
								.add("{player}", target.getName())
						);

						return true;
					}

					case "remove": {
						if (!subArea.isPlayerMember(target)) {
							Messages.send(player, 175);
							return true;
						}

						subArea.removeMember(target);

						Messages.send(player, 173, new Placeholder()
								.add("{subarea}", subArea.getName())
								.add("{player}", target.getName())
						);

						return true;
					}

					case "flags": {
						if (args.length < 6) {
							Messages.send(player, 0);
							return true;
						}

						if (!subArea.isPlayerMember(target)) {
							Messages.send(player, 170);
							return true;
						}

						String flagInput = args[5];

						if (!PlayerFlags.getFlags().contains(flagInput)) {
							Messages.send(player, 41);
							return true;
						}

						SerializableMember member = subArea.getMember(target);

						long flags = member.getFlags();
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

						subArea.setMemberFlags(member, newFlags);

						Messages.send(player, 169, new Placeholder()
								.add("{region}", region.getName())
								.add("{flag}", flagInput)
								.add("{state}", currentState ? "Deny" : "Allow")
								.add("{subarea}", subArea.getName())
								.add("{player}", target.getName())
						);

						return true;
					}

					default: {
						Messages.send(player, 0);
						return true;
					}
				}
			}
			default: {
				Messages.send(player, 0);
				break;
			}
		}

		return true;
	}
}
