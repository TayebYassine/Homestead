package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.gui.menus.GlobalPlayerFlagsMenu;
import tfagaming.projects.minecraft.homestead.gui.menus.WorldFlagsMenu;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

public class FlagsSubCmd extends SubCommandBuilder {
	public FlagsSubCmd() {
		super("flags");
		setUsage("/region flags [global/world/member] {member} [flag] (allow/deny)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

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

		String setType = args[0].toLowerCase();

		switch (setType) {
			case "member": {
				if (!player.hasPermission("homestead.region.flags.members")) {
					Messages.send(player, 8);
					return true;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.SET_MEMBER_FLAGS)) {
					return true;
				}

				if (args.length < 4) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String targetName = args[1];

				OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

				if (target == null) {
					Messages.send(player, 29, new Placeholder()
							.add("{playername}", targetName)
					);
					return true;
				}

				if (!region.isPlayerMember(target)) {
					Messages.send(player, 40, new Placeholder()
							.add("{region}", region.getName())
							.add("{playername}", target.getName())
					);
					return true;
				}

				String flagInput = args[2];

				if (!PlayerFlags.getFlags().contains(flagInput)) {
					Messages.send(player, 41);
					return true;
				}

				if (Homestead.config.isFlagDisabled(flagInput)) {
					Messages.send(player, 42);
					return true;
				}

				long flags = region.getMember(target).getFlags();
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

				region.setMemberFlags(region.getMember(target), newFlags);

				Messages.send(player, 43, new Placeholder()
						.add("{region}", region.getName())
						.add("{player}", target.getName())
						.add("{flag}", flagInput)
						.add("{state}", currentState ? "Deny" : "Allow")
				);

				break;
			}
			case "global": {
				if (!player.hasPermission("homestead.region.flags.global")) {
					Messages.send(player, 8);
					return true;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.SET_GLOBAL_FLAGS)) {
					return true;
				}

				if (args.length == 1) {
					new GlobalPlayerFlagsMenu(player, region);
					return true;
				}

				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String flagInput = args[1];

				if (!PlayerFlags.getFlags().contains(flagInput)) {
					Messages.send(player, 41);
					return true;
				}

				List<String> disabledFlags = Homestead.config.getStringList("disabled-flags");

				if (Homestead.config.isFlagDisabled(flagInput)) {
					Messages.send(player, 42);
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

				Messages.send(player, 44, new Placeholder()
						.add("{region}", region.getName())
						.add("{flag}", flagInput)
						.add("{state}", currentState ? "Deny" : "Allow")
				);

				break;
			}
			case "world": {
				if (!player.hasPermission("homestead.region.flags.world")) {
					Messages.send(player, 8);
					return true;
				}

				if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
						RegionControlFlags.SET_WORLD_FLAGS)) {
					return true;
				}

				if (args.length == 1) {
					new WorldFlagsMenu(player, region);
					return true;
				}

				if (args.length < 3) {
					Messages.send(player, 0, new Placeholder()
							.add("{usage}", getUsage())
					);
					return true;
				}

				String flagInput = args[1];

				if (!WorldFlags.getFlags().contains(flagInput)) {
					Messages.send(player, 41);
					return true;
				}

				List<String> disabledFlags = Homestead.config.getStringList("disabled-flags");

				if (Homestead.config.isFlagDisabled(flagInput)) {
					Messages.send(player, 42);
					return true;
				}

				long flags = region.getWorldFlags();
				long flag = WorldFlags.valueOf(flagInput);

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

				Messages.send(player, 49, new Placeholder()
						.add("{region}", region.getName())
						.add("{flag}", flagInput)
						.add("{state}", currentState ? "Deny" : "Allow")
				);

				break;
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

		if (args.length == 0) {

		}

		return suggestions;
	}
}
