package me.tayebyassine.homestead.listeners.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public final class Projectiles {
    private Projectiles() {
        throw new AssertionError("Uninstantiable class");
    }

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
}
