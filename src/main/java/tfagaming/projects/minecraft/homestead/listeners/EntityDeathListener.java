package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.LevelsManager;
import tfagaming.projects.minecraft.homestead.sessions.targetedregion.TargetRegionSession;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.NumberUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.tools.minecraft.rewards.LevelRewards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class EntityDeathListener implements Listener {
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
		LivingEntity entity = event.getEntity();
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

		long amount = LevelsManager.addRandomXp(region.getUniqueId(), xpRange[0], xpRange[1]);

		Map<String, String> replacements = new HashMap<>();
		replacements.put("{region}", region.getName());
		replacements.put("{entity}", getBeautifulName(entity));
		replacements.put("{xp}", NumberUtils.convertToBalance(amount));

		PlayerUtils.sendMessage(killer, 198, replacements);

		killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

		COOLDOWN.add(killer.getUniqueId());
		Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(killer.getUniqueId()), 5);
	}
}
