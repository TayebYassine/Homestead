package tfagaming.projects.minecraft.homestead.tools.minecraft.chunks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import tfagaming.projects.minecraft.homestead.borders.BorderBlockRenderer;
import tfagaming.projects.minecraft.homestead.borders.ChunkParticlesSpawner;
import tfagaming.projects.minecraft.homestead.borders.SelectedAreaParticlesSpawner;
import tfagaming.projects.minecraft.homestead.managers.ChunkManager;
import tfagaming.projects.minecraft.homestead.managers.SubAreaManager;
import tfagaming.projects.minecraft.homestead.resources.ResourceType;
import tfagaming.projects.minecraft.homestead.resources.Resources;
import tfagaming.projects.minecraft.homestead.resources.files.RegionsFile;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;

public class ChunkBorder {
	public static BorderType getMethod() {
		boolean isEnabled = Resources.<RegionsFile>get(ResourceType.Regions).getBoolean("borders.enabled");

		if (!isEnabled) {
			return null;
		}

		String type = Resources.<RegionsFile>get(ResourceType.Regions).getString("borders.type");

		return switch (type) {
			case "particles" -> BorderType.PARTICLES;
			case "blocks" -> BorderType.BLOCKS;
			default -> null;
		};
	}

	public static Material getBlockType() {
		String blockType = Resources.<RegionsFile>get(ResourceType.Regions).getString("borders.block-type");

		return Material.getMaterial(blockType) == null ? Material.GOLD_BLOCK : Material.getMaterial(blockType);
	}

	public static void stop(Player player) {
		BorderBlockRenderer.removeAll(player);
		ChunkParticlesSpawner.cancelTask(player);
		SelectedAreaParticlesSpawner.cancelTask(player);
	}

	public static void show(Player player) {
		Region region = ChunkManager.getRegionOwnsTheChunk(player.getLocation().getChunk());
		SubArea subArea = null;

		if (region != null) {
			subArea = SubAreaManager.findSubAreaHasLocationInside(player.getLocation());
		}

		stop(player);

		BorderType borderType = getMethod();

		switch (borderType) {
			case PARTICLES: {
				if (region == null) {
					new ChunkParticlesSpawner(player);
				} else {
					if (subArea != null) {
						new SelectedAreaParticlesSpawner(player, subArea.getFirstPoint(), subArea.getSecondPoint());
					} else {
						new ChunkParticlesSpawner(player);
					}
				}

				break;
			}

			case BLOCKS: {
				if (region != null) {
					if (subArea != null) {
						new SelectedAreaParticlesSpawner(player, subArea.getFirstPoint(), subArea.getSecondPoint());
					} else {
						BorderBlockRenderer.show(player, region);
					}
				}

				break;
			}

			case null: {
				break;
			}
		}
	}

	public enum BorderType {
		PARTICLES,
		BLOCKS
	}
}
