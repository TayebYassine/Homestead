package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.PlayerInfo;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfoSubCmd extends SubCommandBuilder {
	public PlayerInfoSubCmd() {
		super("player");
		setUsage("/region player [player]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		
		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (args.length < 1) {
			new PlayerInfo(player, player, player::closeInventory);
		} else {
			String playerName = args[0];

			OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

			if (target == null) {
				Messages.send(player, 29, new Placeholder()
						.add("{playername}", playerName)
				);
				return true;
			}

			new PlayerInfo(player, target, player::closeInventory);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(Homestead.getInstance().getOfflinePlayersSync().stream().map(OfflinePlayer::getName).toList());
		}

		return suggestions;
	}
}
