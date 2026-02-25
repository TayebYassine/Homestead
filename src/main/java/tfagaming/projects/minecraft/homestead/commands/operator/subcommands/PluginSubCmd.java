package tfagaming.projects.minecraft.homestead.commands.operator.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.LevelManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public class PluginSubCmd extends SubCommandBuilder {
	public PluginSubCmd() {
		super("plugin", null, false);
		setUsage("/hsadmin plugin");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Messages.send(sender, 89, new Placeholder()
					.add("{plugin-version}", Homestead.getVersion())
					.add("{regions}", RegionManager.getAll().size())
					.add("{wars}", WarManager.getAll().size())
					.add("{subareas}", SubAreaManager.getAll().size())
					.add("{levels}", LevelManager.getAll().size())
					.add("{provider}", Homestead.database.getSelectedProvider())
					.add("{avg-response-db}", Homestead.database.getLatency())
					.add("{avg-response-cache}", Homestead.database.getLatency())
			);
		} else {
			sender.sendMessage("Please wait...");

			String[] headers = {"Property", "Value"};

			Object[][] data = {
					{"Software", Bukkit.getName()},
					{"Version", Bukkit.getVersion()},
					{"Players", Bukkit.getOnlinePlayers().size()},
					{"Homestead", "v" + Homestead.getVersion()},
					{"Regions", RegionManager.getAll().size()},
					{"Wars", WarManager.getAll().size()},
					{"Sub-Areas", SubAreaManager.getAll().size()},
					{"Levels", LevelManager.getAll().size()},
					{"Provider", Homestead.database.getSelectedProvider()},
					{"Database Latency", Homestead.database.getLatency() + "ms"},
					{"Cache Latency", Homestead.database.getLatency() + "ms"}
			};

			ListUtils.printTable(headers, data);
		}

		return true;
	}
}