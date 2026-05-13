package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.MemberManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.ClaimFlySession;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;

import java.util.List;

public class FlySubCmd extends SubCommandBuilder {
	public FlySubCmd() {
		super("fly");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.fly"
		));
		setUsage("/region fly");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Chunk chunk = player.getLocation().getChunk();
		Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (region == null || (!PlayerUtility.isOperator(player) && !(region.isOwner(player) || MemberManager.isMemberOfRegion(region, player)))) {
			reply(player, "fly.0");
			return true;
		}

		if (ClaimFlySession.hasSession(player)) {
			ClaimFlySession.removeSession(player);

			player.setAllowFlight(false);
			player.setFlying(false);

			reply(player, "fly.2");
		} else {
			ClaimFlySession.newSession(player);

			player.setAllowFlight(true);
			player.setFlying(true);

			reply(player, "fly.1");
		}

		return true;
	}
}
