package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.tools.https.UpdateChecker;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class CheckUpdatesSubCmd extends SubCommandBuilder {
	public CheckUpdatesSubCmd() {
		super("updates");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Messages.send(sender, 98);

		Homestead.getInstance().runAsyncTask(() -> {
			if (UpdateChecker.check(Homestead.getInstance())) {
				Messages.send(sender, 97);
			} else {
				Messages.send(sender, 96);
			}
		});

		return true;
	}
}
