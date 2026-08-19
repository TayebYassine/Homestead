package me.tayebyassine.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.tayebyassine.homestead.commands.SubCommandBuilder;
import me.tayebyassine.homestead.managers.LogManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.sessions.TargetRegionSession;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.weatherandtime.RegionWeather;

import java.util.ArrayList;
import java.util.List;

public class SetWeatherSubCmd extends SubCommandBuilder {
	public SetWeatherSubCmd() {
		super("setweather");
		setPermission(List.of(
				"homestead.commands.region",
				"homestead.commands.region." + getName(),
				"homestead.actions.regions.update.weather"
		));
		setUsage("/hs setweather [weather]");
		setPlayerOnly();
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return false;

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, "commands.setweather.0");
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, "commands.setweather.1");
			return true;
		}

		String weatherInput = args[0];
		int weather = RegionWeather.parse(weatherInput);

		if (weather == -1) {
			Messages.send(player, "commands.setweather.2");
			return true;
		}

		int newWeather = RegionWeather.next(region.getWeather());

		region.setWeather(newWeather);

		Messages.send(player, "commands.setweather.3", weatherInput);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_WEATHER, RegionWeather.from(newWeather));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);
		if (player == null) return new ArrayList<>();

		List<String> suggestions = new ArrayList<>();

		if (args.length == 1) {
			suggestions.addAll(RegionWeather.getAll());
		}

		return suggestions;
	}
}
