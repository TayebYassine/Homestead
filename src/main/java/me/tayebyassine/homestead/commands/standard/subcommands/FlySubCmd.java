package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.ClaimFlySession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;

import java.util.List;

public class FlySubCmd extends SubCommandBuilder {
	public FlySubCmd() {
		super("fly");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.fly"
		));
		setUsage("/hs fly");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Chunk chunk = player.getLocation().getChunk();
		Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (region == null || (!PlayerUtility.isOperator(player) && !(region.isOwner(player) || MemberManager.isMemberOfRegion(region, player)))) {
			Messages.send(player, "commands.fly.0");
			return true;
		}

		if (ClaimFlySession.hasSession(player)) {
			ClaimFlySession.removeSession(player);

			player.setAllowFlight(false);
			player.setFlying(false);

			Messages.send(player, "commands.fly.2");
		} else {
			ClaimFlySession.newSession(player);

			player.setAllowFlight(true);
			player.setFlying(true);

			Messages.send(player, "commands.fly.1");
		}

		return true;
	}
}
