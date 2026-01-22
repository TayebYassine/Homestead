package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

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
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableBlock;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.StringUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerLimits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.subareas.SubAreaUtils;

import java.util.HashMap;
import java.util.Map;

public class SubAreasSubCmd extends SubCommandBuilder {
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
			PlayerUtils.sendMessage(player, 4);
			return true;
		}

		if (args.length == 1) {
			new SubAreasMenu(player, region);
			return true;
		}

		if (args.length < 3) {
			PlayerUtils.sendMessage(player, 0);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.MANAGE_SUBAREAS)) {
			return true;
		}

		switch (args[1]) {
			case "create": {
				if (!player.hasPermission("homestead.region.subareas.create")) {
					PlayerUtils.sendMessage(player, 8);
					return true;
				}

				Selection session = SelectionToolListener.getPlayerSession(player);

				if (session == null) {
					PlayerUtils.sendMessage(player, 54);
					return true;
				}

				Block firstCorner = session.getFirstPosition();
				Block secondCorner = session.getSecondPosition();

				for (Chunk chunk : ChunkUtils.getChunksInArea(firstCorner, secondCorner)) {
					Region chunkRegion = ChunksManager.getRegionOwnsTheChunk(chunk);

					if (chunkRegion == null || !chunkRegion.getUniqueId().equals(region.getUniqueId())) {
						PlayerUtils.sendMessage(player, 55);
						return true;
					}
				}

				if (SubAreaUtils.isIntersectingOtherSubArea(region.getUniqueId(), new SerializableBlock(firstCorner),
						new SerializableBlock(secondCorner))) {
					PlayerUtils.sendMessage(player, 56);
					return true;
				}

				String name = args[2];

				if (!StringUtils.isValidSubAreaName(name)) {
					PlayerUtils.sendMessage(player, 57);
					return true;
				}

				if (SubAreasManager.isNameUsed(region.getUniqueId(), name)) {
					PlayerUtils.sendMessage(player, 58);
					return true;
				}

				if (PlayerLimits.hasPlayerReachedLimit(region.getOwner(), PlayerLimits.LimitType.SUBAREAS_PER_REGION)) {
					PlayerUtils.sendMessage(player, 116);
					return true;
				}

				int volume = SubArea.getVolume(firstCorner, secondCorner);
				int maxVolume = PlayerLimits.getDefaultLimitValue(player, PlayerLimits.LimitType.MAX_SUBAREA_VOLUME);

				if (volume >= maxVolume) {
					Map<String, String> replacements = new HashMap<String, String>();
					replacements.put("{max}", String.valueOf(maxVolume));
					replacements.put("{volume}", String.valueOf(volume));

					PlayerUtils.sendMessage(player, 117, replacements);
					return true;
				}

				SubAreasManager.createSubArea(region.getUniqueId(), name, firstCorner.getWorld(), firstCorner, secondCorner, region.getPlayerFlags());

				SelectionToolListener.cancelPlayerSession(player);

				Map<String, String> replacements = new HashMap<String, String>();
				replacements.put("{subarea}", name);
				replacements.put("{subarea-volume}", String.valueOf(volume));

				PlayerUtils.sendMessage(player, 59, replacements);

				break;
			}
			case "rename": {
				if (!player.hasPermission("homestead.region.subareas.rename")) {
					PlayerUtils.sendMessage(player, 8);
					return true;
				}

				if (args.length < 4) {
					PlayerUtils.sendMessage(player, 0);
					return true;
				}

				String name = args[2];
				String newName = args[3];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					PlayerUtils.sendMessage(player, 60);
					return true;
				}

				if (!StringUtils.isValidSubAreaName(newName)) {
					PlayerUtils.sendMessage(player, 57);
					return true;
				}

				if (subArea.getName().equalsIgnoreCase(newName)) {
					PlayerUtils.sendMessage(player, 11);
					return true;
				}

				if (SubAreasManager.isNameUsed(region.getUniqueId(), newName)) {
					PlayerUtils.sendMessage(player, 58);
					return true;
				}

				final String oldName = subArea.getName();

				subArea.setName(newName);

				Map<String, String> replacements = new HashMap<String, String>();
				replacements.put("{oldname}", oldName);
				replacements.put("{newname}", newName);

				PlayerUtils.sendMessage(player, 61, replacements);

				return true;
			}
			case "delete": {
				if (!player.hasPermission("homestead.region.subareas.delete")) {
					PlayerUtils.sendMessage(player, 8);
					return true;
				}

				String name = args[2];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					PlayerUtils.sendMessage(player, 60);
					return true;
				}

				SubAreasManager.deleteSubArea(subArea.getUniqueId());

				Map<String, String> replacements = new HashMap<String, String>();
				replacements.put("{subarea}", subArea.getName());

				PlayerUtils.sendMessage(player, 62, replacements);

				return true;
			}
			case "flags": {
				if (!player.hasPermission("homestead.region.subareas.flags")) {
					PlayerUtils.sendMessage(player, 8);
					return true;
				}

				if (args.length < 4) {
					PlayerUtils.sendMessage(player, 0);
					return true;
				}

				String name = args[2];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					PlayerUtils.sendMessage(player, 60);
					return true;
				}

				String flagInput = args[3];

				if (!PlayerFlags.getFlags().contains(flagInput)) {
					PlayerUtils.sendMessage(player, 41);
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

				Map<String, String> replacements = new HashMap<String, String>();
				replacements.put("{flag}", flagInput);
				replacements.put("{state}", currentState ? "Deny" : "Allow");
				replacements.put("{region}", region.getName());
				replacements.put("{subarea}", subArea.getName());

				PlayerUtils.sendMessage(player, 63, replacements);

				return true;
			}
			case "players": {
				if (!player.hasPermission("homestead.region.subareas.players")) {
					PlayerUtils.sendMessage(player, 8);
					return true;
				}

				if (args.length < 5) {
					PlayerUtils.sendMessage(player, 0);
					return true;
				}

				String name = args[2];

				SubArea subArea = SubAreasManager.findSubArea(region.getUniqueId(), name);

				if (subArea == null) {
					PlayerUtils.sendMessage(player, 60);
					return true;
				}

				String playerName = args[3];

				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

				if (target == null) {
					Map<String, String> replacements = new HashMap<String, String>();
					replacements.put("{playername}", playerName);

					PlayerUtils.sendMessage(player, 29, replacements);
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
							PlayerUtils.sendMessage(player, 30);
							return true;
						}

						if (!region.isPlayerMember(target)) {
							PlayerUtils.sendMessage(player, 171);
							return true;
						}

						if (subArea.isPlayerMember(target)) {
							PlayerUtils.sendMessage(player, 174);
							return true;
						}

						subArea.addMember(target);

						Map<String, String> replacements = new HashMap<String, String>();
						replacements.put("{subarea}", subArea.getName());
						replacements.put("{player}", target.getName());

						PlayerUtils.sendMessage(player, 172, replacements);

						return true;
					}

					case "remove": {
						if (!subArea.isPlayerMember(target)) {
							PlayerUtils.sendMessage(player, 175);
							return true;
						}

						subArea.removeMember(target);

						Map<String, String> replacements = new HashMap<String, String>();
						replacements.put("{subarea}", subArea.getName());
						replacements.put("{player}", target.getName());

						PlayerUtils.sendMessage(player, 173, replacements);

						return true;
					}

					case "flags": {
						if (args.length < 6) {
							PlayerUtils.sendMessage(player, 0);
							return true;
						}

						if (!subArea.isPlayerMember(target)) {
							PlayerUtils.sendMessage(player, 170);
							return true;
						}

						String flagInput = args[5];

						if (!PlayerFlags.getFlags().contains(flagInput)) {
							PlayerUtils.sendMessage(player, 41);
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

						Map<String, String> replacements = new HashMap<String, String>();
						replacements.put("{flag}", flagInput);
						replacements.put("{state}", currentState ? "Deny" : "Allow");
						replacements.put("{region}", region.getName());
						replacements.put("{subarea}", subArea.getName());
						replacements.put("{player}", target.getName());

						PlayerUtils.sendMessage(player, 169, replacements);

						return true;
					}

					default: {
						PlayerUtils.sendMessage(player, 0);
						return true;
					}
				}
			}
			default: {
				PlayerUtils.sendMessage(player, 0);
				break;
			}
		}

		return true;
	}
}
