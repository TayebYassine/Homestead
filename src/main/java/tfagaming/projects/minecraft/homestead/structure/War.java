package tfagaming.projects.minecraft.homestead.structure;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class War {
	private boolean autoUpdate = true;

	public UUID id;
	public String name;
	public String description;
	public String displayName;
	public final ArrayList<UUID> regions = new ArrayList<>();
	public double prize;
	public long startedAt;

	public War(String name) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.displayName = name;
		this.description = Resources.<LanguageFile>get(ResourceType.Language).getString("default.war-description");
		this.prize = 0.0;
		this.startedAt = System.currentTimeMillis();
	}

	public War(String name, List<UUID> regions) {
		this(name);
		this.regions.addAll(regions);
	}

	/**
	 * Toggle Auto-Update for caching. If {@code true}, any call for setters will automatically
	 * update the cache. Otherwise, only the instance of the class will be updated.<br>
	 * @param autoUpdate Auto-Update toggle
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public UUID getUniqueId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		updateCache();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		updateCache();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		updateCache();
	}

	public double getPrize() {
		return prize;
	}

	public void setPrize(double prize) {
		this.prize = prize;
		updateCache();
	}

	public long getStartedAt() {
		return startedAt;
	}

	public ArrayList<UUID> getRegionUniqueIds() {
		return regions;
	}

	public ArrayList<Region> getRegions() {
		ArrayList<Region> resolved = new ArrayList<>();

		for (UUID uuid : this.regions) {
			Region region = RegionManager.findRegion(uuid);
			if (region != null) {
				resolved.add(region);
			}
		}

		return resolved;
	}

	public void addRegion(Region region) {
		if (this.regions.contains(region.id)) {
			return;
		}
		this.regions.add(region.id);
		updateCache();
	}

	public void removeRegion(Region region) {
		if (!this.regions.contains(region.id)) {
			return;
		}
		this.regions.remove(region.id);
		updateCache();
	}

	public Region getWinner() {
		ArrayList<Region> current = getRegions();
		return current.size() == 1 ? current.getFirst() : null;
	}

	public void updateCache() {
		if (!autoUpdate) return;
		Homestead.warsCache.putOrUpdate(this);
	}
}