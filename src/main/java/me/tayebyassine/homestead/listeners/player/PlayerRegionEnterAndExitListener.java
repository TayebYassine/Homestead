package me.tayebyassine.homestead.listeners.player;

import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.managers.BanManager;
import me.tayebyassine.homestead.managers.ChunkManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.WarManager;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.RegionBan;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.ClaimFlySession;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.platform.PlatformBridge;
import me.tayebyassine.homestead.util.minecraft.players.PlayerUtility;
import me.tayebyassine.homestead.weatherandtime.RegionTime;
import me.tayebyassine.homestead.weatherandtime.RegionWeather;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks when players enter and leave claimed regions.
 *
 * <p>On entry/exit it enforces bans, passthrough restrictions and claim-fly deactivation, displays
 * the region info message, and applies per-region weather, time, glowing and elytra settings.</p>
 */
public final class PlayerRegionEnterAndExitListener implements Listener {
    private static final Map<UUID, Long> REGION_ENTRY_MAP = new ConcurrentHashMap<>();

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

            if (REGION_ENTRY_MAP.containsKey(player.getUniqueId()) && REGION_ENTRY_MAP.get(player.getUniqueId()) == region.getUniqueId()) {
                return;
            } else {
                if (!PlayerUtility.isOperator(player) && ClaimFlySession.hasSession(player)) {
                    ClaimFlySession.removeSession(player);

                    player.setAllowFlight(false);
                    player.setFlying(false);

                    Messages.send(player, "commands.fly.2");
                }
            }

            RegionBan ban = BanManager.getBannedPlayer(region, player);

            if (!PlayerUtility.isOperator(player) && ban != null) {
                Chunk nearbyChunk = ChunkManager.findNearbyUnclaimedChunk(player);

                if (nearbyChunk != null) {
                    PlayerUtility.teleportPlayerToChunk(player, nearbyChunk);
                }

                Messages.send(player, "common.region_ban", region.getName(), ban.getReason());

                return;
            }

            if (!PlayerUtility.isOperator(player) && !region.isOwner(player)
                    && !PlayerUtility.hasPermissionFlag(region, player, PlayerFlag.PASSTHROUGH, true) && !WarManager.isRegionInWar(region)) {
                Chunk nearbyChunk = ChunkManager.findNearbyUnclaimedChunk(player);

                if (nearbyChunk != null) {
                    PlayerUtility.teleportPlayerToChunk(player, nearbyChunk);
                }

                return;
            }

            String regionDescription = region.getDescription() == null ? "?" : region.getDescription().replace("%player%", player.getName()).replace("%owner%", region.getOwnerName());

            if (isRegionInfoMessagesEnabled) {
                Placeholder placeholder = new Placeholder()
                        .add("{region-displayname}", region.getDisplayName())
                        .add("{region-owner}", region.getOwnerName())
                        .add("{region-description}", regionDescription);

                sendMessageRegionEnter(player, placeholder);
            }

            REGION_ENTRY_MAP.put(player.getUniqueId(), region.getUniqueId());

            // Weather and Time
            if (region.getWeather() != RegionWeather.SERVER) {
                switch (region.getWeather()) {
                    case RegionWeather.CLEAR:
                        player.setPlayerWeather(WeatherType.CLEAR);
                        break;
                    case RegionWeather.RAIN:
                        player.setPlayerWeather(WeatherType.DOWNFALL);
                        break;
                    default:
                        break;
                }
            }

            if (region.getTime() != RegionTime.SERVER) {
                player.setPlayerTime(region.getTime(), false);
            }

