package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public final class Level {
	public static final double BASE = 5;
	public static final double COEFFICIENT = 50;
	public static final double CONSTANT = 100;
	private final long id;
	private final long createdAt;
	private boolean autoUpdate = true;
	private long regionId = -1L;
	private int level = 0;
	private long experience = 0L;
	private long totalExperience = 0L;

	public Level(long regionId) {
		this.id = Homestead.getSnowflake().nextId();
		this.regionId = regionId;
		this.createdAt = System.currentTimeMillis();
	}

	public Level(long id, long regionId, int level, long experience, long totalExperience, long createdAt) {
		this.id = id;
		this.regionId = regionId;
		this.level = level;
		this.experience = experience;
		this.totalExperience = totalExperience;
		this.createdAt = createdAt;
	}

	public static long getXpForLevel(int level) {
		if (level < 0) return 0;
		double xp = BASE * Math.pow(level, 2) + (COEFFICIENT * level) + CONSTANT;
		return (long) Math.floor(xp);
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

	public long getRegionId() {
		return regionId;
	}

	/**
	 * Returns the region by directly fetching with region ID from cache.
	 * @return The region if found, {@code null} otherwise.
	 */
	public @Nullable Region getRegion() {
		return RegionManager.findRegion(regionId);
	}

	/**
	 * Returns the region name safely by directly fetching with region ID from cache.
	 * @return The region name if found, {@code "?"} otherwise.
	 */
	public String getRegionName() {
		Region region = getRegion();

		return region == null ? "?" : region.getName();
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
		update();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = Math.max(0, level);
		this.experience = 0;
		update();
	}

	public long getExperience() {
		return experience;
	}

	public long getTotalExperience() {
		return totalExperience;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getTotalXpForCurrentLevel() {
		long total = 0;
		for (int i = 0; i < this.level; i++) {
			total += getXpForLevel(i);
		}
		return total;
	}

	public long getXpForNextLevel() {
		return getXpForLevel(this.level);
	}

	public long getXpProgress() {
		return this.experience;
	}

	public long getXpRemaining() {
		long needed = getXpForNextLevel();
		return Math.max(0, needed - this.experience);
	}

	public double getProgressPercentage() {
		long needed = getXpForNextLevel();
		if (needed == 0) return 100.0;
		return (double) this.experience / needed * 100.0;
	}

	public void addXp(long amount) {
		if (amount <= 0) return;

		this.experience += amount;
		this.totalExperience += amount;

		checkLevelUp();
		update();
	}

	private void checkLevelUp() {
		long needed = getXpForNextLevel();

		while (this.experience >= needed && needed > 0) {
			this.experience -= needed;
			this.level++;
			onLevelUp();
			needed = getXpForNextLevel();
		}
	}

	private void onLevelUp() {
		Region region = Homestead.REGION_CACHE.get(this.regionId);

		if (region == null) return;

		if (region.getOwner() != null && region.getOwner().isOnline()) {
			Player owner = (Player) region.getOwner();

			Messages.send(owner, 208, new Placeholder()
					.add("{level}", this.level)
			);
		}
	}

	public void setXp(long experience) {
		this.experience = Math.max(0, experience);
		checkLevelUp();
		update();
	}

	public void removeXp(long amount) {
		if (amount <= 0) return;

		this.experience = Math.max(0, this.experience - amount);
		update();
	}

	public void reset() {
		this.level = 0;
		this.experience = 0;
		this.totalExperience = 0;
		update();
	}

	private void update() {
		if (!autoUpdate) return;

		Homestead.LEVEL_CACHE.putOrUpdate(this);
	}
}