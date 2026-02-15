package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.LegacySubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionMenu;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionsMenu;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class MenuSubCmd extends LegacySubCommandBuilder {
	public MenuSubCmd() {
		super("menu");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			new RegionsMenu(player);
		} else {
			new RegionMenu(player, region);
		}

		return true;
	}
}
