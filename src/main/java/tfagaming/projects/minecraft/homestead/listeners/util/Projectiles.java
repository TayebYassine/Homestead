package tfagaming.projects.minecraft.homestead.listeners.util;

import org.bukkit.entity.*;

public final class Projectiles {
	public static boolean isProjectile(Entity e) {
		return e instanceof Projectile;
	}

	public static boolean isPlayerProjectile(Entity e) {
		return e instanceof Projectile p &&
				p.getShooter() instanceof Player;
	}

	public static boolean isMobProjectile(Entity e) {
		return e instanceof Projectile p &&
				p.getShooter() instanceof LivingEntity &&
				!(p.getShooter() instanceof Player);
	}

	private Projectiles() {}
}
