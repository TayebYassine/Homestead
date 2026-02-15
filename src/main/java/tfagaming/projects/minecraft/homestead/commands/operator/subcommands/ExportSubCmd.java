package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.database.Database;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.io.IOException;
import java.sql.SQLException;
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

		String provider = args[0];

		if (Database.parseProviderFromString(provider) == null) {
			Messages.send(sender, 84);
			return true;
		}

		String currentProvider = Homestead.database.getSelectedProvider();

		if (currentProvider.equalsIgnoreCase(provider)) {
			Messages.send(sender, 85);
			return true;
		}

		try {
			Database.Provider providerParsed = Database.parseProviderFromString(provider);

			if (providerParsed == null) {
				throw new IllegalStateException("Database provider not found.");
			}

			Database instance = new Database(providerParsed);

			instance.exportRegions();
			instance.exportWars();
			instance.exportSubAreas();
			instance.exportLevels();

			Messages.send(sender, 86, new Placeholder()
					.add("{regions}", RegionsManager.getAll().size())
					.add("{wars}", WarsManager.getAll().size())
					.add("{subareas}", SubAreasManager.getAll().size())
					.add("{current-provider}", currentProvider)
					.add("{selected-provider}", provider)
			);

			instance.closeConnection();
		} catch (ClassNotFoundException | SQLException | IOException e) {
			Logger.error(e);
			Messages.send(sender, 87);
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(List.of("SQLite", "MySQL", "YAML", "PostgreSQL", "MariaDB"));
		}

		return suggestions;
	}
}