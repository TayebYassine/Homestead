package me.tayebyassine.homestead.util.minecraft.players;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.flags.ControlFlag;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.managers.MemberManager;
import me.tayebyassine.homestead.managers.RegionManager;
import me.tayebyassine.homestead.managers.SubAreaManager;
import me.tayebyassine.homestead.managers.WarManager;
import me.tayebyassine.homestead.models.*;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import me.tayebyassine.homestead.util.minecraft.chunks.ChunkUtility;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.limits.Limits.LimitMethod;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility methods for player-related operations: teleportation, operator checks and, most
 * importantly, the resolution of {@link PlayerFlag}/{@link ControlFlag} permissions inside regions
 * and sub-areas.
 */
public final class PlayerUtility {
    public static final Set<PlayerFlag> RENT_FLAGS_SET = Set.of(
            PlayerFlag.PVP
    );
    public static final Set<PlayerFlag> WAR_FLAGS_SET = Set.of(
            PlayerFlag.PVP,
            PlayerFlag.DOORS,
            PlayerFlag.TRAP_DOORS,
            PlayerFlag.FENCE_GATES,
            PlayerFlag.PASSTHROUGH,
            PlayerFlag.ELYTRA,
            PlayerFlag.TELEPORT,
            PlayerFlag.PICKUP_ITEMS,
            PlayerFlag.TAKE_FALL_DAMAGE,
            PlayerFlag.CONTAINERS,
            PlayerFlag.BREAK_BLOCKS,
            PlayerFlag.PLACE_BLOCKS
    );
    private static final int MESSAGE_COOLDOWN_SECONDS = 3;
    private static final Set<UUID> COOLDOWN = ConcurrentHashMap.newKeySet();

    private PlayerUtility() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Teleport a player to a chunk safely, thread-safe for Folia.
     */
    public static void teleportPlayerToChunkSafely(Player player, RegionChunk chunk, @Nullable Runnable after) {
        if (chunk == null) return;

        Location chunkLoc = chunk.toBukkitDisplayLocation();
        if (chunkLoc == null) return;

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        Homestead.getInstance().runLocationTask(chunkLoc, () -> {
            Location loc = ChunkUtility.getLocationWithoutYawPitch(chunk);
            if (loc == null) return;

            loc.setYaw(yaw);
            loc.setPitch(pitch);

            Homestead.getInstance().runPlayerTask(player, () -> {
                if (after != null) after.run();

                new DelayedTeleport(player, loc);
            });
        });
    }

    /**
     * Teleports the player to the center of the given chunk.
     *
     * @param player the player to teleport
     * @param chunk  the destination chunk
     */
    public static void teleportPlayerToChunk(Player player, Chunk chunk) {
        Location location = ChunkUtility.getLocation(player, chunk);

        teleportPlayer(player, location);
    }

    /**
     * Teleports the player to the given location, using the async API on Folia and the
     * synchronous API on Spigot/PaperMC.
     *
     * @param player   the player to teleport
     * @param location the destination location (no-op if {@code null})
     */
    public static void teleportPlayer(Player player, Location location) {
        if (location == null) return;

        if (Homestead.isFolia()) {
            player.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    /**
     * Returns whether the player is considered a Homestead operator (OP or holding the
     * {@code homestead.operator} permission).
     *
     * @param player the player to check
     * @return {@code true} if the player is an operator
     */
    public static boolean isOperator(Player player) {
        if (player.isOp()) return true;
        return player.hasPermission("homestead.operator");
    }

    /** @return {@code true} if the offline player is an OP. */
    public static boolean isOperator(OfflinePlayer player) {
        return player.isOp();
    }

    /**
     * Resolves a player flag for a region by delegating to
     * {@link #hasPermissionFlag(long, Player, PlayerFlag, boolean)}.
     */
    public static boolean hasPermissionFlag(Region region, Player player, PlayerFlag flag, boolean notify) {
        return hasPermissionFlag(region.getUniqueId(), player, flag, notify);
    }

    /**
     * Checks whether the given player has the specified player flag in the target region.
     * If the player lacks the permission, a cooldown-gated info message is sent,
     * except for the {@code TAKE_FALL_DAMAGE} flag where no message is shown.
     * <p>
     * Resolution order:<br>
     * 1. If the player is the active renter of the region, permissions are granted for all flags except {@code PVP}.<br>
     * 2. Otherwise, if the player is a member, the member flags are used.<br>
     * 3. Otherwise, the region's global player flags are used.<br>
     *
     * @param regionId The region ID
     * @param player   The player to fetch
     * @param flag     The PlayerFlag to check
     * @return {@code true} If the action is allowed; {@code false} otherwise
     */
    public static boolean hasPermissionFlag(long regionId, Player player, PlayerFlag flag, boolean notify) {
        Region region = RegionManager.findRegion(regionId);
        if (region == null) return true;

        boolean response;

        SeRent rent = region.getRent();
        War war = WarManager.findWarByRegion(regionId);

        if (rent.hasRenter()
                && rent.isRenterer(player)
                && !RENT_FLAGS_SET.contains(flag)) {
            response = true;
        } else if (WarManager.isPlayerInWar(player, war)
                && WAR_FLAGS_SET.contains(flag)) {
            response = true;
        } else if (MemberManager.isMemberOfRegion(regionId, player)) {
            RegionMember member = MemberManager.getMemberOfRegion(regionId, player);
            response = FlagCalculator.isFlagSet(member.getPlayerFlags(), flag);
        } else {
            response = FlagCalculator.isFlagSet(region.getPlayerFlags(), flag);
        }

        if (!response
                && flag != PlayerFlag.TAKE_FALL_DAMAGE
                && !COOLDOWN.contains(player.getUniqueId())
                && notify) {
            sendDenialMessage(player, region, flag);
        }

        return response;
    }

    /**
     * Resolves a player flag inside a sub-area, falling back to the parent region when the
     * sub-area does not exist. See {@link #hasPermissionFlag(long, long, Player, PlayerFlag, boolean)}.
     */
    public static boolean hasPermissionFlag(Region region, SubArea subArea,
                                            Player player, PlayerFlag flag, boolean notify) {
        return hasPermissionFlag(region.getUniqueId(), subArea.getUniqueId(), player, flag, notify);
    }

    public static boolean hasPermissionFlag(long regionId, long subAreaId,
                                            Player player, PlayerFlag flag, boolean notify) {
        Region region = RegionManager.findRegion(regionId);
        if (region == null) return true;

        SubArea subArea = SubAreaManager.findSubArea(subAreaId);

        if (subArea != null) {
            SeRent subRent = subArea.getRent();
            if (subRent.hasRenter()
                    && subRent.isRenterer(player)
                    && !RENT_FLAGS_SET.contains(flag)) {
                return true;
            }

            if (MemberManager.isMemberOfSubArea(subAreaId, player)) {
                RegionMember member = MemberManager.getMemberOfSubArea(subAreaId, player);
                return FlagCalculator.isFlagSet(member.getPlayerFlags(), flag);
            }

            return FlagCalculator.isFlagSet(subArea.getPlayerFlags(), flag);
        }

        return hasPermissionFlag(regionId, player, flag, notify);
    }

    private static void sendDenialMessage(Player player, Region region, PlayerFlag flag) {
        Messages.send(player, "common.no_flag_permission", new Placeholder()
                .add("{flag}", flag.getName())
                .add("{region}", region.getName())
        );

        if (List.of(PlayerFlag.TRIGGER_TRIPWIRE, PlayerFlag.PVP).contains(flag)) {
            COOLDOWN.add(player.getUniqueId());
            Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()),
                    MESSAGE_COOLDOWN_SECONDS);
        }
    }

