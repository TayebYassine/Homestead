package tfagaming.projects.minecraft.homestead.tools.minecraft.platform;

public final class PlatformBridge {

	private static final IPlatformBridge INSTANCE;
	private static boolean ADVENTURE_CLASS_PRESENT;

	static {
		try {
			Class.forName("net.kyori.adventure.text.Component");
			ADVENTURE_CLASS_PRESENT = true;
		} catch (ClassNotFoundException e) {
			ADVENTURE_CLASS_PRESENT = false;
		}

		INSTANCE = ADVENTURE_CLASS_PRESENT
				? new AdventurePlatformBridge()
				: new LegacyPlatformBridge();
	}

	private PlatformBridge() {
	}

	public static IPlatformBridge get() {
		return INSTANCE;
	}

	public static boolean isAdventureClassPresent() {
		return ADVENTURE_CLASS_PRESENT;
	}
}