            // Glowing
            if (region.isWorldFlagSet(WorldFlag.PLAYER_GLOWING.getBitmask())) {
                if (!player.hasPotionEffect(PotionEffectType.GLOWING)) {
                    player.addPotionEffect(
                            new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 1, false, false));
                }
            }

            // Checking if player has an elytra
            if (player.isGliding() && isWearingElytra(player) && !PlayerUtility.isOperator(player)) {
                if (!region.isOwner(player)
                        && !PlayerUtility.hasPermissionFlag(region, player, PlayerFlag.ELYTRA, true)) {
                    player.setGliding(false);
                }
            }
        } else {
            // Player leaves a region

            if (!REGION_ENTRY_MAP.containsKey(player.getUniqueId())) {
                return;
            }

            Region region = RegionManager.findRegion(REGION_ENTRY_MAP.get(player.getUniqueId()));

            if (isRegionInfoMessagesEnabled) {
                Placeholder placeholder = new Placeholder();

                if (region != null) {
                    String regionDescription = region.getDescription() == null ? "?" : region.getDescription().replace("%player%", player.getName()).replace("%owner%", region.getOwnerName());

                    placeholder.add("{region-displayname}", region.getDisplayName());
                    placeholder.add("{region-owner}", region.getOwnerName());
                    placeholder.add("{region-description}", regionDescription);
                }

                sendMessageRegionExit(player, placeholder);
            }

            REGION_ENTRY_MAP.remove(player.getUniqueId());

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

                Messages.send(player, "commands.fly.2");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        REGION_ENTRY_MAP.remove(player.getUniqueId());
    }

    private boolean isWearingElytra(Player player) {
        return player.getInventory().getChestplate() != null &&
                player.getInventory().getChestplate().getType() == Material.ELYTRA;
    }

    private void sendMessageRegionEnter(Player player, Placeholder placeholder) {
        String type = Resources.<RegionsFile>get(ResourceType.Regions).getString("enter-exit-region-message.type").toLowerCase();

        switch (type) {
            case "title": {
                List<String> titleData = Resources.<RegionsFile>get(ResourceType.Regions).getStringList(
                        "enter-exit-region-message.messages.enter.title");

                if (titleData.size() == 2) {
                    String t1 = Formatter.applyPlaceholders(titleData.getFirst(), placeholder);
                    String t2 = Formatter.applyPlaceholders(titleData.get(1), placeholder);

                    PlatformBridge.get().showTitle(player, t1, t2, 10, 70, 20);
                }
                break;
            }
            case "actionbar": {
                String text = Formatter.applyPlaceholders(
                        Resources.<RegionsFile>get(ResourceType.Regions).getString(
                                "enter-exit-region-message.messages.enter.actionbar"),
                        placeholder);

                PlatformBridge.get().sendActionBar(player, text);
                break;
            }
            default: {
                String text = Formatter.applyPlaceholders(
                        Resources.<RegionsFile>get(ResourceType.Regions).getString(
                                "enter-exit-region-message.messages.enter.chat"),
                        placeholder);

                PlatformBridge.get().sendMessage(player, text);
                break;
            }
        }
    }

    private void sendMessageRegionExit(Player player, Placeholder placeholder) {
        String type = Resources.<RegionsFile>get(ResourceType.Regions).getString("enter-exit-region-message.type").toLowerCase();

        switch (type) {
            case "title": {
                List<String> titleData = Resources.<RegionsFile>get(ResourceType.Regions).getStringList(
                        "enter-exit-region-message.messages.exit.title");

                if (titleData.size() == 2) {
                    String t1 = Formatter.applyPlaceholders(titleData.getFirst(), placeholder);
                    String t2 = Formatter.applyPlaceholders(titleData.get(1), placeholder);

                    PlatformBridge.get().showTitle(player, t1, t2, 10, 70, 20);
                }
                break;
            }
            case "actionbar": {
                String text = Formatter.applyPlaceholders(
                        Resources.<RegionsFile>get(ResourceType.Regions).getString(
                                "enter-exit-region-message.messages.exit.actionbar"),
                        placeholder);

                PlatformBridge.get().sendActionBar(player, text);
                break;
            }
            default: {
                String text = Formatter.applyPlaceholders(
                        Resources.<RegionsFile>get(ResourceType.Regions).getString(
                                "enter-exit-region-message.messages.exit.chat"),
                        placeholder);

                PlatformBridge.get().sendMessage(player, text);
                break;
            }
        }
    }
}

