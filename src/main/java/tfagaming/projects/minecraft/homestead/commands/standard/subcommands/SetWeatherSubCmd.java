package tfagaming.projects.minecraft.homestead.commands.standard.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.commands.SubCommandBuilder;
import tfagaming.projects.minecraft.homestead.managers.LogManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.weatherandtime.RegionTime;
import tfagaming.projects.minecraft.homestead.weatherandtime.RegionWeather;

import java.util.ArrayList;
import java.util.List;

public class SetWeatherSubCmd extends SubCommandBuilder {
	public SetWeatherSubCmd() {
		super("setweather");
		setUsage("/region setweather [weather]");
	}

	@Override
	public boolean onExecution(CommandSender sender, String[] args) {
		Player player = asPlayer(sender);

		if (player == null) {
			sender.sendMessage("This command can only be used by players.");
			return true;
		}

		if (!player.hasPermission("homestead.region.weather")) {
			Messages.send(player, 210);
			return true;
		}

		Region region = TargetRegionSession.getRegion(player);

		if (region == null) {
			Messages.send(player, 4);
			return true;
		}

		if (args.length < 1) {
			Messages.send(player, 0, new Placeholder()
					.add("{usage}", getUsage())
			);
			return true;
		}

		String weatherInput = args[0];
		int weather = RegionWeather.parse(weatherInput);

		if (weather == -1) {
			Messages.send(player, 219);
			return true;
		}

		region.setWeather(weather);

		Messages.send(player, 220, new Placeholder()
				.add("{weather-name}", weatherInput)
		);

		LogManager.addLog(region, player, LogManager.PredefinedLog.UPDATE_WEATHER);

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
