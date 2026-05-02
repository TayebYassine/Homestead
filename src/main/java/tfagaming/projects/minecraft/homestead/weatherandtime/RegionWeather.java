package tfagaming.projects.minecraft.homestead.weatherandtime;

import java.util.List;

public final class RegionWeather {
	public final static int SERVER = 0;
	public final static int CLEAR = 1;
	public final static int RAIN = 2;
	private RegionWeather() {
	}

	public static List<String> getAll() {
		return List.of("Server", "Clear", "Rain");
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

	public static int parse(String weather) {
		return switch (weather) {
			case "Server" -> SERVER;
			case "Clear" -> CLEAR;
			case "Rain" -> RAIN;
			default -> -1;
		};
	}
}
