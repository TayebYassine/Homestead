package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.cooldown.Cooldown;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;

import java.util.ArrayList;
import java.util.List;

public class SetMemberTaxSubCmd extends SubCommandBuilder {
	public SetMemberTaxSubCmd() {
		super("setmembertax");
		setUsage("/region setmembertax [tax amount]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		if (!Homestead.VAULT.isEconomyReady()) {
			Messages.send(player, 69);

			Logger.warning(Logger.PredefinedMessages.ECONOMY_INTEGRATION_DISABLED.getMessage());

			return true;
		}

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("taxes.enabled");

		if (!isEnabled) {
			Messages.send(player, 105);

			return true;
		}

		String taxInput = args[0];

		if (!NumberUtils.isValidDouble(taxInput)) {
			Messages.send(player, 102);

			return true;
		}

		double taxAmount = Double.parseDouble(taxInput);

		double minTax = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("taxes.min-tax");
		double maxTax = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("taxes.max-tax");

		if (taxAmount <= minTax || taxAmount > maxTax) {
			Messages.send(player, 104, new Placeholder()
					.add("{min}", Formatter.getBalance(minTax))
					.add("{max}", Formatter.getBalance(maxTax))
			);

			return true;
		}

		region.setTaxes(taxAmount);

		Messages.send(player, 103, new Placeholder()
				.add("{region}", region.getName())
				.add("{tax-amount}", Formatter.getBalance(taxAmount))
		);

		return true;
	}
}
