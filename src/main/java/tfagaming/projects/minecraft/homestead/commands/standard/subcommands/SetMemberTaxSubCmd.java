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
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs setmembertax [tax amount]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.setmembertax.0");
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, "commands.setmembertax.1");
			return true;
		}

		if (!Homestead.VAULT.isEconomyReady()) {
			Messages.send(player, "commands.setmembertax.2");

			Logger.warning(Logger.PredefinedMessage.ECONOMY_INTEGRATION_DISABLED);

			return true;
		}

		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("taxes.enabled");

		if (!isEnabled) {
			Messages.send(player, "commands.setmembertax.3");

			return true;
		}

		String taxInput = args[0];

		if (!NumberUtils.isValidDouble(taxInput)) {
			Messages.send(player, "commands.setmembertax.4");
			return true;
		}

		double taxAmount = Double.parseDouble(taxInput);

		double minTax = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("taxes.min-tax");
		double maxTax = Resources.<RegionsFile>get(ResourceType.Regions).getDouble("taxes.max-tax");

		if (taxAmount <= minTax || taxAmount > maxTax) {
			Messages.send(player, "commands.setmembertax.5", Formatter.getBalance(maxTax), Formatter.getBalance(maxTax));

			return true;
		}

		region.setTaxes(taxAmount);

		Messages.send(player, "commands.setmembertax.6");

		return true;
	}
}