    /**
     * Resolves a control flag for a region by delegating to
     * {@link #hasControlPermissionFlag(long, Player, ControlFlag, boolean)}.
     */
    public static boolean hasControlPermissionFlag(Region region, Player player, ControlFlag flag, boolean notify) {
        return hasControlPermissionFlag(region.getUniqueId(), player, flag, notify);
    }

    /**
     * Checks whether the player has the given {@link ControlFlag} in the region.
     *
     * <p>Operators and the region owner always pass. Members are evaluated against their control-flag
     * set; non-members are denied. A cooldown-gated denial message is shown when the check fails.</p>
     *
     * @param regionId the region ID
     * @param player   the player to check
     * @param flag     the control flag required
     * @param notify   whether to display a denial message to the player
     * @return {@code true} if the player may perform the controlled action
     */
    public static boolean hasControlPermissionFlag(long regionId, Player player, ControlFlag flag, boolean notify) {
        Region region = RegionManager.findRegion(regionId);

        if (region != null) {
            if (PlayerUtility.isOperator(player) || region.isOwner(player)) return true;

            boolean response = true;

            if (MemberManager.isMemberOfRegion(region, player)) {
                RegionMember member = MemberManager.getMemberOfRegion(region, player);
                response = FlagCalculator.isFlagSet(member.getControlFlags(), flag);
            }

            if (!response && !COOLDOWN.contains(player.getUniqueId())) {
                Messages.send(player, "common.no_flag_permission", new Placeholder()
                        .add("{flag}", flag.getName())
                        .add("{region}", region.getName())
                );

                COOLDOWN.add(player.getUniqueId());
                Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()), 3);
            }

            if (!response
                    && !COOLDOWN.contains(player.getUniqueId())
                    && notify) {
                sendDenialMessage(player, region, flag);
            }

            return response;
        }

        return true;
    }

    private static void sendDenialMessage(Player player, Region region, ControlFlag flag) {
        Messages.send(player, "common.no_flag_permission", new Placeholder()
                .add("{flag}", flag.getName())
                .add("{region}", region.getName())
        );

        COOLDOWN.add(player.getUniqueId());
        Homestead.getInstance().runAsyncTaskLater(() -> COOLDOWN.remove(player.getUniqueId()),
                MESSAGE_COOLDOWN_SECONDS);
    }

    /**
     * Returns the player's primary permission group, or {@code null} when the limits method is not
     * {@link LimitMethod#GROUPS} or no permissions provider is available.
     *
     * @param player the player to query
     * @return the primary group name, or {@code null}
     */
    public static String getPlayerGroup(OfflinePlayer player) {
        if (Limits.getLimitsMethod() != LimitMethod.GROUPS) return null;

        try {
            if (player.isOnline()) {
                return Homestead.VAULT.getPermissions().getPrimaryGroup(player);
            } else {
                return null;
            }
        } catch (Exception e) {
            Logger.error("Unable to find a service provider for permissions and groups, using the default group \"default\".");
            Logger.error("Please install a plugin that supports permissions and groups. We recommend installing the LuckPerms plugin.");
            Logger.error("To ignore this warning, change the limits method to \"static\" in this setting: limits.method");
        }

        return null;
    }

    /** @return {@code true} if both players share the same {@link UUID}. */
    public static boolean equals(OfflinePlayer p1, OfflinePlayer p2) {
        return p1.getUniqueId().equals(p2.getUniqueId());
    }
}

