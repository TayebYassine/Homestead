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
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.flags.WorldFlags;
import tfagaming.projects.minecraft.homestead.managers.BanManager;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.managers.WarManager;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.sessions.ClaimFlySession;

import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtility;
import tfagaming.projects.minecraft.homestead.weatherandtime.TimeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerRegionEnterAndExitListener implements Listener {
	private static final Map<UUID, Long> SESSIONS = new HashMap();

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Chunk chunk = player.getLocation().getChunk();

		if (!(event.getFrom().getBlockX() != event.getTo().getBlockX()
				|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()
				|| event.getFrom().getBlockY() != event.getTo().getBlockY())) {
			return;
		}

		boolean isRegionInfoMessagesEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("enter-exit-region-message.enabled");

		if (ChunkManager.isChunkClaimed(chunk)) {
			// Player enters a region

			Region region = ChunkManager.getRegionOwnsTheChunk(chunk);
			assert region != null;

			if (SESSIONS.containsKey(player.getUniqueId()) && SESSIONS.get(player.getUniqueId()) == region.getUniqueId()) {
				return;
			} else {
				if (!PlayerUtility.isOperator(player) && ClaimFlySession.hasSession(player)) {
					ClaimFlySession.removeSession(player);

					player.setAllowFlight(false);
					player.setFlying(false);

					Messages.send(player, 206);
				}
			}

			if (!PlayerUtility.isOperator(player) && BanManager.isBanned(region, player)) {
				Chunk nearbyChunk = ChunkManager.findNearbyUnclaimedChunk(player);

				if (nearbyChunk != null) {
					PlayerUtility.teleportPlayerToChunk(player, nearbyChunk);
				}

				Messages.send(player, 28, new Placeholder()
						.add("{region}", region.getName())
						.add("{ban-reason}", BanManager.getBannedPlayer(region, player).getReason())
				);

				return;
			}

			if (!PlayerUtility.isOperator(player) && !region.isOwner(player)
					&& !PlayerUtility.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.PASSTHROUGH, true) && !WarManager.isRegionInWar(region.getUniqueId())) {
				Chunk nearbyChunk = ChunkManager.findNearbyUnclaimedChunk(player);

				if (nearbyChunk != null) {
					PlayerUtility.teleportPlayerToChunk(player, nearbyChunk);
				}

				return;
			}

			if (isRegionInfoMessagesEnabled) {
				Placeholder placeholder = new Placeholder()
						.add("{region-displayname}", region.getDisplayName())
						.add("{region-owner}", region.getOwner().getName())
						.add("{region-description}", region.getDescription().replace("%player%", player.getName()));

				PlayerUtility.sendMessageRegionEnter(player, placeholder);
			}

			SESSIONS.put(player.getUniqueId(), region.getUniqueId());

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
			if (player.isGliding() && isWearingElytra(player) && !PlayerUtility.isOperator(player)) {
				if (!region.isOwner(player)
						&& !PlayerUtility.hasPermissionFlag(region.getUniqueId(), player, PlayerFlags.ELYTRA, true)) {
					player.setGliding(false);
				}
			}
		} else {
			// Player leaves a region

			if (!SESSIONS.containsKey(player.getUniqueId())) {
				return;
			}

			Region region = RegionManager.findRegion(SESSIONS.get(player.getUniqueId()));

			if (isRegionInfoMessagesEnabled) {
				Placeholder placeholder = new Placeholder();

				if (region != null) {
					placeholder.add("{region-displayname}", region.getDisplayName());
					placeholder.add("{region-owner}", region.getOwner().getName());
					placeholder.add("{region-description}", region.getDescription());
				}

				PlayerUtility.sendMessageRegionExit(player, placeholder);
			}

			SESSIONS.remove(player.getUniqueId());

			if (player.getPlayerWeather() != null) {
				player.resetPlayerWeather();
			}

			if (player.getPlayerTimeOffset() != 0) {
				player.resetPlayerTime();
			}

			if (player.hasPotionEffect(PotionEffectType.GLOWING)) {
				player.removePotionEffect(PotionEffectType.GLOWING);
			}

			if (!PlayerUtility.isOperator(player) && ClaimFlySession.hasSession(player)) {
				ClaimFlySession.removeSession(player);

				player.setAllowFlight(false);
				player.setFlying(false);

				Messages.send(player, 206);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		SESSIONS.remove(player.getUniqueId());
	}

	private boolean isWearingElytra(Player player) {
		return player.getInventory().getChestplate() != null &&
				player.getInventory().getChestplate().getType() == Material.ELYTRA;
	}
}
