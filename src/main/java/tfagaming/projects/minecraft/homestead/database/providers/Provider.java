package tfagaming.projects.minecraft.homestead.database.providers;

import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;

import java.sql.SQLException;
import java.util.List;

public interface Provider {
	public List<Region> importRegions() throws Exception;
	public void exportRegions(List<Region> regions) throws Exception;

	public List<SubArea> importSubAreas() throws Exception;
	public void exportSubAreas(List<SubArea> subareas) throws Exception;

	public List<Level> importLevels() throws Exception;
	public void exportLevels(List<Level> levels) throws Exception;

	public List<War> importWars() throws Exception;
	public void exportWars(List<War> wars) throws Exception;

	public void prepareTables() throws Exception;
	public long getLatency();
	public void closeConnection() throws Exception;
}
