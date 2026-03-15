package tfagaming.projects.minecraft.homestead.tools.minecraft.platform;

public final class PlatformBridge {

	private static final IPlatformBridge INSTANCE;

	static {
		boolean adventurePresent;
		try {
			Class.forName("net.kyori.adventure.text.Component");
			adventurePresent = true;
		} catch (ClassNotFoundException e) {
			adventurePresent = false;
		}

		INSTANCE = adventurePresent
				? new AdventurePlatformBridge()
				: new LegacyPlatformBridge();
	}

	private PlatformBridge() {}

	public static IPlatformBridge get() {
		return INSTANCE;
	}
}