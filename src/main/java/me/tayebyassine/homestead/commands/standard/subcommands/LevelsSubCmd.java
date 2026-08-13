package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.RegionLevels;
import me.tayebyassine.homestead.managers.LevelManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.LevelsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class LevelsSubCmd extends SubCommandBuilder {
	public LevelsSubCmd() {
		super("levels");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs levels");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (!Resources.<LevelsFile>get(ResourceType.Levels).isEnabled()) {
			Messages.send(player, "commands.levels.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.levels.1");
			return true;
		}

		LevelManager.getOrCreateLevel(region.getUniqueId());

		new RegionLevels(player, region, player::closeInventory);

		return true;
	}
}
