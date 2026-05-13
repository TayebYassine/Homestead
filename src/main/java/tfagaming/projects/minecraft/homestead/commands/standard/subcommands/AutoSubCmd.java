package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.sessions.AutoClaimSession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class AutoSubCmd extends SubCommandBuilder {
	public AutoSubCmd() {
		super("auto");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/region auto");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (AutoClaimSession.hasSession(player)) {
			AutoClaimSession.removeSession(player);

			reply(player, "autoclaim.1");
		} else {
			AutoClaimSession.newSession(player);

			reply(player, "autoclaim.0");
		}

		return true;
	}
}
