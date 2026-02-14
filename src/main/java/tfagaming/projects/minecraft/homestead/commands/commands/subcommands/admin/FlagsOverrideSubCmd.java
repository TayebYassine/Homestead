package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class FlagsOverrideSubCmd extends SubCommandBuilder {
	public FlagsOverrideSubCmd() {
		super("flagsoverride");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 2) {
			Messages.send(sender, 0);
			return true;
		}

		for (Region region : RegionsManager.getAll()) {
			String setType = args[1].toLowerCase();

			switch (setType) {
				case "member": {
					if (args.length < 4) {
						Messages.send(sender, 0);
						return true;
					}

					String targetName = args[2];

					OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(targetName);

					if (target == null) {
						Messages.send(sender, 29, new Placeholder()
								.add("{playername}", targetName)
						);
						return true;
					}

					if (!region.isPlayerMember(target)) {
						continue;
					}

					String flagInput = args[3];

					if (!PlayerFlags.getFlags().contains(flagInput)) {
						Messages.send(sender, 41);
						return true;
					}

					long flags = region.getMember(target).getFlags();
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

					region.setMemberFlags(region.getMember(target), newFlags);

					Messages.send(sender, 43, new Placeholder()
							.add("{region}", region.getName())
							.add("{flag}", flagInput)
							.add("{state}", currentState ? "Deny" : "Allow")
							.add("{player}", target.getName())
					);

					break;
				}
				case "global": {
					if (args.length < 3) {
						Messages.send(sender, 0);
						return true;
					}

					String flagInput = args[2];

					if (!PlayerFlags.getFlags().contains(flagInput)) {
						Messages.send(sender, 41);
						return true;
					}

					long flags = region.getPlayerFlags();
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

					region.setPlayerFlags(newFlags);

					Messages.send(sender, 44, new Placeholder()
							.add("{region}", region.getName())
							.add("{flag}", flagInput)
							.add("{state}", currentState ? "Deny" : "Allow")
					);

					break;
				}
				case "world": {
					if (args.length < 3) {
						Messages.send(sender, 0);
						return true;
					}

					String flagInput = args[2];

					if (!WorldFlags.getFlags().contains(flagInput)) {
						Messages.send(sender, 41);
						return true;
					}

					long flags = region.getWorldFlags();
					long flag = WorldFlags.valueOf(flagInput);

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

					region.setWorldFlags(newFlags);

					Messages.send(sender, 49, new Placeholder()
							.add("{region}", region.getName())
							.add("{flag}", flagInput)
							.add("{state}", currentState ? "Deny" : "Allow")
					);

					break;
				}
				default: {
					Messages.send(sender, 0);
					break;
				}
			}
		}

		return true;
	}
}
