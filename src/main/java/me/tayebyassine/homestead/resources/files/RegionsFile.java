package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;
import org.bukkit.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Typed accessor for {@code regions.yml}.
 */
public final class RegionsFile extends ResourceFile {

    public RegionsFile(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Whether invalid data should be cleaned up on startup.
     *
     * @return {@code true} if clean-startup is enabled, defaults to {@code true}
     */
    public boolean isCleanStartupEnabled() {
        return getBoolean("clean-startup", true);
    }

    /**
     * Whether the instant trust-acceptance system is enabled.
     *
     * @return {@code true} if trust acceptance is skipped, defaults to {@code false}
     */
    public boolean isInstantTrustSystemEnabled() {
        return getBoolean("special-feat.ignore-trust-acceptance-system");
    }

    /**
     * Whether players should be teleported back to region spawn when
     * entering the End exit portal.
     *
     * @return {@code true} if teleportation is active, defaults to {@code false}
     */
    public boolean teleportPlayersBackToTegionSpawnWhenEnteringEndExitPortal() {
        return getBoolean("special-feat.teleport-players-back-to-region-spawn-when-entering-end-exit-portal");
    }

    /**
     * Whether region protection should be ignored for actions in disabled worlds.
     *
     * @return {@code true} to bypass protection, defaults to {@code false}
     */
    public boolean isRegionProtectionDisabledInDisabledWorlds() {
        return getBoolean("special-feat.ignore-region-protection-if-action-in-disabled-world");
    }

    /**
     * Whether TNT should only explode below sea level inside regions.
     *
     * @return {@code true} to restrict TNT, defaults to {@code false}
     */
    public boolean isTntRestrictedToBelowSeaLevel() {
        return getBoolean("special-feat.tnt-explodes-only-below-sea-level");
    }

    /**
     * The price charged per claimed chunk.
     *
     * @return chunk price, defaults to {@code 0.0}
     */
    public double getChunkPrice() {
        return getDouble("chunk-price");
    }

    /**
     * Whether the join welcome message is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isWelcomeMessageEnabled() {
        return getBoolean("welcome-message.enabled");
    }

    /**
     * The welcome message sent to players on join.
     *
     * @return welcome message text
     */
    public String getWelcomeMessage() {
        return getString("welcome-message.message");
    }

    /**
     * Whether the welcome-sign feature is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isWelcomeSignEnabled() {
        return getBoolean("welcome-signs.enabled");
    }

    /**
     * Whether enter/exit region messages are enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isEnterExitRegionMessageEnabled() {
        return getBoolean("enter-exit-region-message.enabled");
    }

    /**
     * The message delivery type (e.g. {@code "chat"}, {@code "title"},
     * {@code "actionbar"}).
     *
     * @return the message type string
     */
    public String getEnterExitRegionMessageType() {
        return getString("enter-exit-region-message.type");
    }

    /**
     * The title lines shown when a player enters a region.
     *
     * @return title lines, never {@code null}
     */
    public List<String> getRegionEnterTitle() {
        return getStringList("enter-exit-region-message.messages.enter.title");
    }

    /**
     * The action-bar message shown when a player enters a region.
     *
     * @return action-bar text
     */
    public String getRegionEnterActionbar() {
        return getString("enter-exit-region-message.messages.enter.actionbar");
    }

    /**
     * The chat message shown when a player enters a region.
     *
     * @return chat message text
     */
    public String getRegionEnterChat() {
        return getString("enter-exit-region-message.messages.enter.chat");
    }

    /**
     * The title lines shown when a player exits a region.
     *
     * @return title lines, never {@code null}
     */
    public List<String> getRegionExitTitle() {
        return getStringList("enter-exit-region-message.messages.exit.title");
    }

    /**
     * The action-bar message shown when a player exits a region.
     *
     * @return action-bar text
     */
    public String getRegionExitActionbar() {
        return getString("enter-exit-region-message.messages.exit.actionbar");
    }

    /**
     * The chat message shown when a player exits a region.
     *
     * @return chat message text
     */
    public String getRegionExitChat() {
        return getString("enter-exit-region-message.messages.exit.chat");
    }

    /**
     * Whether the chunk-border visualisation is enabled.
     *
     * @return {@code true} if borders are active, defaults to {@code false}
     */
    public boolean isBordersEnabled() {
        return getBoolean("borders.enabled");
    }

    /**
     * The block type used for chunk borders.
     *
     * @return the material name string
     */
    public String getBordersBlockType() {
        return getString("borders.block-type");
    }

    /**
     * The RGB colour list for a specific dust-colour type.
     *
     * @param type the dust colour category
     * @return RGB values as an {@link org.bukkit.Color}
     */
    public Color getDustColor(DustColorType type) {
        List<Integer> rgb = getIntegerList("borders.dust-colors." + type.getName());

        if (rgb.size() == 3) {
            return Color.fromRGB(rgb.getFirst(), rgb.get(1), rgb.get(2));
        }

        return Color.fromRGB(255, 255, 255);
    }

    /**
     * The particle dust size for border visualisation.
     *
     * @return dust size, defaults to {@code 3.0F}
     */
    public float getDustSize() {
        return getFloat("borders.dust-size", 3.0F);
    }

    /**
     * Whether the adjacent-chunks rule is enabled (auto-claim neighbours).
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isAdjacentChunksRuleEnabled() {
        return getBoolean("adjacent-chunks");
    }

    /**
     * Whether the sub-area system is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isSubAreasEnabled() {
        return getBoolean("sub-areas.enabled");
    }

    /**
     * Whether the member-reward system is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isRewardsEnabled() {
        return getBoolean("rewards.enabled");
    }

    /**
     * The chunk bonus awarded per trusted member.
     *
     * @return bonus chunks per member, defaults to {@code 0}
     */
    public int getRewardChunksPerMember() {
        return getInt("rewards.for-each-member.chunks", 0);
    }

    /**
     * The sub-area bonus awarded per trusted member.
     *
     * @return bonus sub-areas per member, defaults to {@code 0}
     */
    public int getRewardSubAreasPerMember() {
        return getInt("rewards.for-each-member.subareas", 0);
    }

    /**
     * The list of play-time reward definitions.
     *
     * @return list of reward maps, never {@code null}
     */
    @SuppressWarnings("unchecked")
    public List<Map<?, ?>> getRewardsByPlaytime() {
        return getConfig().getMapList("rewards.by-playtime");
    }

    /**
     * Whether the region barrel-storage feature is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isRegionStorageEnabled() {
        return getBoolean("storage.enabled");
    }

    /**
     * The storage barrel size (valid Bukkit inventory sizes).
     *
     * @return storage size, clamped to a valid Bukkit size, defaults to {@code 27}
     */
    public int getRegionStorageSize() {
        int size = getInt("storage.size");

        if (!List.of(9, 18, 27, 36, 45, 54).contains(size)) {
            size = 27;
        }

        return size;
    }

    /**
     * Whether the region renting system is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isRentingEnabled() {
        return getBoolean("renting.enabled");
    }

    /**
     * The default rent price per chunk.
     *
     * @return default price, defaults to {@code 1500.0}
     */
    public double getDefaultRentPrice() {
        return getDouble("renting.price.default", 1500.0);
    }

    /**
     * The minimum allowed rent price per chunk.
     *
     * @return minimum price, defaults to {@code 500.0}
     */
    public double getMinRentPrice() {
        return getDouble("renting.price.min", 500.0);
    }

    /**
     * The maximum allowed rent price per chunk.
     *
     * @return maximum price, defaults to {@code 10_000_000.0}
     */
    public double getMaxRentPrice() {
        return getDouble("renting.price.max", 10_000_000.0);
    }

    /**
     * The default rent duration in days.
     *
     * @return default days, defaults to {@code 7}
     */
    public int getDefaultRentDays() {
        return getInt("renting.duration.default", 7);
    }

    /**
     * The minimum allowed rent duration in days.
     *
     * @return minimum days, defaults to {@code 1}
     */
    public int getMinRentDays() {
        return getInt("renting.duration.min", 1);
    }

    /**
     * The maximum allowed rent duration in days.
     *
     * @return maximum days, defaults to {@code 84}
     */
    public int getMaxRentDays() {
        return getInt("renting.duration.max", 84);
    }

    /**
     * The default security deposit amount.
     *
     * @return default deposit, defaults to {@code 500.0}
     */
    public double getDefaultSecurityDeposit() {
        return getDouble("renting.security-deposit.default", 500.0);
    }

    /**
     * The minimum allowed security deposit.
     *
     * @return minimum deposit, defaults to {@code 0.0}
     */
    public double getMinSecurityDeposit() {
        return getDouble("renting.security-deposit.min", 0.0);
    }

    /**
     * The maximum allowed security deposit.
     *
     * @return maximum deposit, defaults to {@code 100_000.0}
     */
    public double getMaxSecurityDeposit() {
        return getDouble("renting.security-deposit.max", 100_000.0);
    }

    /**
     * The number of days' notice required before vacating a rented region.
     *
     * @return notice days, defaults to {@code 3}
     */
    public int getNoticeToVacateDays() {
        return getInt("renting.notice-to-vacate", 3);
    }

    /**
     * Whether the member-tax system is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isTaxesEnabled() {
        return getBoolean("taxes.enabled");
    }

    /**
     * The minimum allowed member tax rate.
     *
     * @return minimum tax, defaults to {@code 0.0}
     */
    public double getMinTax() {
        return getDouble("taxes.min-tax");
    }

    /**
     * The maximum allowed member tax rate.
     *
     * @return maximum tax, defaults to {@code 0.0}
     */
    public double getMaxTax() {
        return getDouble("taxes.max-tax");
    }

    /**
     * The interval (in minutes) between tax collection ticks.
     *
     * @return tax timer in minutes, defaults to {@code 0}
     */
    public int getTaxTimer() {
        return getInt("taxes.tax-timer");
    }

    /**
     * Whether the upkeep (region-cost) system is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isUpkeepEnabled() {
        return getBoolean("upkeep.enabled");
    }

    /**
     * The interval (in minutes) between upkeep ticks.
     *
     * @return upkeep timer in minutes, defaults to {@code 0}
     */
    public int getUpkeepTimer() {
        return getInt("upkeep.upkeep-timer");
    }

    /**
     * The upkeep cost charged per claimed chunk.
     *
     * @return per-chunk cost, defaults to {@code 0.0}
     */
    public double getUpkeepPerChunk() {
        return getDouble("upkeep.per-chunk");
    }

    /**
     * The delay (in minutes) before the first upkeep charge after a region
     * is created.
     *
     * @return start-upkeep delay in minutes, defaults to {@code 0}
     */
    public int getStartUpkeep() {
        return getInt("upkeep.start-upkeep");
    }

    /**
     * Whether the war system is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isWarsEnabled() {
        return getBoolean("wars.enabled");
    }

    /**
     * The minimum war prize amount.
     *
     * @return minimum prize, defaults to {@code 0.0}
     */
    public double getMinWarPrize() {
        return getDouble("wars.min-prize");
    }

    /**
     * The maximum war prize amount.
     *
     * @return maximum prize, defaults to {@code 0.0}
     */
    public double getMaxWarPrize() {
        return getDouble("wars.max-prize");
    }

    /**
     * Whether winners keep their inventory on death during a war.
     *
     * @return {@code true} to keep inventory, defaults to {@code false}
     */
    public boolean isWarKeepInventory() {
        return getBoolean("wars.keep-inventory");
    }

    /**
     * Whether the winning player receives a player head on kill during a war.
     *
     * @return {@code true} to give heads, defaults to {@code false}
     */
    public boolean isWarGiveHead() {
        return getBoolean("wars.give-head");
    }

    /**
     * The broadcast delivery type for war events (e.g. {@code "chat"}).
     *
     * @return broadcast type string
     */
    public String getWarBroadcastType() {
        return getString("wars.broadcast-type");
    }

    /**
     * Whether the delayed-teleport system is enabled.
     *
     * @return {@code true} if enabled, defaults to {@code false}
     */
    public boolean isDelayedTeleportEnabled() {
        return getBoolean("delayed-teleport.enabled");
    }

    /**
     * Whether the teleport countdown should be cancelled when the player moves.
     *
     * @return {@code true} to cancel on move, defaults to {@code false}
     */
    public boolean isDelayedTeleportCancelOnMove() {
        return getBoolean("delayed-teleport.cancel-on-move");
    }

    /**
     * Whether operators bypass the delayed-teleport system.
     *
     * @return {@code true} to bypass for ops, defaults to {@code false}
     */
    public boolean isDelayedTeleportIgnoreOperators() {
        return getBoolean("delayed-teleport.ignore-operators");
    }

    /**
     * The delay (in seconds) before the teleport completes.
     *
     * @return delay in seconds, defaults to {@code 0}
     */
    public int getDelayedTeleportDelay() {
        return getInt("delayed-teleport.delay");
    }

    /**
     * Whether the boss-bar countdown is shown during delayed teleport.
     *
     * @return {@code true} if the boss bar is enabled, defaults to {@code true}
     */
    public boolean isDelayedTeleportBossBarEnabled() {
        return getBoolean("delayed-teleport.boss-bar.enabled", true);
    }

    /**
     * The price charged for using a delayed teleport.
     *
     * @return teleport price, defaults to {@code 0.0}
     */
    public double getDelayedTeleportPrice() {
        return getDouble("delayed-teleport.price");
    }

    /**
     * A message template for the selection tool.
     *
     * @param path the message sub-path (e.g. {@code "selected-chunk"})
     * @return the message text
     */
    public String getSelectionToolMessage(String path) {
        return getString("selection-tool.messages." + path);
    }

    /**
     * The display name of the selection tool item.
     *
     * @return item name
     */
    public String getSelectionToolItemName() {
        return getString("selection-tool.item.name");
    }

    /**
     * The lore lines of the selection tool item.
     *
     * @return lore lines, never {@code null}
     */
    public List<String> getSelectionToolItemLore() {
        return getStringList("selection-tool.item.lore");
    }

    /**
     * The material type of the selection tool item.
     *
     * @return material name
     */
    public String getSelectionToolItemType() {
        return getString("selection-tool.item.type");
    }

    /**
     * Whether operators bypass a specific cooldown.
     *
     * @param key the cooldown identifier
     * @return {@code true} to bypass for ops, defaults to {@code true}
     */
    public boolean isCooldownIgnoreOperators(String key) {
        return getBoolean("cooldown." + key + ".ignore-operators", true);
    }

    /**
     * The duration (in seconds) of a specific cooldown.
     *
     * @param key the cooldown identifier
     * @return cooldown seconds, defaults to {@code 0}
     */
    public int getCooldownValue(String key) {
        return getInt("cooldown." + key + ".value", 0);
    }

    /**
     * Whether private-chat messages are logged to console.
     *
     * @return {@code true} if logging is enabled, defaults to {@code false}
     */
    public boolean isLogPrivateChat() {
        return getBoolean("log-private-chat");
    }

    /**
     * Whether the target-region session should be automatically set when a
     * player enters a region.
     *
     * @return {@code true} if auto-set is enabled, defaults to {@code false}
     */
    public boolean isAutoSetTargetRegion() {
        return getBoolean("autoset-target-region");
    }

    /**
     * The list of world names that are completely excluded from region actions.
     *
     * @return disabled world names, never {@code null}
     */
    public List<String> getDisabledWorldsExact() {
        return getStringList("disabled-worlds-exact");
    }

    /**
     * The list of regex patterns matching world names excluded from region actions.
     *
     * @return disabled world patterns, never {@code null}
     */
    public List<String> getDisabledWorldsPattern() {
        return getStringList("disabled-worlds-pattern");
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
