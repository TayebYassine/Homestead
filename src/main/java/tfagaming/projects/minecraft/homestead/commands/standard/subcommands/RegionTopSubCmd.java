package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.TopBankRegionsMenu;

public class RegionTopSubCmd extends LegacySubCommandBuilder {
	public RegionTopSubCmd() {
		super("top");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		new TopBankRegionsMenu(player);

		return true;
	}
}
