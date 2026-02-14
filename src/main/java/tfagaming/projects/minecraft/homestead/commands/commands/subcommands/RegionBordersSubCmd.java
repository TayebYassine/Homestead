package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class RegionBordersSubCmd extends SubCommandBuilder {
	public RegionBordersSubCmd() {
		super("borders");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (args.length == 2 && args[1].equalsIgnoreCase("stop")) {
			ChunkBorder.stop(player);

			Messages.send(player, 26);

			return true;
		}

		ChunkBorder.show(player);

		Messages.send(player, 27);

		return true;
	}
}
