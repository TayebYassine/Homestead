package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.List;

public class HelpSubCmd extends SubCommandBuilder {
	public HelpSubCmd() {
		super("help");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		List<String> listString = Homestead.language.get("101");

		for (String string : listString) {
			PlayerUtils.sendMessage(player, string, "");
		}

		return true;
	}
}
