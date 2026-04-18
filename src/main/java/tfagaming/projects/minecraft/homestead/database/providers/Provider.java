package tfagaming.projects.minecraft.homestead.database.providers;

import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.util.List;

public interface Provider {
	List<Region> importRegions() throws Exception;

	void exportRegions(List<Region> regions) throws Exception;

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
