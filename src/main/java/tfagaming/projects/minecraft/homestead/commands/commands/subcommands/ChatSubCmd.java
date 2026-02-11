package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ChatSubCmd extends SubCommandBuilder {
	public ChatSubCmd() {
		super("chat");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (!player.hasPermission("homestead.region.chat")) {
			PlayerUtils.sendMessage(player, 8);
			return true;
		}

		if (args.length < 2) {
			PlayerUtils.sendMessage(player, 0);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			PlayerUtils.sendMessage(player, 4);
			return true;
		}

		List<String> messageList = Arrays.asList(args).subList(1, args.length);
		String message = String.join(" ", messageList);

		List<UUID> playerIds = new ArrayList<>();

		playerIds.add(region.getOwnerId());

		for (SerializableMember member : region.getMembers()) {
			playerIds.add(member.getPlayerId());
		}

		for (UUID playerId : playerIds) {
			OfflinePlayer regionPlayer = Homestead.getInstance().getOfflinePlayerSync(playerId);

			if (regionPlayer != null && regionPlayer.isOnline()) {
				((Player) regionPlayer).sendMessage(Formatters.formatPrivateChat(region.getDisplayName(), player.getName(), message));
			}
		}

		boolean logToConsole = Homestead.config.getBoolean("log-private-chat");

		if (logToConsole) {
			Logger.info(String.format("[Chat] %s (UUID: %s) -> %s: %s", player.getName(), player.getUniqueId(), region.getName(), message));
		}

		return true;
	}
}
