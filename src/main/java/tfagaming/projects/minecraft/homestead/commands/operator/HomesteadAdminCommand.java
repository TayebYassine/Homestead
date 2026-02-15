package tfagaming.projects.minecraft.homestead.commands.operator;

import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.commands.operator.subcommands.*;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.java.StringSimilarity;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class HomesteadAdminCommand extends CommandBuilder {
	public HomesteadAdminCommand() {
		super("homesteadadmin", "hsadmin");

		registerSubCommand(new ExportSubCmd());
		registerSubCommand(new PluginSubCmd());
		registerSubCommand(new ReloadSubCmd());
		registerSubCommand(new CheckUpdatesSubCmd());
		registerSubCommand(new ImportSubCmd());
		registerSubCommand(new FlagsOverrideSubCmd());
		registerSubCommand(new ClaimSubCmd());
		registerSubCommand(new UnclaimSubCmd());
		registerSubCommand(new TransferOwnershipSubCmd());
	}

	@Override
	public boolean onDefaultExecution(CommandSender sender, String[] args) {
		if (args.length == 0) {
			Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String attempted = args[0].toLowerCase();
		String similarity = String.join(", ", StringSimilarity.find(getSubCommandNames(), attempted));

		if (sender instanceof org.bukkit.entity.Player) {
			Messages.send(sender, 7, new Placeholder()
					.add("{similarity-subcmds}", similarity)
			);
		} else {
			sender.sendMessage("Unknown sub-command. Did you mean: " + similarity);
		}

		return true;
	}

	@Override
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return super.onDefaultTabComplete(sender, args);
	}
}