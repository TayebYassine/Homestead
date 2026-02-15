package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class FlagsOverrideSubCmd extends SubCommandBuilder {
	public FlagsOverrideSubCmd() {
		super("flagsoverride");
		setUsage("/hsadmin flagsoverride [global/world/member] {member} [flag] (allow/deny)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 1) {
			Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String setType = args[0].toLowerCase();

		switch (setType) {
			case "member" -> handleMemberFlags(sender, args);
			case "global" -> handleGlobalFlags(sender, args);
			case "world" -> handleWorldFlags(sender, args);
			default -> Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
		}

		return true;
	}

	private void handleMemberFlags(CommandSender sender, String[] args) {
		if (args.length < 3) {
			Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return;
		}

		String targetName = args[1];
		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

		if (target == null) {
			Messages.send(sender, 29, new Placeholder()
					.add("{playername}", targetName)
			);
			return;
		}

		String flagInput = args[2];

		if (!PlayerFlags.getFlags().contains(flagInput)) {
			Messages.send(sender, 41);
			return;
		}

		long flag = PlayerFlags.valueOf(flagInput);

		for (Region region : RegionsManager.getAll()) {
			if (!region.isPlayerMember(target)) {
				continue;
			}

			long flags = region.getMember(target).getFlags();
			boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

			if (args.length > 3) {
				currentState = !parseFlagState(args[3]);
			}

			long newFlags = currentState
					? FlagsCalculator.removeFlag(flags, flag)
					: FlagsCalculator.addFlag(flags, flag);

			region.setMemberFlags(region.getMember(target), newFlags);

			Messages.send(sender, 43, new Placeholder()
					.add("{region}", region.getName())
					.add("{flag}", flagInput)
					.add("{state}", currentState ? "Deny" : "Allow")
					.add("{player}", target.getName())
			);
		}
	}

	private void handleGlobalFlags(CommandSender sender, String[] args) {
		if (args.length < 2) {
			Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return;
		}

		String flagInput = args[1];

		if (!PlayerFlags.getFlags().contains(flagInput)) {
			Messages.send(sender, 41);
			return;
		}

		long flag = PlayerFlags.valueOf(flagInput);

		for (Region region : RegionsManager.getAll()) {
			long flags = region.getPlayerFlags();
			boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

			if (args.length > 2) {
				currentState = !parseFlagState(args[2]);
			}

			long newFlags = currentState
					? FlagsCalculator.removeFlag(flags, flag)
					: FlagsCalculator.addFlag(flags, flag);

			region.setPlayerFlags(newFlags);

			Messages.send(sender, 44, new Placeholder()
					.add("{region}", region.getName())
					.add("{flag}", flagInput)
					.add("{state}", currentState ? "Deny" : "Allow")
			);
		}
	}

	private void handleWorldFlags(CommandSender sender, String[] args) {
		if (args.length < 2) {
			Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return;
		}

		String flagInput = args[1];

		if (!WorldFlags.getFlags().contains(flagInput)) {
			Messages.send(sender, 41);
			return;
		}

		long flag = WorldFlags.valueOf(flagInput);

		for (Region region : RegionsManager.getAll()) {
			long flags = region.getWorldFlags();
			boolean currentState = FlagsCalculator.isFlagSet(flags, flag);

			if (args.length > 2) {
				currentState = !parseFlagState(args[2]);
			}

			long newFlags = currentState
					? FlagsCalculator.removeFlag(flags, flag)
					: FlagsCalculator.addFlag(flags, flag);

			region.setWorldFlags(newFlags);

			Messages.send(sender, 49, new Placeholder()
					.add("{region}", region.getName())
					.add("{flag}", flagInput)
					.add("{state}", currentState ? "Deny" : "Allow")
			);
		}
	}

	private boolean parseFlagState(String input) {
		return switch (input.toLowerCase()) {
			case "1", "t", "true", "allow" -> true;
			default -> false;
		};
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(List.of("member", "global", "world"));
		} else if (args.length == 2 && args[0].equalsIgnoreCase("member")) {
			if (player != null) {
				Region region = TargetRegionSession.getRegion(player);
				if (region != null) {
					for (SerializableMember member : region.getMembers()) {
						OfflinePlayer bukkitMember = member.getBukkitOfflinePlayer();
						if (bukkitMember != null) {
							suggestions.add(bukkitMember.getName());
						}
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
				|| (args.length == 4 && args[0].equalsIgnoreCase("member"))) {
			suggestions.addAll(List.of("allow", "deny"));
		}

		return suggestions;
	}
}