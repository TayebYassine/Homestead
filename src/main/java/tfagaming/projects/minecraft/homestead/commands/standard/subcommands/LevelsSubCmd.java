package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.gui.menus.RegionLevels;
import tfagaming.projects.minecraft.homestead.managers.LevelManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LevelsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class LevelsSubCmd extends SubCommandBuilder {
	public LevelsSubCmd() {
		super("levels");
		setUsage("/region levels");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		boolean levelsEnabled = Resources.<LevelsFile>get(ResourceType.Levels).isEnabled();

		if (!levelsEnabled) {
			Messages.send(player, 197);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		LevelManager.getOrCreateLevel(region.getUniqueId());

		new RegionLevels(player, region, player::closeInventory);

		return true;
	}
}
