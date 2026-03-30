package tfagaming.projects.minecraft.homestead.resources.files;

import tfagaming.projects.minecraft.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;

public class LimitsFile extends ResourceFile {

	public LimitsFile(File file) throws FileNotFoundException {
		super(file);
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

	public String getLimitsMethod() {
		return getString("limits.method", "static");
	}

	public int getStaticNonOpRegions() {
		return getInt("limits.static.non-op.regions", 1);
	}

	public int getStaticNonOpChunksPerRegion() {
		return getInt("limits.static.non-op.chunks-per-region", 4);
	}

	public int getStaticNonOpMembersPerRegion() {
		return getInt("limits.static.non-op.members-per-region", 2);
	}

	public int getStaticNonOpSubAreasPerRegion() {
		return getInt("limits.static.non-op.subareas-per-region", 1);
	}

	public int getStaticNonOpMaxSubAreaVolume() {
		return getInt("limits.static.non-op.max-subarea-volume", 400);
	}

	public int getStaticNonOpMaxForceLoadedChunks() {
		return getInt("limits.static.non-op.max-force-loaded-chunks", 2);
	}

	public int getStaticNonOpCommandsCooldown() {
		return getInt("limits.static.non-op.commands-cooldown", 2);
	}

	public int getStaticOpRegions() {
		return getInt("limits.static.op.regions", 10);
	}

	public int getStaticOpChunksPerRegion() {
		return getInt("limits.static.op.chunks-per-region", 100);
	}

	public int getStaticOpMembersPerRegion() {
		return getInt("limits.static.op.members-per-region", 50);
	}

	public int getStaticOpSubAreasPerRegion() {
		return getInt("limits.static.op.subareas-per-region", 20);
	}

	public int getStaticOpMaxSubAreaVolume() {
		return getInt("limits.static.op.max-subarea-volume", 5000);
	}

	public int getStaticOpMaxForceLoadedChunks() {
		return getInt("limits.static.op.max-force-loaded-chunks", 100);
	}

	public int getStaticOpCommandsCooldown() {
		return getInt("limits.static.op.commands-cooldown", 0);
	}

	public int getGroupRegions(String group) {
		return getInt("limits.groups." + group + ".regions", 1);
	}

	public int getGroupChunksPerRegion(String group) {
		return getInt("limits.groups." + group + ".chunks-per-region", 4);
	}

	public int getGroupMembersPerRegion(String group) {
		return getInt("limits.groups." + group + ".members-per-region", 2);
	}

	public int getGroupSubAreasPerRegion(String group) {
		return getInt("limits.groups." + group + ".subareas-per-region", 1);
	}

	public int getGroupMaxSubAreaVolume(String group) {
		return getInt("limits.groups." + group + ".max-subarea-volume", 400);
	}

	public int getGroupMaxForceLoadedChunks(String group) {
		return getInt("limits.groups." + group + ".max-force-loaded-chunks", 2);
	}

	public int getGroupCommandsCooldown(String group) {
		return getInt("limits.groups." + group + ".commands-cooldown", 2);
	}

	public boolean hasPlayerOverride(String playerName) {
		return !getKeysUnderPath("player-limits." + playerName).isEmpty();
	}

	public int getPlayerRegions(String player) {
		return getInt("player-limits." + player + ".regions", -1);
	}

	public int getPlayerChunksPerRegion(String player) {
		return getInt("player-limits." + player + ".chunks-per-region", -1);
	}

	public int getPlayerMembersPerRegion(String player) {
		return getInt("player-limits." + player + ".members-per-region", -1);
	}

	public int getPlayerSubAreasPerRegion(String player) {
		return getInt("player-limits." + player + ".subareas-per-region", -1);
	}

	public int getPlayerMaxSubAreaVolume(String player) {
		return getInt("player-limits." + player + ".max-subarea-volume", -1);
	}

	public int getPlayerCommandsCooldown(String player) {
		return getInt("player-limits." + player + ".commands-cooldown", -1);
	}
}