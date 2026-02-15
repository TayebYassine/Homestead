package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionMenu;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionsMenu;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class MenuSubCmd extends SubCommandBuilder {
	public MenuSubCmd() {
		super("menu");
		setUsage("/region menu");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by players.");
			return true;
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
