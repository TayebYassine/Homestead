package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
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
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class FlagsSubCmd extends SubCommandBuilder {

	private static final List<String> SET_TYPES = List.of("member", "global", "world");
	private static final List<String> FLAG_STATES = List.of("allow", "deny");
	private static final List<String> ALLOW_INPUTS = List.of("1", "t", "true", "allow");
	private static final List<String> DENY_INPUTS = List.of("0", "f", "false", "deny");

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

		switch (args[0].toLowerCase()) {
			case "member" -> handleMemberFlags(player, region, args);
			case "global" -> handleGlobalFlags(player, region, args);
			case "world" -> handleWorldFlags(player, region, args);
			default -> Messages.send(player, "commands.flags.4", getUsage());
		}
		return true;
	}

	private void handleMemberFlags(Player player, Region region, String[] args) {
		if (!checkPermission(player, region, "homestead.actions.regions.update.flags.members", ControlFlags.SET_MEMBER_FLAGS)) {
			return;
		}

		if (args.length < 3) {
			Messages.send(player, "commands.flags.4", getUsage());
			return;
		}

		OfflinePlayer target = resolveTarget(player, args[1]);
		if (target == null) return;

		RegionMember member = MemberManager.getMemberOfRegion(region, target);
		if (member == null) {
			Messages.send(player, "commands.flags.6");
			return;
		}

		if (!canModifyMemberFlags(player, region, target)) {
			Messages.send(player, "commands.flags.7");
			return;
		}

		String flagInput = args[2];
		if (!isValidPlayerFlag(player, flagInput)) return;

		boolean newState = toggleFlag(member.getPlayerFlags(), PlayerFlags.valueOf(flagInput), args, 3);
		member.setPlayerFlags(applyFlag(member.getPlayerFlags(), PlayerFlags.valueOf(flagInput), newState));

		Messages.send(player, "commands.flags.10", flagInput, Formatter.getFlagState(newState), target.getName(), region.getName());
		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
				flagInput, member.getPlayerName(), Formatter.getFlagState(newState));
	}

	private void handleGlobalFlags(Player player, Region region, String[] args) {
		if (!checkPermission(player, region, "homestead.actions.regions.update.flags.global", ControlFlags.SET_GLOBAL_FLAGS)) {
			return;
		}
		if (args.length == 1) {
			new GlobalPlayerFlags(player, region);
			return;
		}

		String flagInput = args[1];
		if (!isValidPlayerFlag(player, flagInput)) return;

		boolean newState = toggleFlag(region.getPlayerFlags(), PlayerFlags.valueOf(flagInput), args, 2);
		region.setPlayerFlags(applyFlag(region.getPlayerFlags(), PlayerFlags.valueOf(flagInput), newState));

		Messages.send(player, "commands.flags.11", flagInput, Formatter.getFlagState(newState), region.getName());
		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
				flagInput, region.getName(), Formatter.getFlagState(newState));
	}

	private void handleWorldFlags(Player player, Region region, String[] args) {
		if (!checkPermission(player, region, "homestead.actions.regions.update.flags.world", ControlFlags.SET_WORLD_FLAGS)) {
			return;
		}
		if (args.length == 1) {
			new RegionWorldFlags(player, region);
			return;
		}

		String flagInput = args[1];
		if (!isValidWorldFlag(player, flagInput)) return;

		long flag = WorldFlags.valueOf(flagInput);
		if (flag == WorldFlags.WARS && Cooldown.hasCooldown(player, Cooldown.Type.WAR_FLAG_DISABLED)) {
			Cooldown.sendCooldownMessage(player);
			return;
		}

		boolean newState = toggleFlag(region.getWorldFlags(), flag, args, 2);
		region.setWorldFlags(applyFlag(region.getWorldFlags(), flag, newState));

		Messages.send(player, "commands.flags.12", flagInput, Formatter.getFlagState(newState), region.getName());
		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_FLAG_STATE,
				flagInput, region.getName(), Formatter.getFlagState(newState));
	}

	private boolean checkPermission(Player player, Region region, String permission, long controlFlag) {
		if (!player.hasPermission(permission)) {
			Messages.send(player, "commands.flags.2");
			return false;
		}

		return PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player, controlFlag);
	}

	private OfflinePlayer resolveTarget(Player player, String targetName) {
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);
		if (target == null) {
			Messages.send(player, "commands.flags.5");
		}
		return target;
	}

	private boolean canModifyMemberFlags(Player player, Region region, OfflinePlayer target) {
		return PlayerUtility.isOperator(player) || region.isOwner(player) || !PlayerUtility.equals(player, target);
	}

	private boolean isValidPlayerFlag(Player player, String flagInput) {
		if (!PlayerFlags.getFlags().contains(flagInput)) {
			Messages.send(player, "commands.flags.8");
			return false;
		}
		return isFlagEnabled(player, flagInput);
	}

	private boolean isValidWorldFlag(Player player, String flagInput) {
		if (!WorldFlags.getFlags().contains(flagInput)) {
			Messages.send(player, "commands.flags.8");
			return false;
		}
		return isFlagEnabled(player, flagInput);
	}

	private boolean isFlagEnabled(Player player, String flagInput) {
		if (Resources.<FlagsFile>get(ResourceType.Flags).isFlagDisabled(flagInput)) {
			Messages.send(player, "commands.flags.9");
			return false;
		}
		return true;
	}

	private boolean toggleFlag(long currentFlags, long flag, String[] args, int stateIndex) {
		boolean currentState = FlagsCalculator.isFlagSet(currentFlags, flag);
		if (args.length > stateIndex) {
			String input = args[stateIndex].toLowerCase();
			if (ALLOW_INPUTS.contains(input)) return true;
			if (DENY_INPUTS.contains(input)) return false;
		}
		return !currentState;
	}

	private long applyFlag(long flags, long flag, boolean set) {
		return set ? FlagsCalculator.addFlag(flags, flag) : FlagsCalculator.removeFlag(flags, flag);
	}

	private long regionIdFor(Player player) {
		Region region = TargetRegionSession.getRegion(player);
		return region != null ? region.getUniqueId() : null;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();
		String setType = args[0].toLowerCase();

		switch (args.length) {
			case 1 -> suggestions.addAll(SET_TYPES);
			case 2 -> {
				switch (setType) {
					case "member" -> suggestions.addAll(getMemberNames(player));
					case "global" -> suggestions.addAll(PlayerFlags.getFlags());
					case "world" -> suggestions.addAll(WorldFlags.getFlags());
				}
			}
			case 3 -> {
				if (setType.equals("member")) {
					suggestions.addAll(PlayerFlags.getFlags());
				} else if (setType.equals("global") || setType.equals("world")) {
					suggestions.addAll(FLAG_STATES);
				}
			}
			case 4 -> {
				if (setType.equals("member")) {
					suggestions.addAll(FLAG_STATES);
				}
			}
		}
		return suggestions;
	}

	private List<String> getMemberNames(Player player) {
		List<String> names = new ArrayList<>();
		Region region = TargetRegionSession.getRegion(player);
		if (region == null) return names;

		for (RegionMember member : MemberManager.getMembersOfRegion(region)) {
			OfflinePlayer m = member.getPlayer();
			if (m != null) names.add(m.getName());
		}
		return names;
	}
}