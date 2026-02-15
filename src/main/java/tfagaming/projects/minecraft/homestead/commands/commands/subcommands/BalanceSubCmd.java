package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class BalanceSubCmd extends SubCommandBuilder {
	public BalanceSubCmd() {
		super("balance");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.bank")) {
			Messages.send(player, 8);
			return true;
		}

		if (!Homestead.vault.isEconomyReady()) {
			Messages.send(player, 69);
			Logger.warning("The player \"" + player.getName() + "\" (UUID: " + player.getUniqueId()
					+ ") executed a command that requires economy implementation, but it's disabled.");
			Logger.warning(
					"The execution has been ignored, you may resolve this issue by installing a plugin that implements economy on the server.");

			return true;
		}

		Region region;

		if (args.length >= 2) {
			String regionName = args[1];

			region = RegionsManager.findRegion(regionName);

			if (region == null) {
				Messages.send(player, 9);
				return false;
			}
		} else {
			region = TargetRegionSession.getRegion(player);

			if (region == null) {
				Messages.send(player, 4);
				return true;
			}
		}

		Messages.send(player, 167, new Placeholder()
				.add("{region}", region.getName())
				.add("{balance}", Formatters.getBalance(region.getBank()))
		);

		return true;
	}
}
