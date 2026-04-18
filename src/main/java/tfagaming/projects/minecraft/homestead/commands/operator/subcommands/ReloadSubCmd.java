package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class ReloadSubCmd extends SubCommandBuilder {
	public ReloadSubCmd() {
		super("reload", null, false);
		setUsage("/hsadmin reload");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Homestead instance = Homestead.getInstance();

		try {
			Resources.load(instance);

			Messages.send(sender, 90);
		} catch (Exception e) {
			Logger.error(e);
			Messages.send(sender, 87);
		}

		return true;
	}
}