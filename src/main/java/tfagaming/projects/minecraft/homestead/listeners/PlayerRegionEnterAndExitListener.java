package tfagaming.projects.minecraft.homestead.listeners;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.ConfigFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import tfagaming.projects.minecraft.homestead.weatherandtime.TimeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerRegionEnterAndExitListener implements Listener {
	private static final Map<UUID, UUID> sessions = new HashMap<UUID, UUID>();

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Chunk chunk = player.getLocation().getChunk();

		if (!(event.getFrom().getBlockX() != event.getTo().getBlockX()
				|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()
				|| event.getFrom().getBlockY() != event.getTo().getBlockY())) {
			return;
		}

		boolean isFeatureEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("enter-exit-region-message.enabled");

		if (ChunkManager.isChunkClaimed(chunk)) {
			// Player enters a region

			Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
			assert region != null;

			if (sessions.containsKey(player.getUniqueId())
					&& sessions.get(player.getUniqueId()).equals(region.getUniqueId())) {
				return;
			}

			if (!PlayerUtils.isOperator(player) && region.isPlayerBanned(player)) {
				Chunk nearbyChunk = ChunkManager.findNearbyUnclaimedChunk(player);

				if (nearbyChunk != null) {
					PlayerUtils.teleportPlayerToChunk(player, nearbyChunk);
				}

				Messages.send(player, 28, new Placeholder()
						.add("{region}", region.getName())
						.add("{ban-reason}", region.getBannedPlayer(player).getReason())
				);

				return;
			}

			if (!PlayerUtils.isOperator(player) && !region.isOwner(player)
					&& !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true) && !WarManager.isRegionInWar(region.getOwnerId())) {
				Chunk nearbyChunk = ChunkManager.findNearbyUnclaimedChunk(player);

				if (nearbyChunk != null) {
					PlayerUtils.teleportPlayerToChunk(player, nearbyChunk);
				}

				return;
			}

			if (isFeatureEnabled) {
				Placeholder placeholder = new Placeholder()
						.add("{region-displayname}", region.getDisplayName())
						.add("{region-owner}", region.getOwner().getName())
						.add("{region-description}", region.getDescription().replace("%player%", player.getName()));

				PlayerUtils.sendMessageRegionEnter(player, placeholder);
			}

			sessions.put(player.getUniqueId(), region.getUniqueId());

			// Weather and Time
			if (region.getWeather() != tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType.SERVER) {
				switch (region.getWeather()) {
					case tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType.CLEAR:
						player.setPlayerWeather(WeatherType.CLEAR);
						break;
					case tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType.RAIN:
						player.setPlayerWeather(WeatherType.DOWNFALL);
					default:
						break;
				}
			}

			if (region.getTime() != TimeType.SERVER) {
				player.setPlayerTime(region.getTime(), false);
			}

			// Glowing
			if (region.isWorldFlagSet(WorldFlags.PLAYER_GLOWING)) {
				if (!player.hasPotionEffect(PotionEffectType.GLOWING)) {
					player.addPotionEffect(
							new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 1, false, false));
				}
			}

			// Checking if player has an elytra
			if (player.isGliding() && isWearingElytra(player) && !PlayerUtils.isOperator(player)) {
				if (!region.isOwner(player)
						&& !PlayerUtils.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.ELYTRA, true)) {
					player.setGliding(false);
				}
			}
		} else {
			// Player leaves a region

			if (!sessions.containsKey(player.getUniqueId())) {
				return;
			}

			Region region = RegionManager.findRegion(sessions.get(player.getUniqueId()));

			if (isFeatureEnabled) {
				Placeholder placeholder = new Placeholder();

				if (region != null) {
					placeholder.add("{region-displayname}", region.getDisplayName());
					placeholder.add("{region-owner}", region.getOwner().getName());
					placeholder.add("{region-description}", region.getDescription());
				}

				PlayerUtils.sendMessageRegionExit(player, placeholder);
			}

			sessions.remove(player.getUniqueId());

			if (player.getPlayerWeather() != null) {
				player.resetPlayerWeather();
			}

			if (player.getPlayerTimeOffset() != 0) {
				player.resetPlayerTime();
			}

			if (player.hasPotionEffect(PotionEffectType.GLOWING)) {
				player.removePotionEffect(PotionEffectType.GLOWING);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		sessions.remove(player.getUniqueId());
	}

	private boolean isWearingElytra(Player player) {
		return player.getInventory().getChestplate() != null &&
				player.getInventory().getChestplate().getType() == Material.ELYTRA;
	}
}
