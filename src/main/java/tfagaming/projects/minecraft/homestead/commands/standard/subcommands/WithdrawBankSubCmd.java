package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.RegionControlFlags;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerBank;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class WithdrawBankSubCmd extends SubCommandBuilder {
	public WithdrawBankSubCmd() {
		super("withdraw");
		setUsage("/region withdraw [amount/all]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.bank")) {
			Messages.send(player, 8);
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		if (!Homestead.vault.isEconomyReady()) {
			Messages.send(player, 69);

			Logger.warning(Logger.PredefinedMessages.ECONOMY_INTEGRATION_DISABLED.getMessage());

			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (WarManager.isRegionInWar(region)) {
			Messages.send(player, 156);
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				RegionControlFlags.WITHDRAW_MONEY)) {
			return true;
		}

		String amountInput = args[0];

		if ((!amountInput.equalsIgnoreCase("all") && !NumberUtils.isValidDouble(amountInput))
				|| (NumberUtils.isValidDouble(amountInput) && Double.parseDouble(amountInput) > Integer.MAX_VALUE)) {
			Messages.send(player, 64);
			return true;
		}

		double amount = amountInput.equalsIgnoreCase("all") ? region.getBank()
				: Double.parseDouble(amountInput);

		if (amount <= 0) {
			Messages.send(player, 64);
			return true;
		}

		if (amount > region.getBank()) {
			Messages.send(player, 67);
			return true;
		}

		PlayerBank.deposit(player, amount);
		region.withdrawBank(amount);

		Messages.send(player, 68, new Placeholder()
				.add("{region}", region.getName())
				.add("{amount}", Formatter.getBalance(amount))
		);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.add("all");
		}

		return suggestions;
	}
}
