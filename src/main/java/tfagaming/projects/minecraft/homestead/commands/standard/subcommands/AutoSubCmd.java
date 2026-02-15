package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.sessions.autoclaim.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class AutoSubCmd extends SubCommandBuilder {
	public AutoSubCmd() {
		super("auto");
		setUsage("/region auto");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (AutoClaimSession.hasSession(player)) {
			AutoClaimSession.removeSession(player);

			Messages.send(player, 136);
		} else {
			AutoClaimSession.newSession(player);

			Messages.send(player, 135);
		}

		return true;
	}
}
