package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class TransferOwnershipSubCmd extends SubCommandBuilder {
	public TransferOwnershipSubCmd() {
		super("transfer");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 3) {
			Messages.send(sender, 0);
			return true;
		}

		String regionName = args[1];

		Region region = RegionsManager.findRegion(regionName);

		if (region == null) {
			Messages.send(sender, 9);
			return true;
		}

		String playerName = args[2];

		OfflinePlayer target = Homestead.getInstance().getOfflinePlayerSync(playerName);

		if (target == null) {
			Messages.send(sender, 29, new Placeholder()
					.add("{playername}", playerName)
			);
			return true;
		}

		if (region.isOwner(target.getUniqueId())) {
			Messages.send(sender, 192);
			return false;
		}

		if (region.isPlayerBanned(target)) {
			region.unbanPlayer(target);
		}

		region.setOwner(target);

		if (region.isPlayerMember(target)) region.removeMember(target);
		if (region.isPlayerInvited(target)) region.removePlayerInvite(target);

		Messages.send(sender, 193, new Placeholder()
				.add("{region}", region.getName())
				.add("{player}", target.getName())
		);

		return true;
	}
}
