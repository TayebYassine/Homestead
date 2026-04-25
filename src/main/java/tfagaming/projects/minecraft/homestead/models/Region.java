package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.flags.FlagsCalculator;
import tfagaming.projects.minecraft.homestead.flags.PlayerFlags;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.MapColor;
import tfagaming.projects.minecraft.homestead.weatherandtime.TimeType;
import tfagaming.projects.minecraft.homestead.weatherandtime.WeatherType;

import java.util.UUID;

public final class Region {
	private static final Homestead INSTANCE = Homestead.getInstance();
	private final long id;
	private final long createdAt;
	private boolean autoUpdate = true;
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
	private int weather = WeatherType.SERVER;
	private int time = TimeType.SERVER;
	private SeLocation welcomeSign;
	private long upkeepAt = 0L;

	public Region(String name, OfflinePlayer player) {
		this(name, player.getUniqueId());
	}

	public Region(String name, UUID ownerId) {
		this.id = Homestead.SNOWFLAKE.nextId();
		this.name = name;
		this.ownerId = ownerId;
		this.createdAt = System.currentTimeMillis();
	}

	public Region(long id, String name, UUID ownerId, long createdAt) {
		this.id = id;
		this.name = name;
		this.ownerId = ownerId;
		this.createdAt = createdAt;
	}

	/**
	 * Toggle Auto-Update for caching. If {@code true}, any call for setters will automatically
	 * update the cache. Otherwise, only the instance of the class will be updated.<br>
	 * @param autoUpdate Auto-Update toggle
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public long getUniqueId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		update();
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		update();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		update();
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
		update();
	}

	public OfflinePlayer getOwner() {
		if (INSTANCE == null) return null;

		return INSTANCE.getOfflinePlayerSync(ownerId);
	}

	public boolean isOwner(OfflinePlayer player) {
		return isOwner(player.getUniqueId());
	}

	public boolean isOwner(UUID id) {
		return this.ownerId.equals(id);
	}

	public void setOwner(OfflinePlayer owner) {
		this.ownerId = owner.getUniqueId();
		update();
	}

	public SeLocation getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = new SeLocation(location);
		update();
	}

	public void setLocation(SeLocation location) {
		this.location = location;
		update();
	}

	public long getPlayerFlags() {
		return playerFlags;
	}

	public void setPlayerFlags(long playerFlags) {
		this.playerFlags = playerFlags;
		update();
	}

	public boolean isPlayerFlagSet(long flag) {
		return FlagsCalculator.isFlagSet(playerFlags, flag);
	}

	public long getWorldFlags() {
		return worldFlags;
	}

	public void setWorldFlags(long worldFlags) {
		this.worldFlags = worldFlags;
		update();
	}

	public boolean isWorldFlagSet(long flag) {
		return FlagsCalculator.isFlagSet(worldFlags, flag);
	}

	public double getTaxes() {
		return taxes;
	}

	public void setTaxes(double taxes) {
		this.taxes = taxes;
		update();
	}

	public double getBank() {
		return bank;
	}

	public void setBank(double bank) {
		this.bank = bank;
		update();
	}

	public void depositBank(double amount) {
		if (amount <= 0) return;

		setBank(getBank() + amount);
	}

	public void withdrawBank(double amount) {
		setBank(getBank() - amount);

		if (getBank() < 0) setBank(0);
	}

	public int getMapColor() {
		return mapColor;
	}

	public void setMapColor(int mapColor) {
		this.mapColor = mapColor;
		update();
	}

	public String getMapIcon() {
		return mapIcon;
	}

	public void setMapIcon(String mapIcon) {
		this.mapIcon = mapIcon;
		update();
	}

	public SeRent getRent() {
		return rent;
	}

	public void setRent(SeRent rent) {
		this.rent = rent;
		update();
	}

	public int getWeather() {
		return weather;
	}

	public void setWeather(int weather) {
		this.weather = weather;
		update();
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
		update();
	}

	public SeLocation getWelcomeSign() {
		return welcomeSign;
	}

	public void setWelcomeSign(SeLocation welcomeSign) {
		this.welcomeSign = welcomeSign;
		update();
	}

	public long getUpkeepAt() {
		return upkeepAt;
	}

	public void setUpkeepAt(long upkeepAt) {
		this.upkeepAt = upkeepAt;
		update();
	}

	public long getCreatedAt() {
		return createdAt;
	}

	/**
	 * Check if the region is public.
	 * @return {@code true} if the flags {@code PASSTHROUGH} ({@value tfagaming.projects.minecraft.homestead.flags.PlayerFlags#PASSTHROUGH}) and
	 * {@code TELEPORT_SPAWN} ({@value tfagaming.projects.minecraft.homestead.flags.PlayerFlags#TELEPORT_SPAWN}) are allowed to global players, {@code false} otherwise.
	 */
	public boolean isPublic() {
		return FlagsCalculator.isFlagSet(playerFlags, PlayerFlags.PASSTHROUGH) && FlagsCalculator.isFlagSet(playerFlags, PlayerFlags.TELEPORT_SPAWN);
	}

	private void update() {
		if (!autoUpdate) return;

		Homestead.regionsCache.putOrUpdate(this);
	}
}