package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.api.events.BankWithdrawEvent;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.flags.ControlFlags;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.managers.WarManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.java.Formatter;
import me.tayebyassine.homestead.tools.java.NumberUtils;
import me.tayebyassine.homestead.tools.java.Placeholder;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerBank;
import me.tayebyassine.homestead.tools.minecraft.players.PlayerUtility;

import java.util.ArrayList;
import java.util.List;

public class WithdrawBankSubCmd extends SubCommandBuilder {
	public WithdrawBankSubCmd() {
		super("withdraw");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.withdraw_bank"
		));
		setUsage("/hs withdraw [amount/all]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (args.length < 1) {
			Messages.send(player, "commands.withdraw.0");
			return true;
		}

		if (!Homestead.VAULT.isEconomyReady()) {
			Messages.send(player, "commands.withdraw.1");

			Logger.warning(Logger.PredefinedMessage.ECONOMY_INTEGRATION_DISABLED);

			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.withdraw.2");
			return true;
		}

		if (WarManager.isRegionInWar(region)) {
			Messages.send(player, "commands.withdraw.3");
			return true;
		}

		if (!PlayerUtility.hasControlRegionPermissionFlag(region.getUniqueId(), player,
				ControlFlags.WITHDRAW_MONEY)) {
			return true;
		}

		String amountInput = args[0];

		if (!amountInput.equalsIgnoreCase("all") && !NumberUtils.isValidDouble(amountInput)) {
			Messages.send(player, "commands.withdraw.5");
			return true;
		}

		double amount = amountInput.equalsIgnoreCase("all") ? region.getBank()
				: Double.parseDouble(amountInput);

		if (!Double.isFinite(amount) || amount <= 0) {
			Messages.send(player, "commands.withdraw.6");
			return true;
		}

		if (amount > region.getBank()) {
			Messages.send(player, "commands.withdraw.7");
			return true;
		}

		PlayerBank.deposit(player, amount);
		region.withdrawBank(amount);

		Messages.send(player, "commands.withdraw.8", Formatter.getBalance(amount), region.getName());

		Homestead.callEvent(new BankWithdrawEvent(region, amount));

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
