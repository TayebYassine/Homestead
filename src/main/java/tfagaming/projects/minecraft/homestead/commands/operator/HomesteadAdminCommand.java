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

		setPermission("homestead.commands.homesteadadmin");
		setUsage("/hsadmin [sub-command]");

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
			return true;
		}

		String attempted = args[0].toLowerCase();
		String similarity = String.join(", ", StringSimilarity.find(getSubCommandNames(), attempted));

		Messages.send(sender, "commands.op_hsadmin.0", similarity);

		return true;
	}

	@Override
	public List<String> onDefaultTabComplete(CommandSender sender, String[] args) {
		return super.onDefaultTabComplete(sender, args);
	}
}