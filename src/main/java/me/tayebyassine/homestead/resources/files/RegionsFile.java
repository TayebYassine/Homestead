package me.tayebyassine.homestead.resources.files;

import org.bukkit.Color;
import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class RegionsFile extends ResourceFile {

	public RegionsFile(File file) throws FileNotFoundException {
		super(file);
	}

	public boolean isInstantTrustSystemEnabled() {
		return getBoolean("special-feat.ignore-trust-acceptance-system");
	}

	public boolean teleportPlayersBackToTegionSpawnWhenEnteringEndExitPortal() {
		return getBoolean("special-feat.teleport-players-back-to-region-spawn-when-entering-end-exit-portal");
	}

	public boolean isWelcomeSignEnabled() {
		return getBoolean("welcome-signs.enabled");
	}

	public boolean isAdjacentChunksRuleEnabled() {
		return getBoolean("adjacent-chunks");
	}

	public boolean isBordersEnabled() {
		return getBoolean("borders.enabled");
	}

	public Color getDustColor(DustColorType type) {
		List<Integer> rgb = getIntegerList("borders.dust-colors." + type.getName());

		if (rgb.size() == 3) {
			return Color.fromRGB(rgb.getFirst(), rgb.get(1), rgb.get(2));
		}

		return Color.fromRGB(255, 255, 255);
	}

	public float getDustSize() {
		return getFloat("borders.dust-size", 3.0F);
	}

	public boolean isRewardsEnabled() {
		return getBoolean("rewards.enabled");
	}

	public int getRewardChunksPerMember() {
		return getInt("rewards.for-each-member.chunks", 0);
	}

	public int getRewardSubAreasPerMember() {
		return getInt("rewards.for-each-member.subareas", 0);
	}

	public boolean isRegionStorageEnabled() {
		return getBoolean("storage.enabled");
	}

	public int getRegionStorageSize() {
		int size = getInt("storage.size");

		if (!List.of(9, 18, 27, 36, 45, 54).contains(size)) {
			size = 27;
		}

		return size;
	}

	public boolean isRentingEnabled() {
		return getBoolean("renting.enabled");
	}

	public double getDefaultRentPrice() { return getDouble("renting.price.default", 1500.0); }

	public double getMinRentPrice() {
		return getDouble("renting.price.min", 500.0);
	}

	public double getMaxRentPrice() {
		return getDouble("renting.price.max", 10_000_000.0);
	}

	public int getDefaultRentDays() {
		return getInt("renting.duration.default", 7);
	}

	public int getMinRentDays() {
		return getInt("renting.duration.min", 1);
	}

	public int getMaxRentDays() {
		return getInt("renting.duration.max", 84);
	}

	public double getDefaultSecurityDeposit() {
		return getDouble("renting.security-deposit.default", 500.0);
	}

	public double getMinSecurityDeposit() {
		return getDouble("renting.security-deposit.min", 0.0);
	}

	public double getMaxSecurityDeposit() {
		return getDouble("renting.security-deposit.max", 100_000.0);
	}

	public int getNoticeToVacateDays() {
		return getInt("renting.notice-to-vacate", 3);
	}

	public enum DustColorType {
		OWNER("owner"),
		MEMBER("member"),
		VISITOR("visitor"),
		SUB_AREA("sub-area");

		public final String name;

		DustColorType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}