package tfagaming.projects.minecraft.homestead.commands.commands.subcommands.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreasManager;
import tfagaming.projects.minecraft.homestead.managers.WarsManager;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

import java.util.HashMap;
import java.util.Map;

public class PluginSubCmd extends SubCommandBuilder {
	public PluginSubCmd() {
		super("plugin");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Messages.send(sender, 89, new Placeholder()
					.add("{plugin-version}", Homestead.getVersion())
					.add("{regions}", RegionsManager.getAll().size())
					.add("{wars}", WarsManager.getAll().size())
					.add("{subareas}", SubAreasManager.getAll().size())
					.add("{provider}", Homestead.database.getSelectedProvider())
					.add("{avg-response-db}", Homestead.database.getLatency())
					.add("{avg-response-cache}", Homestead.regionsCache.getLatency())
			);
		} else {
			sender.sendMessage("Please wait...");

			String[] headers = {"Property", "Value"};

			Object[][] data = {
					{"Software", Bukkit.getName()},
					{"Version", Bukkit.getVersion()},
					{"Players", Bukkit.getOnlinePlayers().size()},
					{"Homestead", "v" + Homestead.getVersion()},
					{"Regions", RegionsManager.getAll().size()},
					{"Wars", WarsManager.getAll().size()},
					{"Sub-Areas", SubAreasManager.getAll().size()},
					{"Database", Homestead.database.getSelectedProvider()},
					{"Latency", Homestead.database.getLatency() + " ms"},
					{"Latency (cache)", Homestead.regionsCache.getLatency() + " ms"}
			};

			ListUtils.printTable(headers, data);
		}

		return true;
	}
}
