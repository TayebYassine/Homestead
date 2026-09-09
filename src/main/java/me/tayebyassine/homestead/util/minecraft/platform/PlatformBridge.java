package me.tayebyassine.homestead.util.minecraft.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class PlatformBridge {

    private static final IPlatformBridge INSTANCE;
    private static final boolean ADVENTURE_CLASS_PRESENT;

    static {
        ADVENTURE_CLASS_PRESENT = hasAdventureSupport();
        INSTANCE = ADVENTURE_CLASS_PRESENT
                ? new AdventurePlatformBridge()
                : new LegacyPlatformBridge();
    }

    private PlatformBridge() {
    }

    private static boolean hasAdventureSupport() {
        try {
            Player.class.getMethod("sendMessage", Component.class);
            return true;
        } catch (NoSuchMethodException | NoClassDefFoundError e) {
            return false;
        }
    }

    public static IPlatformBridge get() {
        return INSTANCE;
    }

    public static boolean isAdventureClassPresent() {
        return ADVENTURE_CLASS_PRESENT;
    }
}