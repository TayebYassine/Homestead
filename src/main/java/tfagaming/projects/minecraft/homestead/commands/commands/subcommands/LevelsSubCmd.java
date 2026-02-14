package tfagaming.projects.minecraft.homestead.commands.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionLevelMenu;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class LevelsSubCmd extends SubCommandBuilder {
	public LevelsSubCmd() {
		super("levels");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("You cannot use this command via the console.");
			return false;
		}

		boolean levelsEnabled = Homestead.config.getBoolean("levels.enabled");

		if (!levelsEnabled) {
			Messages.send(player, 197);
			return false;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		LevelsManager.getOrCreateLevel(region.getUniqueId());

		new RegionLevelMenu(player, region, player::closeInventory);

		return true;
	}
}
