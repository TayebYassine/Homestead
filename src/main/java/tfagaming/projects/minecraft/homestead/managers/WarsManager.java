package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.OfflinePlayer;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableMember;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarsManager {
	public static War declareWar(String name, double prize, List<Region> regions) {
		War war = new War(name);

		for (Region region : regions) {
			war.addRegion(region);
		}

		return war;
	}

	public static List<War> getAll() {
		return Homestead.warsCache.getAll();
	}

	public static War findWar(UUID id) {
		for (War war : Homestead.warsCache.getAll()) {
			if (war.getUniqueId().equals(id)) {
				return war;
			}
		}

		return null;
	}

	public static War findWar(String name) {
		for (War war : Homestead.warsCache.getAll()) {
			if (war.getName().equals(name)) {
				return war;
			}
		}

		return null;
	}

	public static War findWarByRegionId(UUID id) {
		for (War war : Homestead.warsCache.getAll()) {
			for (Region region : war.getRegions()) {
				if (region.getUniqueId().equals(id)) {
					return war;
				}
			}
		}

		return null;
	}

	public static void endWar(UUID id) {
		War war = findWar(id);

		if (war == null) {
			return;
		}

		Homestead.warsCache.remove(war.getUniqueId());
	}

	public static List<OfflinePlayer> getMembersOfWar(UUID id) {
		ArrayList<OfflinePlayer> players = new ArrayList<>();

		War war = findWar(id);

		if (war == null) return players;
		if (war.getRegions().isEmpty()) return players;

		Region first = war.getRegions().getFirst();
		Region second = war.getRegions().getLast();

		// Trigger's region
		for (SerializableMember member : first.getMembers()) {
			OfflinePlayer p = member.getBukkitOfflinePlayer();

			if (!players.contains(p)) {
				players.add(p);
			}
		}

		// Target region
		for (SerializableMember member : second.getMembers()) {
			OfflinePlayer p = member.getBukkitOfflinePlayer();

			if (!players.contains(p)) {
				players.add(p);
			}
		}

		players.add(first.getOwner());
		players.add(second.getOwner());

		return players;
	}

	public static War surrenderRegionFromFirstWarFound(UUID id) {
		for (War war : Homestead.warsCache.getAll()) {
			for (Region region : war.getRegions()) {
				if (region.getUniqueId().equals(id)) {
					war.removeRegion(region);

					return war;
				}
			}
		}

		return null;
	}

	public static boolean isNameUsed(String name) {
		for (War war : Homestead.warsCache.getAll()) {
			if (war.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isRegionInWar(UUID id) {
		for (War war : Homestead.warsCache.getAll()) {
			for (Region region : war.getRegions()) {
				if (region.getUniqueId().equals(id)) {
					return true;
				}
			}
		}

		return false;
	}
}
