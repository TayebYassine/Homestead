package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.command.CommandSender;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.database.Database;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ExportSubCmd extends SubCommandBuilder {
	public ExportSubCmd() {
		super("export");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (args.length < 2) {
			PlayerUtils.sendMessage(sender, 0);
			return true;
		}

		String provider = args[1];

		if (Database.parseProviderFromString(provider) == null) {
			PlayerUtils.sendMessage(sender, 84);
			return true;
		}

		String currentProvider = Homestead.database.getSelectedProvider();

		if (currentProvider.equalsIgnoreCase(provider)) {
			PlayerUtils.sendMessage(sender, 85);
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

			Map<String, String> replacements = new HashMap<>();
			replacements.put("{regions}", String.valueOf(Homestead.regionsCache.getAll().size()));
			replacements.put("{wars}", String.valueOf(Homestead.warsCache.getAll().size()));
			replacements.put("{current-provider}", currentProvider);
			replacements.put("{selected-provider}", provider);

			PlayerUtils.sendMessage(sender, 86, replacements);

			instance.closeConnection();
		} catch (ClassNotFoundException | SQLException | IOException e) {
			Logger.error(e);
			PlayerUtils.sendMessage(sender, 87);
		}

		return true;
	}
}
