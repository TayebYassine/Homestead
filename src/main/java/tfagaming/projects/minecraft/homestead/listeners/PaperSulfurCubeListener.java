package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldRules;
import tfagaming.projects.minecraft.homestead.listeners.util.RegionProtection;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;

public final class PaperSulfurCubeListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPunchSulfurCube(PlayerAnimationEvent event) {
		if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
			return;
		}

		Player player = event.getPlayer();

		Entity targetEntity = getTargetEntity(player, player.getGameMode() == GameMode.CREATIVE ? 5 : 3);
		if (targetEntity == null) {
			return;
		}

		if (!targetEntity.getType().name().equals("SULFUR_CUBE")) {
			return;
		}

		if (!hasAbsorbedBlock(targetEntity)) {
			return;
		}

		Location location = targetEntity.getLocation();
		Chunk chunk = location.getChunk();

		if (!ChunkManager.isChunkClaimed(chunk)) {
			if (!WorldRules.isPlayerFlagAllowed(chunk.getWorld(), PlayerFlags.PUNCH_SULFUR_CUBES)) {
				cancelAndFreeze(player, targetEntity);
				return;
			}
		}

		RegionProtection.hasPermission(player, chunk, location, PlayerFlags.PUNCH_SULFUR_CUBES, null, () -> {
			cancelAndFreeze(player, targetEntity);
		});
	}

	private void cancelAndFreeze(Player player, Entity target) {
		Location frozenLocation = target.getLocation().clone();

		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority = EventPriority.HIGHEST)
			public void onDamage(EntityDamageByEntityEvent e) {
				if (!e.getDamager().equals(player) || !e.getEntity().equals(target)) {
					return;
				}
				e.setCancelled(true);
				EntityDamageByEntityEvent.getHandlerList().unregister(this);
			}
		}, Homestead.getInstance());

		Homestead.getInstance().runSyncTask(() -> {
			if (target.isValid()) {
				target.teleport(frozenLocation);
				target.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
			}
		});
	}

	private Entity getTargetEntity(Player player, double maxDistance) {
		Location eyeLocation = player.getEyeLocation();
		org.bukkit.util.@NotNull Vector direction = eyeLocation.getDirection().normalize();

		for (Entity entity : player.getNearbyEntities(maxDistance, maxDistance, maxDistance)) {
			if (entity.equals(player)) continue;

			String type = entity.getType().name();
			if (!type.equals("SULFUR_CUBE")) {
				continue;
			}

			if (isLookingAt(player, entity, maxDistance)) {
				return entity;
			}
		}

		return null;
	}

	/**
	 * Checks if a player is looking at a specific entity.
	 */
	private boolean isLookingAt(Player player, Entity entity, double maxDistance) {
		Location eyeLocation = player.getEyeLocation();
		org.bukkit.util.@NotNull Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector());
		double distance = toEntity.length();

		if (distance > maxDistance) {
			return false;
		}

		org.bukkit.util.@NotNull Vector direction = eyeLocation.getDirection().normalize();
		org.bukkit.util.@NotNull Vector toEntityNormalized = toEntity.normalize();

		double dot = direction.dot(toEntityNormalized);

		double tolerance = Math.max(0.85, 1.0 - (0.3 / distance));

		return dot >= tolerance;
	}

	/**
	 * Checks if a sulfur cube has absorbed a block inside it.
	 */
	private boolean hasAbsorbedBlock(Entity entity) {
		if (!(entity instanceof LivingEntity living)) {
			return false;
		}

		EntityEquipment equipment = living.getEquipment();
		if (equipment == null) {
			return false;
		}

		ItemStack bodyItem = equipment.getItem(org.bukkit.inventory.EquipmentSlot.BODY);
		return bodyItem != null && !bodyItem.getType().isAir();
	}
}
