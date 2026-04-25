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

public class FlySubCmd extends SubCommandBuilder {
	public FlySubCmd() {
		super("fly");
		setUsage("/region fly");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.fly")) {
			Messages.send(player, 8);
			return true;
		}

		Chunk chunk = player.getLocation().getChunk();
		Region region = ChunkManager.getRegionOwnsTheChunk(chunk);

		if (region == null || (!PlayerUtility.isOperator(player) && !(region.isOwner(player) || MemberManager.isMemberOfRegion(region, player)))) {
			Messages.send(player, 207);
			return true;
		}

		if (ClaimFlySession.hasSession(player)) {
			ClaimFlySession.removeSession(player);

			player.setAllowFlight(false);
			player.setFlying(false);

			Messages.send(player, 206);
		} else {
			ClaimFlySession.newSession(player);

			player.setAllowFlight(true);
			player.setFlying(true);

			Messages.send(player, 205);
		}

		return true;
	}
}
