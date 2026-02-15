package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chunks.ChunkBorder;

import java.util.ArrayList;
import java.util.List;

public class BordersSubCmd extends SubCommandBuilder {
	public BordersSubCmd() {
		super("borders");
		setUsage("/region borders (stop)");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
			ChunkBorder.stop(player);

			Messages.send(player, 26);

			return true;
		}

		ChunkBorder.show(player);

		Messages.send(player, 27);

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 0) {
			suggestions.add("stop");
		}

		return suggestions;
	}
}
