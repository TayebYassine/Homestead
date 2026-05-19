package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.api.events.BankWithdrawEvent;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.flags.ControlFlags;
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

		if ((!amountInput.equalsIgnoreCase("all") && !NumberUtils.isValidDouble(amountInput))
				|| (NumberUtils.isValidDouble(amountInput) && Double.parseDouble(amountInput) > Integer.MAX_VALUE)) {
			Messages.send(player, "commands.withdraw.5");
			return true;
		}

		double amount = amountInput.equalsIgnoreCase("all") ? region.getBank()
				: Double.parseDouble(amountInput);

		if (amount <= 0) {
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
