package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class DepositBankSubCmd extends SubCommandBuilder {
	public DepositBankSubCmd() {
		super("deposit");
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

		if (args.length < 2) {
			Messages.send(player, 0);
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

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (WarsManager.isRegionInWar(region.getUniqueId())) {
			Messages.send(player, 156);
			return true;
		}

		if (!PlayerUtils.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.DEPOSIT_MONEY)) {
			return true;
		}

		String amountInput = args[1];

		if ((!amountInput.equalsIgnoreCase("all") && !NumberUtils.isValidDouble(amountInput))
				|| (NumberUtils.isValidDouble(amountInput) && Double.parseDouble(amountInput) > 2147483647)) {
			Messages.send(player, 64);
			return true;
		}

		double amount = amountInput.equalsIgnoreCase("all") ? PlayerBank.get(player) : Double.parseDouble(amountInput);

		if (amount <= 0) {
			Messages.send(player, 64);
			return true;
		}

		if (amount > PlayerBank.get(player)) {
			Messages.send(player, 65);
			return true;
		}

		PlayerBank.withdraw(player, amount);
		region.addBalanceToBank(amount);

		Messages.send(player, 66, new Placeholder()
				.add("{region}", region.getName())
				.add("{amount}", Formatters.getBalance(amount))
		);

		return true;
	}
}
