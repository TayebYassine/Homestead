package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.GlobalPlayerFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionWorldFlags;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.RegionMember;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.FlagsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class FlagsSubCmd extends SubCommandBuilder {
	public FlagsSubCmd() {
		super("flags");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs flags [global/world/member] {member} [flag] (allow/deny)");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			Messages.send(player, "commands.flags.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.flags.1");
			return true;
		}

		String setType = args[0].toLowerCase();

		switch (setType) {
			case "member": {
				if (!player.hasPermission("homestead.actions.regions.update.flags.members")) {
					Messages.send(player, "commands.flags.2");
					return true;
				}

				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						ControlFlags.SET_MEMBER_FLAGS)) {
					Messages.send(player, "commands.flags.3");
					return true;
				}

				if (args.length < 3) {
					Messages.send(player, "commands.flags.4", getUsage());
					return true;
				}

				String targetName = args[1];

				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

				if (target == null) {
					Messages.send(player, "commands.flags.5");
					return true;
				}

				RegionMember member = MemberManager.getMemberOfRegion(region, target);

				if (member == null) {
					Messages.send(player, "commands.flags.6");
					return true;
				}

				if (!PlayerUtility.isOperator(player) && !region.isOwner(player) && player.getUniqueId().equals(target.getUniqueId())) {
					Messages.send(player, "commands.flags.7");
					return true;
				}

				String flagInput = args[2];

				if (!PlayerFlags.getFlags().contains(flagInput)) {
					Messages.send(player, "commands.flags.8");
					return true;
				}

				if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagInput)) {
					Messages.send(player, "commands.flags.9");
					return true;
				}

				long flags = member.getPlayerFlags();
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

				member.setPlayerFlags(newFlags);

				Messages.send(player, "commands.flags.10", flagInput, Formatter.getFlagState(!currentState), targetName, region.getName());

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagInput, member.getPlayerName(), Formatter.getFlagState(!currentState));

				break;
			}
			case "global": {
				if (!player.hasPermission("homestead.actions.regions.update.flags.global")) {
					Messages.send(player, "commands.flags.2");
					return true;
				}

				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						ControlFlags.SET_GLOBAL_FLAGS)) {
					Messages.send(player, "commands.flags.3");
					return true;
				}

				if (args.length == 1) {
					new GlobalPlayerFlags(player, region);
					return true;
				}

				String flagInput = args[1];

				if (!PlayerFlags.getFlags().contains(flagInput)) {
					Messages.send(player, "commands.flags.8");
					return true;
				}

				if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagInput)) {
					Messages.send(player, "commands.flags.9");
					return true;
				}

				long flags = region.getPlayerFlags();
				long flag = PlayerFlags.valueOf(flagInput);

				boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

				if (args.length > 2) {
					String flagStateInput = args[2];

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

				region.setPlayerFlags(newFlags);

				Messages.send(player, "commands.flags.11", flagInput, Formatter.getFlagState(!currentState), region.getName());

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagInput, region.getName(), Formatter.getFlagState(!currentState));

				break;
			}
			case "world": {
				if (!player.hasPermission("homestead.actions.regions.update.flags.world")) {
					Messages.send(player, "commands.flags.2");
					return true;
				}

				if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						ControlFlags.SET_WORLD_FLAGS)) {
					Messages.send(player, "commands.flags.3");
					return true;
				}

				if (args.length == 1) {
					new RegionWorldFlags(player, region);
					return true;
				}

				String flagInput = args[1];

				if (!WorldFlags.getFlags().contains(flagInput)) {
					Messages.send(player, "commands.flags.8");
					return true;
				}

				if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagInput)) {
					Messages.send(player, "commands.flags.9");
					return true;
				}

				long flags = region.getWorldFlags();
				long flag = WorldFlags.valueOf(flagInput);

				if (Cooldown.hasCooldown(player, Cooldown.Type.WAR_FLAG_DISABLED) && flag == WorldFlags.WARS) {
					Cooldown.sendCooldownMessage(player);
					return true;
				}

				boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

				if (args.length > 2) {
					String flagStateInput = args[2];

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

				region.setWorldFlags(newFlags);

				Messages.send(player, "commands.flags.12", flagInput, Formatter.getFlagState(!currentState), region.getName());

				LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE, flagInput, region.getName(), Formatter.getFlagState(!currentState));

				break;
			}
			default: {
				Messages.send(player, "commands.flags.4", getUsage());
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
			suggestions.addAll(List.of("member", "global", "world"));
		else if (args.length == 2 && args[0].equalsIgnoreCase("member")) {
			Region region = TargetRegionSession.getRegion(player);

			if (region != null) {
				for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
					OfflinePlayer m = member.getPlayer();

					if (m != null) {
						suggestions.add(m.getName());
					}
				}
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("global")) {
			suggestions.addAll(PlayerFlags.getFlags());
		} else if (args.length == 2 && args[0].equalsIgnoreCase("world")) {
			suggestions.addAll(WorldFlags.getFlags());
		} else if (args.length == 3 && args[0].equalsIgnoreCase("member")) {
			suggestions.addAll(PlayerFlags.getFlags());
		} else if ((args.length == 3 && args[0].equalsIgnoreCase("global"))
				|| (args.length == 3 && args[0].equalsIgnoreCase("world"))
				|| args.length == 4 && args[0].equalsIgnoreCase("member")) {
			suggestions.addAll(List.of("allow", "deny"));
		}

		return suggestions;
	}
}
