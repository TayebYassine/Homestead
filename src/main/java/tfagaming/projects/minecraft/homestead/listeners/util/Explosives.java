package tfagaming.projects.minecraft.homestead.listeners.util;

import org.bukkit.entity.*;

public final class Explosives {
	public static boolean isExplosive(Entity e) {
		if (e == null) return false;

		if (e instanceof TNTPrimed) return true;
		if (e instanceof Creeper) return true;
		if (e instanceof Fireball) return true;
		if (e instanceof WitherSkull) return true;
		if (e instanceof EnderCrystal) return true;
		if (e instanceof WindCharge) return true;

		if (e instanceof Player) {
			return false;
		}

		return switch (e.getType()) {
			case TNT, TNT_MINECART, CREEPER, FIREBALL, WITHER_SKULL, END_CRYSTAL, WIND_CHARGE -> true;
			default -> false;
		};
	}

	private Explosives() {}
}
