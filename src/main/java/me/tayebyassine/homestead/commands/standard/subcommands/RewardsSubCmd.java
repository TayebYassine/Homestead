package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.gui.menus.Rewards;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.tools.minecraft.chat.Messages;

import java.util.List;

public class RewardsSubCmd extends SubCommandBuilder {
	public RewardsSubCmd() {
		super("rewards");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName()
		));
		setUsage("/hs rewards");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		if (!Resources.<RegionsFile>get(ResourceType.Regions).isRewardsEnabled()) {
			Messages.send(player, "commands.rewards.0");
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.rewards.1");
			return true;
		}

		new Rewards(player, region, player::closeInventory);

		return true;
	}
}
