package tfagaming.projects.minecraft.homestead.weatherandtime;

import java.util.List;

public class WeatherType {
	public final static int SERVER = 0;
	public final static int CLEAR = 1;
	public final static int RAIN = 2;

	public static List<String> getAll() {
		return List.of("server", "clear", "rain");
	}

	public static int next(int current) {
		return switch (current) {
			case SERVER -> CLEAR;
			case CLEAR -> RAIN;
			case RAIN -> SERVER;
			default -> SERVER;
		};
	}

	public static String from(int weather) {
		return switch (weather) {
			case SERVER -> "Server";
			case CLEAR -> "Clear";
			case RAIN -> "Rain";
			default -> "Server";
		};
	}
}
