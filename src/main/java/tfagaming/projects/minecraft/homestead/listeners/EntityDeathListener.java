package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.LevelManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LevelsFile;
import tfagaming.projects.minecraft.homestead.sessions.TargetRegionSession;

import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;

import java.util.HashSet;
import java.util.UUID;

public final class EntityDeathListener implements Listener {
	private static final HashSet<UUID> COOLDOWN = new HashSet<UUID>();

	private static String getBeautifulName(Entity entity) {
		String rawName = entity.getType().name();
		String[] words = rawName.split("_");
		StringBuilder nameBuilder = new StringBuilder();

		for (String word : words) {
			nameBuilder.append(word.substring(0, 1).toUpperCase())
					.append(word.substring(1).toLowerCase())
					.append(" ");
		}

		return nameBuilder.toString().trim();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		boolean levelsEnabled = Resources.<LevelsFile>get(ResourceType.Levels).isEnabled();

		if (!levelsEnabled) {
			return;
		}

		LivingEntity entity = event.getEntity();
		Location location = entity.getLocation();
		Chunk chunk = location.getChunk();
		EntityType type = entity.getType();
		Player killer = entity.getKiller();

		if (entity instanceof Player || killer == null || COOLDOWN.contains(killer.getUniqueId())) {
			return;
		}

		if (!LevelRewards.hasEntityKillReward(type)) {
			return;
		}

		int[] xpRange = LevelRewards.getEntityKillReward(type);

		if (xpRange == null) {
			return;
		}

		Region region = TargetRegionSession.getRegion(killer);

		if (region == null || !region.isOwner(killer)) {
			return;
		}

		if (!(entity instanceof EnderDragon) && !ChunkManager.isChunkClaimedByRegion(region, chunk)) {
			return;
		}

		long amount = LevelManager.addRandomXp(region.getUniqueId(), xpRange[0], xpRange[1]);

		Messages.send(killer, 198, new Placeholder()
				.add("{region}", region.getName())
				.add("{entity}", getBeautifulName(entity))
				.add("{xp}", NumberUtils.convertToBalance(amount))
		);

		killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

		COOLDOWN.add(killer.getUniqueId());
		Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(killer.getUniqueId()), 2);
	}
}
