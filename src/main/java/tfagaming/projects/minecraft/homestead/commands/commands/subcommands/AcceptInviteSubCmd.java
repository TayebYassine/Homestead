package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.limits.Limits;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class AcceptInviteSubCmd extends SubCommandBuilder {
	public AcceptInviteSubCmd() {
		super("accept");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		if (args.length < 2) {
			Messages.send(player, 0);
			return true;
		}

		String regionName = args[1];

		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			Messages.send(player, 9);
			return false;
		}

		if (!region.isPlayerInvited(player)) {
			Messages.send(player, 45, new Placeholder()
					.add("{region}", region.getName())
			);
			return true;
		}

		if (Limits.hasReachedLimit(null, region, Limits.LimitType.MEMBERS_PER_REGION)) {
			Messages.send(player, 116);
			return true;
		}

		region.removePlayerInvite(player);

		region.addMember(player);

		Messages.send(player, 46, new Placeholder()
				.add("{region}", region.getName())
				.add("{playername}", player.getName())
		);

		Messages.send(region.getOwner().getPlayer(), 138, new Placeholder()
				.add("{region}", region.getName())
				.add("{playername}", player.getName())
		);

		return true;
	}
}
