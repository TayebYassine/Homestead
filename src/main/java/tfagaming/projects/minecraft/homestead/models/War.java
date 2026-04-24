package tfagaming.projects.minecraft.homestead.models;

import tfagaming.projects.minecraft.homestead.Homestead;

import java.util.ArrayList;
import java.util.List;

public final class War {
	private final long id;
	private final List<Long> regionIds;
	private boolean autoUpdate = true;
	private String name;
	private String displayName;
	private String description;
	private double prize;
	private long startedAt;

	public War(String name) {
		this.id = Homestead.SNOWFLAKE.nextId();
		this.name = name;
		this.displayName = name;
		this.description = "";
		this.regionIds = new ArrayList<>();
		this.prize = 0.0;
		this.startedAt = System.currentTimeMillis();
	}

	public War(String name, List<Long> regionIds) {
		this(name);
		this.regionIds.addAll(regionIds);
	}

	public War(long id, String name, String displayName, String description, List<Long> regionIds, double prize, long startedAt) {
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.regionIds = new ArrayList<>(regionIds);
		this.prize = prize;
		this.startedAt = startedAt;
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

	public List<Long> getRegionIds() {
		return new ArrayList<>(regionIds);
	}

	public void setRegionIds(List<Long> regionIds) {
		this.regionIds.clear();
		this.regionIds.addAll(regionIds);
		update();
	}

	public void addRegionId(long regionId) {
		if (!this.regionIds.contains(regionId)) {
			this.regionIds.add(regionId);
			update();
		}
	}

	public void removeRegionId(long regionId) {
		if (this.regionIds.remove(regionId)) {
			update();
		}
	}

	public double getPrize() {
		return prize;
	}

	public void setPrize(double prize) {
		this.prize = prize;
		update();
	}

	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
		update();
	}

	public Region getWinner() {
		if (regionIds.size() == 1) {
			return Homestead.regionsCache.get(regionIds.get(0));
		}
		return null;
	}

	private void update() {
		if (!autoUpdate) return;

		Homestead.warsCache.putOrUpdate(this);
	}
}