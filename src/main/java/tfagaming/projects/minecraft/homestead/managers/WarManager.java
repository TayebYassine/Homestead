package tfagaming.projects.minecraft.homestead.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.models.Region;
import tfagaming.projects.minecraft.homestead.models.War;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.LanguageFile;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;


import tfagaming.projects.minecraft.homestead.tools.java.Formatter;
import tfagaming.projects.minecraft.homestead.tools.java.Placeholder;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.Messages;

import java.util.*;

public final class WarManager {
	private WarManager() {
	}

	/**
	 * Declares a new war between exactly two regions.
	 *
	 * @param name    The war display name
	 * @param prize   The reward given to the winning region; must be &gt; 0
	 * @param regionA The first participating region
	 * @param regionB The second participating region
	 */
	public static War declareWar(String name, double prize, Region regionA, Region regionB) {
		if (regionA.getUniqueId() == regionB.getUniqueId()) {
			throw new IllegalArgumentException("A war must involve two distinct regions.");
		}

		if (isRegionInWar(regionA.getUniqueId()) || isRegionInWar(regionB.getUniqueId())) {
			throw new IllegalStateException("One of the regions is currently in a war.");
		}

		War war = new War(name);

		war.setAutoUpdate(false);

		war.addRegionId(regionA.getUniqueId());
		war.addRegionId(regionB.getUniqueId());
		war.setPrize(prize);

		war.setAutoUpdate(true);

		Homestead.warsCache.putOrUpdate(war);

		return war;
	}

	public static List<War> getAll() {
		return Homestead.warsCache.getAll();
	}

	/** Returns the war with the exact UUID, or {@code null} if none exists. */
	public static War findWar(long warId) {
		return Homestead.warsCache.get(warId);
	}

	/** Returns the war with the exact name (case-sensitive), or {@code null} if none exists. */
	public static War findWar(String name) {
		return getAll().stream()
				.filter(w -> w.getName().equals(name))
				.findFirst()
				.orElse(null);
	}

	/** Returns the war the given region is participating in, or {@code null}. */
	public static War findWarByRegion(long regionId) {
		return getAll().stream()
				.filter(w -> w.getRegions().stream()
						.anyMatch(r -> r.getUniqueId() == regionId))
				.findFirst()
				.orElse(null);
	}

	/** Ends and removes the war with the given UUID. */
	public static void endWar(long warId) {
		Homestead.warsCache.remove(warId);
	}

	/**
	 * Collects all members and owners from every region in the war.
	 *
	 * @param warId The war UUID
	 */
	public static List<OfflinePlayer> getMembersOfWar(long warId) {
		War war = findWar(warId);
		return getMembersOfWar(war);
	}

	/**
	 * Collects all members and owners from every region in the war.
	 *
	 * @param war The war
	 */
	public static List<OfflinePlayer> getMembersOfWar(War war) {
		if (war == null || war.getRegions().size() < 2) {
			return Collections.emptyList();
		}

		Set<OfflinePlayer> players = new HashSet<>();
		for (Region region : war.getRegions()) {
			MemberManager.getMembersOfRegion(region).forEach(m -> players.add(m.getPlayer()));
			players.add(region.getOwner());
		}

		return new ArrayList<>(players);
	}

	/** Returns {@code true} if the given player is a member or owner of any active war. */
	public static boolean isPlayerInWar(OfflinePlayer player) {
		UUID pid = player.getUniqueId();
		return getAll().stream().anyMatch(war ->
				war.getRegions().stream().anyMatch(r ->
						r.getOwner().getUniqueId().equals(pid) ||
								MemberManager.getMembersOfRegion(r).stream().anyMatch(m -> m.getPlayerId().equals(pid))
				)
		);
	}

