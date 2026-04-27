package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
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
		super("export", null, false);
		setUsage("/hsadmin export [provider]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 1) {
			Messages.send(sender, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String providerInput = args[0];
		Driver provider = Driver.parse(providerInput);

		if (provider == null) {
			Messages.send(sender, 84);
			return true;
		}

		if (Homestead.database.getProvider() == provider) {
			Messages.send(sender, 85);
			return true;
		}

		try {
			Database instance = new Database(provider);

			for (int i = 0; i < 5; i++) {
				Logger.warning("[Export] THE SERVER MAY LAG DUE TO MANY DATA MODELS EXISTING ON THIS SERVER.");
				Logger.warning("[Export] IGNORE THE FOLLOWING WARNINGS/ERRORS.");
			}

			instance.exportFromCache();

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

			Messages.send(sender, 86, new Placeholder()
					.add("{regions}", RegionManager.getAll().size())
					.add("{wars}", WarManager.getAll().size())
					.add("{subareas}", SubAreaManager.getAll().size())
					.add("{current-provider}", provider.toString())
					.add("{selected-provider}", provider)
			);

			instance.closeConnection();
		} catch (Exception e) {
			Logger.error(e);
			Messages.send(sender, 87);
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