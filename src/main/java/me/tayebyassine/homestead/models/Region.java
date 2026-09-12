package me.tayebyassine.homestead.models;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.flags.FlagCalculator;
import me.tayebyassine.homestead.flags.PlayerFlag;
import me.tayebyassine.homestead.flags.WorldFlag;
import me.tayebyassine.homestead.models.serialize.SeLocation;
import me.tayebyassine.homestead.models.serialize.SeRent;
import me.tayebyassine.homestead.util.minecraft.chunks.ProtectionMode;
import me.tayebyassine.homestead.util.minecraft.plugins.MapColor;
import me.tayebyassine.homestead.weatherandtime.RegionTime;
import me.tayebyassine.homestead.weatherandtime.RegionWeather;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a claimable region in the world.
 *
 * <p>A region is the primary territorial unit. Each region has an owner,
 * optional metadata (display name, description), bitmask-based player and
 * world flags, financial state (taxes, bank), map rendering options, a
 * configurable weather/time override, and an optional rent system.
 * </p>
 *
 * <p>Every mutating method automatically pushes the change to
 * {@link Homestead#REGION_CACHE} via {@link #update()} so that callers
 * never have to remember to persist.</p>
 */
public final class Region {

    private static final Homestead INSTANCE = Homestead.getInstance();

    private final long id;
    private final long createdAt;
    private String name;
    private String displayName;
    private String description;
    private UUID ownerId;
    private SeLocation location;
    private long playerFlags = 0L;
    private long worldFlags = 0L;
    private double taxes = 0.0;
    private double bank = 0.0;
    private int mapColor = MapColor.DEFAULT;
    private String mapIcon;
    private SeRent rent;
    private int weather = RegionWeather.SERVER;
    private int time = RegionTime.SERVER;
    private SeLocation welcomeSign;
    private long upkeepAt = 0L;

    /**
     * Create a new region with a generated snowflake ID.
     *
     * @param name   the region name
     * @param player the owner
     */
    public Region(String name, OfflinePlayer player) {
        this(name, player.getUniqueId());
    }

    /**
     * Create a new region with a generated snowflake ID.
     *
     * @param name    the region name
     * @param ownerId the owner UUID
     */
    public Region(String name, UUID ownerId) {
        this.id = Homestead.getSnowflake().nextId();
        this.name = name;
        this.ownerId = ownerId;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Create a region from pre-existing data (deserialization).
     *
     * @param id        the snowflake ID
     * @param name      the region name
     * @param ownerId   the owner UUID
     * @param createdAt the creation timestamp
     */
    public Region(long id, String name, UUID ownerId, long createdAt) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    /**
     * Get the unique snowflake ID of this region.
     *
     * @return the region ID
     */
    public long getUniqueId() {
        return id;
    }

    /**
     * Get the creation timestamp.
     *
     * @return the epoch-millis timestamp when the region was created
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the region name.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Set the region name.
     *
     * @param name the new name
     */
    public void setName(@NotNull String name) {
        this.name = name;
        update();
    }

    /**
     * Get the optional display name.
     *
     * @return the display name, or {@code null} if not set
     */
    public @Nullable String getDisplayName() {
        return displayName;
    }

    /**
     * Set the optional display name.
     *
     * @param displayName the display name, or {@code null} to clear
     */
    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
        update();
    }

    /**
     * Get the optional description.
     *
     * @return the description, or {@code null} if not set
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * Set the optional description.
     *
     * @param description the description, or {@code null} to clear
     */
    public void setDescription(@Nullable String description) {
        this.description = description;
        update();
    }

    /**
     * Get the owner UUID.
     *
     * @return the owner UUID
     */
    public @NotNull UUID getOwnerId() {
        return ownerId;
    }

    /**
     * Set the owner UUID.
     *
     * @param ownerId the new owner UUID
     */
    public void setOwnerId(@NotNull UUID ownerId) {
        this.ownerId = ownerId;
        update();
    }

    /**
     * Get the owner as an offline player.
     *
     * @return the owner, or {@code null} if not found
     */
    public @Nullable OfflinePlayer getOwner() {
        if (INSTANCE == null) return null;

        return INSTANCE.getOfflinePlayerSync(ownerId);
    }

    /**
     * Set the owner.
     *
     * @param owner the new owner
     */
    public void setOwner(@NotNull OfflinePlayer owner) {
        this.ownerId = owner.getUniqueId();
        update();
    }

    /**
     * Get the owner's name safely. If the player was not found by their
     * ID, it will return {@code "?"} instead.
     *
     * @return the owner's name if found, {@code "?"} otherwise
     */
    public @NotNull String getOwnerName() {
        OfflinePlayer player = getOwner();

        return player == null || player.getName() == null ? "?" : player.getName();
    }

    /**
     * Check whether the given player is the owner.
     *
     * @param player the player to check
     * @return {@code true} if the player is the owner
     */
    public boolean isOwner(OfflinePlayer player) {
        return isOwner(player.getUniqueId());
    }

    /**
     * Check whether the given UUID is the owner.
     *
     * @param id the UUID to check
     * @return {@code true} if the UUID matches the owner
     */
    public boolean isOwner(UUID id) {
        return this.ownerId.equals(id);
    }

    /**
     * Get the region's set location (spawn/home point).
     *
     * @return the location, or {@code null} if not set
     */
    public @Nullable SeLocation getLocation() {
        return location;
    }

    /**
     * Set the region's location from a Bukkit location.
     *
     * @param location the new location, or {@code null} to clear
     */
    public void setLocation(@Nullable Location location) {
        this.location = location == null ? null : new SeLocation(location);
        update();
    }

    /**
     * Set the region's location from a serialisable location.
     *
     * @param location the new location, or {@code null} to clear
     */
    public void setLocation(@Nullable SeLocation location) {
        this.location = location;
        update();
    }

    /**
     * Reset the region's location to {@code null}.
     */
    public void resetLocation() {
        this.location = null;
        update();
    }

    /**
     * Get the player flags bitmask.
     *
     * @return the player flags
     */
    public long getPlayerFlags() {
        return playerFlags;
    }

    /**
     * Set the player flags bitmask.
     *
     * @param playerFlags the new player flags
     */
    public void setPlayerFlags(long playerFlags) {
        this.playerFlags = playerFlags;
        update();
    }

    /**
     * Check whether a specific player flag is set.
     *
     * @param flag the flag bitmask
     * @return {@code true} if the flag is set, or if protection mode is
     * enabled
     */
    public boolean isPlayerFlagSet(long flag) {
        if (ProtectionMode.isEnabled()) return false;

        return FlagCalculator.isFlagSet(playerFlags, flag);
    }

    /**
     * Check whether a specific player flag is set.
     *
     * @param flag the flag
     * @return {@code true} if the flag is set
     */
    public boolean isPlayerFlagSet(PlayerFlag flag) {
        return isPlayerFlagSet(flag.getBitmask());
    }

    /**
     * Get the world flags' bitmask.
     *
     * @return the world flags
     */
    public long getWorldFlags() {
        return worldFlags;
    }

    /**
     * Set the world flags' bitmask.
     *
     * @param worldFlags the new world flags
     */
    public void setWorldFlags(long worldFlags) {
        this.worldFlags = worldFlags;
        update();
    }

    /**
     * Check whether a specific world flag is set.
     *
     * @param flag the flag bitmask
     * @return {@code true} if the flag is set
     */
    public boolean isWorldFlagSet(long flag) {
        if (ProtectionMode.isEnabled()) return false;

        return FlagCalculator.isFlagSet(worldFlags, flag);
    }

    /**
     * Check whether a specific world flag is set.
     *
     * @param flag the flag
     * @return {@code true} if the flag is set
     */
    public boolean isWorldFlagSet(WorldFlag flag) {
        return isWorldFlagSet(flag.getBitmask());
    }

    /**
     * Get the accumulated taxes.
     *
     * @return the taxes
     */
    public double getTaxes() {
        return taxes;
    }

    /**
     * Set the accumulated taxes.
     *
     * @param taxes the new tax amount
     */
    public void setTaxes(double taxes) {
        this.taxes = taxes;
        update();
    }

    /**
     * Get the bank balance.
     *
     * @return the bank balance
     */
    public double getBank() {
        return bank;
    }

    /**
     * Set the bank balance.
     *
     * @param bank the new balance
     */
    public void setBank(double bank) {
        this.bank = bank;
        update();
    }

    /**
     * Deposit an amount into the bank. Ignored if the amount is
     * non-positive.
     *
     * @param amount the amount to deposit
     */
    public void depositBank(double amount) {
        if (amount <= 0) return;

        setBank(getBank() + amount);
    }

    /**
     * Withdraw an amount from the bank. Ignored if the amount is
     * non-positive or non-finite. The balance never goes below zero.
     *
     * @param amount the amount to withdraw
     */
    public void withdrawBank(double amount) {
        if (amount <= 0 || !Double.isFinite(amount)) return;

        setBank(Math.max(0, getBank() - amount));
    }

    /**
     * Get the map colour index.
     *
     * @return the colour index
     */
    public int getMapColor() {
        return mapColor;
    }

    /**
     * Set the map colour index.
     *
     * @param mapColor the new colour index
     */
    public void setMapColor(int mapColor) {
        this.mapColor = mapColor;
        update();
    }

    /**
     * Get the map icon code.
     *
     * @return the icon, or {@code null} if not set
     */
    public @Nullable String getMapIcon() {
        return mapIcon;
    }

    /**
     * Set the map icon code.
     *
     * @param mapIcon the icon, or {@code null} to clear
     */
    public void setMapIcon(@Nullable String mapIcon) {
        this.mapIcon = mapIcon;
        update();
    }

    /**
     * Get the rent configuration. Always returns a non-null instance.
     *
     * @return the rent
     */
    public @NotNull SeRent getRent() {
        if (rent == null) {
            rent = new SeRent();
        }
        return rent;
    }

    /**
     * Set the rent configuration.
     *
     * @param rent the new rent, or {@code null} to reset to defaults
     */
    public void setRent(@Nullable SeRent rent) {
        this.rent = rent != null ? rent : new SeRent();
        update();
    }

    /**
     * Get the weather override.
     *
     * @return the weather state
     */
    public int getWeather() {
        return weather;
    }

    /**
     * Set the weather override.
     *
     * @param weather the new weather state
     */
    public void setWeather(int weather) {
        this.weather = weather;
        update();
    }

    /**
     * Get the time override.
     *
     * @return the time state
     */
    public int getTime() {
        return time;
    }

    /**
     * Set the time override.
     *
     * @param time the new time state
     */
    public void setTime(int time) {
        this.time = time;
        update();
    }

    /**
     * Get the welcome sign location.
     *
     * @return the sign location, or {@code null} if not set
     */
    public @Nullable SeLocation getWelcomeSign() {
        return welcomeSign;
    }

    /**
     * Set the welcome sign location.
     *
     * @param welcomeSign the sign location, or {@code null} to clear
     */
    public void setWelcomeSign(@Nullable SeLocation welcomeSign) {
        this.welcomeSign = welcomeSign;
        update();
    }

    /**
     * Get the last upkeep timestamp.
     *
     * @return the epoch-millis timestamp
     */
    public long getUpkeepAt() {
        return upkeepAt;
    }

    /**
     * Set the last upkeep timestamp.
     *
     * @param upkeepAt the epoch-millis timestamp
     */
    public void setUpkeepAt(long upkeepAt) {
        this.upkeepAt = upkeepAt;
        update();
    }

    /**
     * Check if the region is public. A region is public when both the
     * {@link PlayerFlag#PASSTHROUGH} and {@link PlayerFlag#TELEPORT_SPAWN}
     * flags are enabled for global players.
     *
     * @return {@code true} if the region is public
     */
    public boolean isPublic() {
        return FlagCalculator.isFlagSet(playerFlags, PlayerFlag.PASSTHROUGH.getBitmask()) && FlagCalculator.isFlagSet(playerFlags, PlayerFlag.TELEPORT_SPAWN.getBitmask());
    }

    private void update() {
        Homestead.REGION_CACHE.putOrUpdate(this);
    }
}
