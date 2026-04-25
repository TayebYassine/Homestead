package tfagaming.projects.minecraft.homestead.database.providers;

import tfagaming.projects.minecraft.homestead.models.*;

import java.util.List;

public interface Provider {
	List<Region> importRegions() throws Exception;

	void exportRegions(List<Region> regions) throws Exception;

	List<RegionMember> importRegionMembers() throws Exception;

	void exportRegionMembers(List<RegionMember> regions) throws Exception;

	List<RegionChunk> importRegionChunks() throws Exception;

	void exportRegionChunks(List<RegionChunk> regions) throws Exception;

	List<RegionLog> importRegionLogs() throws Exception;

	void exportRegionLogs(List<RegionLog> regions) throws Exception;

	List<RegionRate> importRegionRates() throws Exception;

	void exportRegionRates(List<RegionRate> regions) throws Exception;

	List<RegionInvite> importRegionInvites() throws Exception;

	void exportRegionInvites(List<RegionInvite> regions) throws Exception;

	List<RegionBan> importRegionBannedPlayers() throws Exception;

	void exportRegionBannedPlayers(List<RegionBan> regions) throws Exception;

	List<SubArea> importSubAreas() throws Exception;

	void exportSubAreas(List<SubArea> subareas) throws Exception;

	List<Level> importLevels() throws Exception;

	void exportLevels(List<Level> levels) throws Exception;

	List<War> importWars() throws Exception;

	void exportWars(List<War> wars) throws Exception;

	void prepareTables() throws Exception;

	long getLatency();

	void closeConnection() throws Exception;
}
