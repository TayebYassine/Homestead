package tfagaming.projects.minecraft.homestead.weatherandtime;

import java.util.List;

public final class RegionTime {
	public final static int SERVER = 0;
	public final static int SUNRISE = 23500;
	public final static int MORNING = 1000;
	public final static int NOON = 6000;
	public final static int AFTERNOON = 9000;
	public final static int SUNSET = 13000;
	public final static int NIGHT = 15000;
	public final static int MIDNIGHT = 18000;
	private RegionTime() {
	}

	public static List<String> getAll() {
		return List.of("Server", "Sunrise", "Morning", "Noon", "Afternoon", "Sunset", "Night", "Midnight");
	}

	public static int next(int current) {
		return switch (current) {
			case SERVER -> SUNRISE;
			case SUNRISE -> MORNING;
			case MORNING -> NOON;
			case NOON -> AFTERNOON;
			case AFTERNOON -> SUNSET;
			case SUNSET -> NIGHT;
			case NIGHT -> MIDNIGHT;
			case MIDNIGHT -> SERVER;
			default -> SERVER;
		};
	}

	public static String from(int time) {
		return switch (time) {
			case SERVER -> "Server";
			case SUNRISE -> "Sunrise";
			case MORNING -> "Morning";
			case NOON -> "Noon";
			case AFTERNOON -> "Afternoon";
			case SUNSET -> "Sunset";
			case NIGHT -> "Night";
			case MIDNIGHT -> "Midnight";
			default -> "Server";
		};
	}

	public static int parse(String time) {
		return switch (time) {
			case "Server" -> SERVER;
			case "Sunrise" -> SUNRISE;
			case "Morning" -> MORNING;
			case "Noon" -> NOON;
			case "Afternoon" -> AFTERNOON;
			case "Sunset" -> SUNSET;
			case "Night" -> NIGHT;
			case "Midnight" -> MIDNIGHT;
			default -> -1;
		};
	}
}
