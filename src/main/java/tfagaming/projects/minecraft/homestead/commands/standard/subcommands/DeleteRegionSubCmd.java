package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class DeleteRegionSubCmd extends LegacySubCommandBuilder {
	public DeleteRegionSubCmd() {
		super("delete");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.delete")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 2) {
			Messages.send(player, 0);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (!PlayerUtils.isOperator(player) && !region.isOwner(player)) {
			Messages.send(player, 159);
			return true;
		}

		String confirmInput = args[1];

		if (!confirmInput.equalsIgnoreCase("confirm")) {
			Messages.send(player, 5);
			return true;
		}

		final double bankAmount = region.getBank();

		RegionsManager.deleteRegion(region.getUniqueId(), player);

		if (Homestead.vault.isEconomyReady()) {
			PlayerBank.deposit(region.getOwner(), bankAmount);
		}

		Messages.send(player, 6, new Placeholder()
				.add("{region}", region.getName())
				.add("{region-bank}", Formatters.getBalance(bankAmount))
		);

		TargetRegionSession.randomizeRegion(player);

		return true;
	}
}
