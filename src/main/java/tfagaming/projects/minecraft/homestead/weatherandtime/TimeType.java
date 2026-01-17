package tfagaming.projects.minecraft.homestead.weatherandtime;

import java.util.List;

public class TimeType {
	public final static int SERVER = 0;
	public final static int MORNING = 1;
	public final static int NOON = 6000;
	public final static int AFTERNOON = 9000;
	public final static int SUNSET = 12000;
	public final static int NIGHT = 18000;

	public static List<String> getAll() {
		return List.of("server", "morning", "noon", "afternoon", "sunset", "night");
	}

	public static int next(int current) {
		return switch (current) {
			case SERVER -> MORNING;
			case MORNING -> NOON;
			case NOON -> AFTERNOON;
			case AFTERNOON -> SUNSET;
			case SUNSET -> NIGHT;
			case NIGHT -> SERVER;
			default -> SERVER;
		};
	}

	public static String from(int time) {
		return switch (time) {
			case SERVER -> "Server";
			case MORNING -> "Morning";
			case NOON -> "Noon";
			case AFTERNOON -> "Afternoon";
			case SUNSET -> "Sunset";
			case NIGHT -> "Night";
			default -> "Server";
		};
	}
}
