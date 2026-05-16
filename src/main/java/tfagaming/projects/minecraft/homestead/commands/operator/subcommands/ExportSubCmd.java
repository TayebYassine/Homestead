package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.database.Database;
import tfagaming.projects.minecraft.homestead.database.Driver;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.ArrayList;
import java.util.List;

public class ExportSubCmd extends SubCommandBuilder {
	public ExportSubCmd() {
		super("export");
		setPermission(List.of(
				"homestead.commands.homesteadadmin",
				"homestead.commands.homesteadadmin." + getName()
		));
		setUsage("/hsadmin export [provider]");
		setConsoleOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 1) {
			Logger.error("Insufficient arguments, usage: ", getUsage());
			return true;
		}

		String providerInput = args[0];
		Driver provider = Driver.parse(providerInput);

		if (provider == null) {
			Logger.error("Incorrect provider provided.");
			return true;
		}

		if (Homestead.database.getProvider() == provider) {
			Logger.error("Provider already in use.");
			return true;
		}

		try {
			Logger.info("Please wait...");
			Logger.warning("The data exporter is asynchronous, please do NOT shutdown your server until you see \"Done.\"!");

			final Database instance = new Database(provider);

			Homestead.getInstance().runAsyncTask(() -> {
				try {
					instance.exportFromCache();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				String[] headers = {"Model", "Exported"};

				Object[][] data = {
						{"Regions", RegionManager.getRegionCount()},
						{"Members", MemberManager.getMemberCount()},
						{"Chunks", ChunkManager.getChunkCount()},
						{"Invites", InviteManager.getInviteCount()},
						{"Logs", LogManager.getLogCount()},
						{"Rates", RateManager.getRateCount()},
						{"Bans", BanManager.getBanCount()},
						{"Levels", LevelManager.getLevelCount()},
						{"Wars", WarManager.getWarCount()},
						{"SubAreas", SubAreaManager.getSubAreaCount()},
				};

				ListUtils.printTable(headers, data);

				Logger.info("Done.");

				try {
					instance.closeConnection();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		} catch (Exception e) {
			Logger.error(e);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(List.of("SQLite", "MySQL", "YAML", "PostgreSQL", "MariaDB", "MongoDB"));
		}

		return suggestions;
	}
}