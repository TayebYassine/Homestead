package tfagaming.projects.minecraft.homestead.models;

import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

public final class Level {
	public static final double BASE = 5;
	public static final double COEFFICIENT = 50;
	public static final double CONSTANT = 100;
	private final long id;
	private final long createdAt;
	private boolean autoUpdate = true;
	private long regionId;
	private int level;
	private long experience;
	private long totalExperience;

	public Level(long regionId) {
		this.id = Homestead.SNOWFLAKE.nextId();
		this.regionId = regionId;
		this.level = 0;
		this.experience = 0;
		this.totalExperience = 0;
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
		Region region = Homestead.regionsCache.get(this.regionId);

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

		Homestead.levelsCache.putOrUpdate(this);
	}
}