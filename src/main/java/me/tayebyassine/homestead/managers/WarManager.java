package me.tayebyassine.homestead.managers;

import me.tayebyassine.homestead.Homestead;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.models.Region;
import me.tayebyassine.homestead.models.War;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.LanguageFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.util.java.Formatter;
import me.tayebyassine.homestead.util.java.Placeholder;
import me.tayebyassine.homestead.util.minecraft.chat.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class that manages {@link War}.
 */
public final class WarManager {
    private WarManager() {
        throw new AssertionError("Uninstantiable class");
    }

    /**
     * Returns the total number of active wars.
     *
     * @return war count
     */
    public static int getWarCount() {
        return getAll().size();
    }

    /**
     * Returns all active wars.
     *
     * @return list of wars
     */
    public static List<War> getAll() {
        return Homestead.WAR_CACHE.getAll();
    }

    /**
     * Returns all active wars (alias for semantic clarity).
     *
     * @return list of active wars
     */
    public static List<War> getActiveWars() {
        return getAll();
    }

    /**
     * Retrieves the war with the exact ID.
     *
     * @param warId the war ID
     * @return the war, or {@code null}
     */
    public static War findWar(long warId) {
        return Homestead.WAR_CACHE.get(warId);
    }

    /**
     * Retrieves the war with the exact name (case-sensitive).
     *
     * @param name the war name
     * @return the war, or {@code null}
     */
    public static War findWar(String name) {
        return getAll().stream()
                .filter(w -> w.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a war by name (case-insensitive).
     *
     * @param name the war name
     * @return the war, or {@code null}
     */
    public static War findWarIgnoreCase(String name) {
        return getAll().stream()
                .filter(w -> w.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns all war names for tab-completion.
     *
     * @return list of war names
     */
    public static List<String> getWarNames() {
        return getAll().stream()
                .map(War::getName)
                .collect(Collectors.toList());
    }

    /**
     * Returns the war the given region is participating in.
     *
     * @param regionId the region ID
     * @return the war, or {@code null}
     */
    public static War findWarByRegion(long regionId) {
        return getAll().stream()
                .filter(w -> w.getRegionIds().contains(regionId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns all wars a region is participating in.
     *
     * @param region the region
     * @return list of wars
     */
    public static List<War> getWarsByRegion(Region region) {
        return getWarsByRegion(region.getUniqueId());
    }

    /**
     * Returns all wars a region is participating in.
     *
     * @param regionId the region ID
     * @return list of wars
     */
    public static List<War> getWarsByRegion(long regionId) {
        return getAll().stream()
                .filter(w -> w.getRegionIds().contains(regionId))
                .collect(Collectors.toList());
    }

    /**
     * Declares a new war between exactly two regions.
     *
     * @param name    the war display name
     * @param prize   the reward given to the winning region; must be &gt; 0
     * @param regionA the first participating region
     * @param regionB the second participating region
     * @return the created war
     * @throws IllegalArgumentException if regions are identical
     * @throws IllegalStateException    if either region is already in a war
     */
    public static War declareWar(String name, double prize, Region regionA, Region regionB) {
        if (regionA.getUniqueId() == regionB.getUniqueId()) {
            throw new IllegalArgumentException("A war must involve two distinct regions.");
        }

        if (isRegionInWar(regionA.getUniqueId()) || isRegionInWar(regionB.getUniqueId())) {
            throw new IllegalStateException("One of the regions is currently in a war.");
        }

        War war = new War(name);

        war.setDescription(Resources.<LanguageFile>get(ResourceType.Language).getDefaultWarDescription());
        war.addRegionId(regionA.getUniqueId());
        war.addRegionId(regionB.getUniqueId());
        war.setPrize(prize);

        Homestead.WAR_CACHE.putOrUpdate(war);

        return war;
    }

    /**
     * Checks if two regions can declare war (pre-flight validation).
     *
     * @param regionA the first region
     * @param regionB the second region
     * @return {@code true} if war can be declared
     */
    public static boolean canDeclareWar(Region regionA, Region regionB) {
        if (regionA.getUniqueId() == regionB.getUniqueId()) return false;
        return !isRegionInWar(regionA.getUniqueId()) && !isRegionInWar(regionB.getUniqueId());
    }

    /**
     * Adds a region to an existing war.
     *
     * @param war    the war
     * @param region the region to add
     * @return {@code true} if added successfully
     */
    public static boolean addRegionToWar(War war, Region region) {
        if (war == null || isRegionInWar(region.getUniqueId())) return false;
        war.addRegionId(region.getUniqueId());
        return true;
    }

    /**
     * Ends and removes the war with the given ID.
     *
     * @param warId the war ID
     */
    public static void endWar(long warId) {
        Homestead.WAR_CACHE.remove(warId);
    }

    /**
     * Ends a war and declares a specific winner.
     *
     * @param war    the war
     * @param winner the winning region
     */
    public static void forceEndWar(War war, Region winner) {
        if (war == null || !war.getRegionIds().contains(winner.getUniqueId())) return;

        List<OfflinePlayer> members = getMembersOfWar(war);
        tellPlayersWarEnded(members, winner);
        endWar(war.getUniqueId());
    }

    /**
     * Ends all active wars immediately.
     *
     * @return the number of wars ended
     */
    public static int endAllWars() {
        List<Long> toEnd = getAll().stream()
                .map(War::getUniqueId)
                .toList();

        for (Long id : toEnd) {
            endWar(id);
        }
        return toEnd.size();
    }

    /**
     * Returns how long a war has been running in milliseconds.
     *
     * @param war the war
     * @return duration in milliseconds
     */
    public static long getWarDuration(War war) {
        return System.currentTimeMillis() - war.getStartedAt();
    }

    /**
     * Returns the war with the longest duration.
     *
     * @return the longest running war, or {@code null}
     */
    public static War getLongestWar() {
        return getAll().stream()
                .max(Comparator.comparingLong(WarManager::getWarDuration))
                .orElse(null);
    }

    /**
     * Returns the war with the highest prize.
     *
     * @return the richest war, or {@code null}
     */
    public static War getRichestWar() {
        return getAll().stream()
                .max(Comparator.comparingDouble(War::getPrize))
                .orElse(null);
    }

    /**
     * Returns wars sorted by prize descending.
     *
     * @param limit maximum results
     * @return list of wars
     */
    public static List<War> getWarLeaderboard(int limit) {
        return getAll().stream()
                .sorted(Comparator.comparingDouble(War::getPrize).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns the most recently declared wars.
     *
     * @param limit maximum results
     * @return list of recent wars
     */
    public static List<War> getRecentWars(int limit) {
        return getAll().stream()
                .sorted(Comparator.comparingLong(War::getStartedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns all regions participating in a war.
     *
     * @param war the war
     * @return list of regions
     */
    public static List<Region> getWarParticipants(War war) {
        return war != null ? war.getRegions() : Collections.emptyList();
    }

    /**
     * Returns the number of regions in a war.
     *
     * @param war the war
     * @return participant count
     */
    public static int getWarParticipantCount(War war) {
        return war != null ? war.getRegionIds().size() : 0;
    }

    /**
     * Returns the total number of unique players involved in a war.
     *
     * @param war the war
     * @return player count
     */
    public static int getWarMemberCount(War war) {
        return getMembersOfWar(war).size();
    }

    /**
     * Returns only online players participating in a war.
     *
     * @param war the war
     * @return list of online players
     */
    public static List<Player> getOnlineWarMembers(War war) {
        List<Player> online = new ArrayList<>();
        for (OfflinePlayer p : getMembersOfWar(war)) {
            if (p.isOnline()) {
                online.add((Player) p);
            }
        }
        return online;
    }

    /**
     * Collects all members and owners from every region in the war.
     *
     * @param warId the war ID
     * @return list of unique players
     */
    public static List<OfflinePlayer> getMembersOfWar(long warId) {
        War war = findWar(warId);
        return getMembersOfWar(war);
    }

    /**
     * Collects all members and owners from every region in the war.
     *
     * @param war the war
     * @return list of unique players
     */
    public static List<OfflinePlayer> getMembersOfWar(War war) {
        if (war == null || war.getRegionIds().size() < 2) {
            return Collections.emptyList();
        }

        Set<OfflinePlayer> players = new HashSet<>();
        for (Region region : war.getRegions()) {
            MemberManager.getMembersOfRegion(region).forEach(m -> {
                OfflinePlayer p = m.getPlayer();
                if (p != null) players.add(p);
            });
            OfflinePlayer owner = region.getOwner();
            if (owner != null) players.add(owner);
        }

        return new ArrayList<>(players);
    }

    /**
     * Returns {@code true} if the given player is a member or owner of any active war.
     *
     * @param player the player
     * @return {@code true} if in any war
     */
    public static boolean isPlayerInWar(OfflinePlayer player) {
        UUID pid = player.getUniqueId();
        return getAll().stream().anyMatch(war ->
                war.getRegions().stream().anyMatch(r -> {
                    OfflinePlayer owner = r.getOwner();
                    return (owner != null && owner.getUniqueId().equals(pid)) ||
                            MemberManager.getMembersOfRegion(r).stream().anyMatch(m -> m.getPlayerId().equals(pid));
                })
        );
    }

    /**
     * Returns {@code true} if the given player is a member or owner of the specified war.
     *
     * @param player the player
     * @param war    the war
     * @return {@code true} if in the war
     */
    public static boolean isPlayerInWar(OfflinePlayer player, War war) {
        if (war == null) return false;

        UUID pid = player.getUniqueId();

        return war.getRegions().stream().anyMatch(r -> {
            OfflinePlayer owner = r.getOwner();
            return (owner != null && owner.getUniqueId().equals(pid)) ||
                    MemberManager.getMembersOfRegion(r).stream().anyMatch(m -> m.getPlayerId().equals(pid));
        });
    }

    /**
     * Returns {@code true} if the given region is currently participating in any war.
     *
     * @param region the region
     * @return {@code true} if in a war
     */
    public static boolean isRegionInWar(Region region) {
        return isRegionInWar(region.getUniqueId());
    }

    /**
     * Returns {@code true} if the given region is currently participating in any war.
     *
     * @param regionId the region ID
     * @return {@code true} if in a war
     */
    public static boolean isRegionInWar(long regionId) {
        return findWarByRegion(regionId) != null;
    }

    /**
     * Removes the given region from whichever war it belongs to, without ending the war.
     * The caller is responsible for checking the war's state afterward and ending it if needed.
     *
     * @param regionId the region ID to remove
     * @return the war the region was removed from, or {@code null}
     */
    public static War removeRegionFromWar(long regionId) {
        for (War war : getAll()) {
            if (war.getRegionIds().contains(regionId)) {
                war.removeRegionId(regionId);
                return war;
            }
        }
        return null;
    }

    /**
     * Updates the prize for a war.
     *
     * @param war      the war
     * @param newPrize the new prize amount
     */
    public static void updateWarPrize(War war, double newPrize) {
        if (war == null) return;
        war.setPrize(newPrize);
    }

    /**
     * Sends a message to all online participants of a war.
     *
     * @param war     the war
     * @param message the message to send
     */
    public static void sendWarMessage(War war, String message) {
        for (Player player : getOnlineWarMembers(war)) {
            player.sendMessage(message);
        }
    }

    /**
     * Plays a sound to all online participants of a war.
     *
     * @param war   the war
     * @param sound the sound to play
     */
    public static void playWarSound(War war, Sound sound) {
        for (Player player : getOnlineWarMembers(war)) {
            player.playSound(player.getLocation(), sound, SoundCategory.PLAYERS, 1f, 1f);
        }
    }

    /**
     * Notifies all war participants that the war has ended.
     *
     * @param receivers the players to notify
     * @param winner    the winning region
     */
    public static void tellPlayersWarEnded(List<OfflinePlayer> receivers, Region winner) {
        for (OfflinePlayer warPlayer : receivers) {
            if (warPlayer.isOnline()) {
                Player player = (Player) warPlayer;

                Messages.send(player, "common.war_end", winner.getName());
            }
        }
    }

    /**
     * Broadcasts the declaration of war to participants or the entire server.
     *
     * @param war the war to broadcast
     */
    public static void broadcastDeclarationOfWar(War war) {
        String type = Resources.<RegionsFile>get(ResourceType.Regions).getWarBroadcastType();

        switch (type.toLowerCase()) {
            case "regions": {
                List<OfflinePlayer> players = WarManager.getMembersOfWar(war.getUniqueId());

                for (OfflinePlayer p : players) {
                    if (p.isOnline()) {
                        sendBroadcastMessage((Player) p, war, war.getRegions().get(0), war.getRegions().get(1));
                    }
                }

                break;
            }
            case "server": {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    sendBroadcastMessage(p, war, war.getRegions().get(0), war.getRegions().get(1));
                }

                Logger.debug("[Broadcast - War] Declaration of War: Name = " + war.getName() + ", Regions = " + war.getRegions().get(0).getName() + " & " + war.getRegions().get(1).getName());

                break;
            }
        }
    }

    private static void sendBroadcastMessage(Player player, War war, Region regionA, Region regionB) {
        List<String> listString = Resources.<LanguageFile>get(ResourceType.Language).getWarDeclarationMessages();

        Placeholder placeholder = new Placeholder()
                .add("{war-name}", war.getName())
                .add("{regionplayer}", regionA.getName())
                .add("{regiontarget}", regionB.getName())
                .add("{prize}", Formatter.getBalance(war.getPrize()));

        player.playSound(player.getLocation(), Sound.EVENT_MOB_EFFECT_RAID_OMEN, SoundCategory.PLAYERS, 1f, 1f);

        for (String string : listString) {
            Messages.sendString(player, Formatter.applyPlaceholders(string, placeholder));
        }
    }

    /**
     * Checks whether any active war already carries the supplied name (case-insensitive).
     *
     * @param name the name to check
     * @return {@code true} if the name is used
     */
    public static boolean isNameUsed(String name) {
        return getAll().stream()
                .anyMatch(w -> w.getName().equalsIgnoreCase(name));
    }

    /**
     * Checks whether any active war already carries the supplied name (case-sensitive).
     *
     * @param name the name to check
     * @return {@code true} if the name is used
     */
    public static boolean isWarNameUsed(String name) {
        return getAll().stream()
                .anyMatch(w -> w.getName().equals(name));
    }

    /**
     * Removes all wars with invalid references:<br>
     * - Regions that no longer exist<br>
     * - Wars with fewer than 2 valid regions
     *
     * @return number of corrupted wars removed
     */
    public static int cleanupInvalidWars() {
        List<Long> toRemove = new ArrayList<>();

        for (War war : Homestead.WAR_CACHE.getAll()) {
            List<Long> validRegions = new ArrayList<>();
            for (Long regionId : war.getRegionIds()) {
                if (RegionManager.findRegion(regionId) != null) {
                    validRegions.add(regionId);
                }
            }

            if (validRegions.size() < 2) {
                toRemove.add(war.getUniqueId());
            } else if (validRegions.size() != war.getRegionIds().size()) {
                war.setRegionIds(validRegions);
            }
        }

        for (Long id : toRemove) {
            endWar(id);
        }
        return toRemove.size();
    }
}
