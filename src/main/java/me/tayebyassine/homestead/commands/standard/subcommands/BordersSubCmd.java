package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;
import me.tayebyassine.homestead.tools.minecraft.chunks.ChunkBorder;

import java.util.ArrayList;
import java.util.List;

public class BordersSubCmd extends SubCommandBuilder {
	public BordersSubCmd() {
		super("borders");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs borders (stop)");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (!Resources.<RegionsFile>get(ResourceType.Regions).isBordersEnabled()) {
			Messages.send(player, "commands.borders.0");
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
			ChunkBorder.stop(player);

			Messages.send(player, "commands.borders.1");

			return true;
		}

		ChunkBorder.show(player);

		Messages.send(player, "commands.borders.2");

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.add("stop");
		}

		return suggestions;
	}
}
