package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class DenyInviteSubCmd extends LegacySubCommandBuilder {
	public DenyInviteSubCmd() {
		super("deny");
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

		region.removePlayerInvite(player);

		Messages.send(player, 47, new Placeholder()
				.add("{region}", region.getName())
		);

		return true;
	}
}
