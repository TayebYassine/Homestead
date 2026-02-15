package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.sessions.autoclaim.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class AutoSubCmd extends LegacySubCommandBuilder {
	public AutoSubCmd() {
		super("auto");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (AutoClaimSession.hasSession(player)) {
			AutoClaimSession.removeSession(player);

			Messages.send(player, 136);
		} else {
			new AutoClaimSession(player);

			Messages.send(player, 135);
		}

		return true;
	}
}
