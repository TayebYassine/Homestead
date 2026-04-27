package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.database.Database;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class PluginSubCmd extends SubCommandBuilder {
	public PluginSubCmd() {
		super("plugin", null, false);
		setUsage("/hsadmin plugin");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (sender instanceof Player p) {
			Messages.send(p, 216);
		}

		Logger.info("Please wait...");

		String[] headers = {"Property", "Value"};

		Object[][] data = {
				{"Software", Bukkit.getName()},
				{"Version", Bukkit.getVersion()},
				{"Players", Bukkit.getOnlinePlayers().size()},
				{"Homestead", "v" + Homestead.getVersion()},
				{"Database Provider", Homestead.database.getProvider().toString()},
				{"Database Latency", Homestead.database.getLatency() + "ms"},
				{"Cache Latency", Database.getCacheLatency() + "ms"},
				{"Data - Regions", RegionManager.getRegionCount()},
				{"Data - Members", MemberManager.getMemberCount()},
				{"Data - Chunks", ChunkManager.getChunkCount()},
				{"Data - Invites", InviteManager.getInviteCount()},
				{"Data - Logs", LogManager.getLogCount()},
				{"Data - Rates", RateManager.getRateCount()},
				{"Data - Bans", BanManager.getBanCount()},
				{"Data - Levels", LevelManager.getLevelCount()},
				{"Data - Wars", WarManager.getWarCount()},
				{"Data - SubAreas", SubAreaManager.getSubAreaCount()},
		};

		ListUtils.printTable(headers, data);

		return true;
	}
}