	/** Returns {@code true} if the given player is a member or owner of any active war. */
	public static boolean isPlayerInWar(OfflinePlayer player, War war) {
		if (war == null) return false;

		UUID pid = player.getUniqueId();

		return war.getRegions().stream().anyMatch(r ->
				r.getOwner().getUniqueId().equals(pid) ||
						MemberManager.getMembersOfRegion(r).stream().anyMatch(m -> m.getPlayerId().equals(pid))
		);
	}

	/**
	 * Removes the given region from whichever war it belongs to, without ending the war.
	 * The caller is responsible for checking the war's state afterward and ending it if needed.
	 *
	 * @param regionId The UUID of the region to remove
	 */
	public static War removeRegionFromWar(long regionId) {
		for (War war : getAll()) {
			for (Region region : war.getRegions()) {
				if (region.getUniqueId() == regionId) {
					war.removeRegionId(region.getUniqueId());
					return war;
				}
			}
		}
		return null;
	}

	/** Checks whether any active war already carries the supplied name (case-insensitive). */
	public static boolean isNameUsed(String name) {
		return getAll().stream()
				.anyMatch(w -> w.getName().equalsIgnoreCase(name));
	}

	/** Returns {@code true} if the given region is currently participating in any war. */
	public static boolean isRegionInWar(long regionId) {
		return findWarByRegion(regionId) != null;
	}

	public static void tellPlayersWarEnded(List<OfflinePlayer> receivers, Region winner) {
		for (OfflinePlayer warPlayer : receivers) {
			if (warPlayer.isOnline()) {
				Player player = (Player) warPlayer;

				Messages.send(player, 214, new Placeholder()
						.add("{region}", winner.getName())
				);
			}
		}
	}

	public static void broadcastDeclarationOfWar(War war) {
		String type = Resources.<RegionsFile>get(ResourceType.Regions).getString("wars.broadcast-type");

		switch (type.toLowerCase()) {
			case "regions": {
				List<OfflinePlayer> players = WarManager.getMembersOfWar(war.getUniqueId());

				for (OfflinePlayer p : players) {
					if (p.isOnline()) {
						sendBroadcastMessage((Player) p, war, war.getRegions().getFirst(), war.getRegions().getLast());
					}
				}

				break;
			}
			case "server": {
				for (Player p : Bukkit.getOnlinePlayers()) {
					sendBroadcastMessage(p, war, war.getRegions().getFirst(), war.getRegions().getLast());
				}

				Logger.info("[Broadcast - War] Declaration of War: Name = " + war.getName() + ", Regions = " + war.getRegions().getFirst().getName() + " & " + war.getRegions().getLast().getName());

				break;
			}
		}
	}

	private static void sendBroadcastMessage(Player player, War war, Region regionA, Region regionB) {
		List<String> listString = Resources.<LanguageFile>get(ResourceType.Language).getStringList("147");

		Placeholder placeholder = new Placeholder()
				.add("{war-name}", war.getName())
				.add("{regionplayer}", regionA.getName())
				.add("{regiontarget}", regionB.getName())
				.add("{prize}", tfagaming.projects.minecraft.homestead.tools.java.Formatter.getBalance(war.getPrize()));

		player.playSound(player.getLocation(), Sound.EVENT_MOB_EFFECT_RAID_OMEN, SoundCategory.PLAYERS, 1f, 1f);

		for (String string : listString) {
			Messages.send(player, Formatter.applyPlaceholders(string, placeholder));
		}
	}

	public static void cleanStartup() {
		Logger.debug("Cleaning up wars data...");

		List<War> warsToEnd = new ArrayList<>();

		for (War war : Homestead.warsCache.getAll()) {
			if (war.getRegions().size() < 2) {
				warsToEnd.add(war);
			}
		}

		for (War war : warsToEnd) {
			WarManager.endWar(war.getUniqueId());
		}

		if (warsToEnd.isEmpty()) {
			Logger.debug("No data corruption was found!");
		} else {
			Logger.debug(warsToEnd.size() + " corrupted war(s) were removed.");
		}
	}
}